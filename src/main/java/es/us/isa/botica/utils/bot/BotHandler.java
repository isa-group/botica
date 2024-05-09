package es.us.isa.botica.utils.bot;

import java.util.Properties;

import es.us.isa.botica.launchers.AbstractLauncher;
import org.json.JSONObject;

/**
 * This class handles bot messages and creates bot launchers.
 */
public class BotHandler {

    private BotHandler() {
    }

    private static void handleBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties, String launchersPackage, JSONObject messageData) {
        String botType = botRabbitConfig.getBotType();
        String keyToPublish = botRabbitConfig.getKeyToPublish();
        String orderToPublish = botRabbitConfig.getOrderToPublish();

        try {
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName(launchersPackage + "." + launcherName);
            AbstractLauncher launcher = (AbstractLauncher) launcherClass
                    .getConstructor(String.class, String.class, Properties.class)
                    .newInstance(keyToPublish, orderToPublish, botProperties);

            if (messageData != null) {
                launcher.setMessageData(messageData);
            }

            launcher.executeBotActionAndSendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a bot reactive action.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botProperties   The bot's properties.
     * @param messageData     The message data.
     */
    public static void handleReactiveBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties, String launchersPackage, JSONObject messageData) {
        handleBotAction(botRabbitConfig, botProperties, launchersPackage, messageData);
    }

    /**
     * Handles a bot proactive action.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botProperties   The bot's properties.
     */
    public static void handleProactiveBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties, String launchersPackage) {
        handleBotAction(botRabbitConfig, botProperties, launchersPackage, null);
    }

    /**
     * Handles a bot data to create a specific launcher.
     *
     * @param botType        The bot's type.
     * @param keyToPublish   The key to publish.
     * @param orderToPublish The order to publish.
     * @param botProperties  The bot's properties.
     */
    public static AbstractLauncher handleLauncherType(String botType, String keyToPublish, String orderToPublish, Properties botProperties, String launchersPackage) {

        try {
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName(launchersPackage + "." + launcherName);
            return (AbstractLauncher) launcherClass.getConstructor(String.class, String.class, Properties.class)
                    .newInstance(keyToPublish, orderToPublish, botProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
