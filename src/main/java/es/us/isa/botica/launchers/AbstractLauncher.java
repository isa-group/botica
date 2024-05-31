package es.us.isa.botica.launchers;

import com.rabbitmq.client.DeliverCallback;
import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.configuration.bot.BotInstanceConfiguration;
import es.us.isa.botica.configuration.bot.BotPublishConfiguration;
import es.us.isa.botica.configuration.bot.BotTypeConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.BotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ProactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.bot.lifecycle.ReactiveBotLifecycleConfiguration;
import es.us.isa.botica.configuration.broker.RabbitMqConfiguration;
import es.us.isa.botica.rabbitmq.RabbitMQManager;
import es.us.isa.botica.utils.bot.BotRabbitConfig;
import es.us.isa.botica.utils.logging.ExceptionUtils;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * This class serves as the base launcher for bots and provides methods for
 * connecting to RabbitMQ and launching bot-related tasks.
 */
@Getter
@Setter
public abstract class AbstractLauncher {

    protected static final Logger logger = LogManager.getLogger(AbstractLauncher.class);

    protected JSONObject messageData;
    protected final MainConfiguration configuration;
    protected final BotTypeConfiguration botTypeConfiguration;
    protected final BotInstanceConfiguration botInstanceConfiguration;
    protected final BotLifecycleConfiguration lifecycleConfiguration;
    protected final RabbitMQManager messageSender;

    protected String launcherPackage;                                       // The launcher package name.

    protected AbstractLauncher(MainConfiguration configuration) {
        this.configuration = configuration;
        String botType = System.getenv("BOTICA_BOT_TYPE");
        String botId = System.getenv("BOTICA_BOT_ID");

        this.botTypeConfiguration = configuration.getBotTypes().get(botType);
        this.botInstanceConfiguration = botTypeConfiguration.getInstances().get(botId);
        this.messageSender = new RabbitMQManager((RabbitMqConfiguration) configuration.getBrokerConfiguration());
        this.lifecycleConfiguration = botInstanceConfiguration.getLifecycleConfiguration() == null
                ? botTypeConfiguration.getLifecycleConfiguration()
                : botInstanceConfiguration.getLifecycleConfiguration();
    }

    /**
     * Launches the bot.
     */
    public void launchBot() {
        try {
            if (lifecycleConfiguration instanceof ReactiveBotLifecycleConfiguration){
                this.launchAction();
            }else if (lifecycleConfiguration instanceof ProactiveBotLifecycleConfiguration) {
                ProactiveBotLifecycleConfiguration proactiveConfiguration = (ProactiveBotLifecycleConfiguration) lifecycleConfiguration;
                this.checkBrokerConnection();
                long initialDelay = proactiveConfiguration.getInitialDelay();
                long period = proactiveConfiguration.getPeriod();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(this::launchAction, initialDelay, period, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    private void launchAction() {
        BotPublishConfiguration publishConfiguration = botTypeConfiguration.getPublishConfiguration();
        BotRabbitConfig botRabbitConfig = new BotRabbitConfig(botTypeConfiguration.getName(),
                publishConfiguration.getKey(), publishConfiguration.getOrder(), botInstanceConfiguration.isPersistent());
        try {
            String queueName = botTypeConfiguration.getName(); // TODO: queue by bot
            boolean autoDelete = false; // TODO: queue by bot
            List<Boolean> queueOptions = Arrays.asList(true, false, autoDelete);

            this.messageSender.connect(queueName, botTypeConfiguration.getSubscribeKeys(), queueOptions, botInstanceConfiguration.getId());
            asyncShutdownConnection();
            if (lifecycleConfiguration instanceof ReactiveBotLifecycleConfiguration) {
                String order = ((ReactiveBotLifecycleConfiguration) lifecycleConfiguration).getOrder();
                this.messageSender.receiveMessage(queueName, botRabbitConfig, order, this.launcherPackage);
            } else if (lifecycleConfiguration instanceof ProactiveBotLifecycleConfiguration) {
                this.messageSender.proactiveAction(botRabbitConfig, this.launcherPackage);
            }
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error launching bot: " + botInstanceConfiguration.getId(), e);
        }
    }

    // Executes bot action.
    protected abstract void botAction();

    // Shutdown condition.
    protected abstract boolean shutdownCondition();

    // Creates message to send to RabbitMQ.
    protected abstract JSONObject createMessage();

    // Executes bot action and sends message to RabbitMQ.
    public void executeBotActionAndSendMessage() {
        botAction();
        try{
            this.messageSender.sendMessageToExchange(
                    this.botTypeConfiguration.getPublishConfiguration().getKey(),
                    createMessage().toString());
        } catch (Exception e) {
            ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }

    // Checks connection to RabbitMQ broker.
    public void checkBrokerConnection() {
        try{
            this.messageSender.checkRabbitMQConnection();
        } catch (Exception e) {
            ExceptionUtils.throwRuntimeErrorException("Error checking connection to RabbitMQ", e);
        }
    }

    public void asyncShutdownConnection(){
        String botId = System.getenv("BOTICA_BOT_ID");
        String queueName = botId + ".shutdown.queue";

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try{
                String message = new String(delivery.getBody(), "UTF-8");
                logger.info("Received message: " + message);
                shutdownAction();
            }  catch (Exception e) {
                ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
            }
        };
        messageSender.prepareShutdownConnection(queueName, deliverCallback);
    }

    public void shutdownAction() {
        Boolean shutdownCond = shutdownCondition();

        while(!shutdownCond){
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                shutdownCond = shutdownCondition();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (shutdownCond){
            try{
                this.messageSender.sendMessageToExchange("shutdownManager", "ready " + System.getenv("BOTICA_BOT_ID")); // TODO: REVIEW ROUTING KEY
                this.messageSender.close();
                logger.info("Shutdown action completed");
            } catch (Exception e) {
                ExceptionUtils.handleException(logger, "Error sending message to RabbitMQ", e);
            }
        }
    }
}
