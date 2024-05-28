package es.us.isa.botica.runners;

import es.us.isa.botica.launchers.AbstractLauncher;
import es.us.isa.botica.utils.bot.BotRabbitConfig;
import es.us.isa.botica.utils.logging.ExceptionUtils;
import java.util.ArrayList;
import java.util.List;
import javax.management.RuntimeErrorException;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties files and connects the bot to RabbitMQ.
 */
@Getter
public class BOTICALoader extends AbstractLoader {

    private static final Logger logger = LogManager.getLogger(BOTICALoader.class);

    String botType;                 // The type of bot.
    String autonomyType;            // The autonomy associated with the bot.
    String order;                   // The order to be sent in the message in case of a reactive bot.
    Integer initialDelay;           // The initial delay for the scheduler.
    Integer period;                 // The period for the scheduler.
    String keyToPublish;            // The binding key for publishing messages.
    String orderToPublish;          // The order to be sent in the message.
    List<String> bindings;          // The list of bindings for the RabbitMQ queue.
    boolean queueByBot;             // Whether create a queue by bot.

    public BOTICALoader() {
		readProperties();
	}
    
    @Override
    protected void readProperties() {
        logger.info("Loading configuration parameter values");

        botType = readProperty("BOTICA_BOT_TYPE");
        logger.info("Bot type: {}", botType);

        autonomyType = readProperty("BOTICA_BOT_AUTONOMY_TYPE");
        logger.info("Autonomy: {}", autonomyType);

        if (autonomyType.equals("proactive")) {
            initialDelay = Integer.parseInt(readProperty("BOTICA_BOT_AUTONOMY_INITIAL_DELAY"));
            logger.info("Initial delay: {}", initialDelay);
            period = Integer.parseInt(readProperty("BOTICA_BOT_AUTONOMY_PERIOD"));
            logger.info("Period: {}", period);
        } else if (autonomyType.equals("reactive")){
            order = readProperty("BOTICA_BOT_AUTONOMY_ORDER");
            logger.info("Order: {}", order);
        }

        keyToPublish = readProperty("BOTICA_BOT_PUBLISH_KEY");
        logger.info("Key to publish: {}", keyToPublish);

        orderToPublish = readProperty("BOTICA_BOT_PUBLISH_ORDER");
        logger.info("Order to publish: {}", orderToPublish);

        // Read the bindings from the .properties file
        // The list of bindings is a comma-separated list of strings
        String bindingsString = readProperty("BOTICA_BOT_SUBSCRIBE_KEYS");
        logger.info("Bindings: {}", bindingsString);
        
        // Convert the string into a list of strings
        bindings = new ArrayList<>();
        if (bindingsString != null) {
            String[] bindingsArray = bindingsString.split(",");
            for (String binding : bindingsArray) {
                bindings.add(binding.trim());
            }
        }
    }

    /**
     * Connects the bot to RabbitMQ.
     */
    public void connectBotToRabbit(AbstractLauncher launcher) {
        
        String botId = System.getenv("BOTICA_BOT_ID");

        BotRabbitConfig botRabbitConfig = new BotRabbitConfig(botType, keyToPublish, orderToPublish);
        try{
            if (queueByBot) {
                String bindingKey = botType + "." + botId;
                List<String> bindingKeys = new ArrayList<>();
                bindingKeys.add(bindingKey);
                launcher.launchBot(botRabbitConfig, botId, bindingKeys, true, autonomyType, autonomyType.equals("proactive") ? null : order);
            } else {
                launcher.launchBot(botRabbitConfig, botType, bindings, false, autonomyType, autonomyType.equals("proactive") ? null : order);
            }
        } catch (RuntimeErrorException e) {
            ExceptionUtils.throwRuntimeErrorException("Error when starting and connecting the bot to RabbitMQ: " + botId, e);
        }
    }
}