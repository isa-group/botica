package es.us.isa.botica.utils.bot;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.launchers.AbstractLauncher;
import es.us.isa.botica.runners.BOTICALoader;
import org.json.JSONObject;

/**
 * This class handles bot messages and creates bot launchers.
 */
public class BotHandler {

    private BotHandler() {
    }

    private static void handleBotAction(BotRabbitConfig botRabbitConfig, String launchersPackage, JSONObject messageData) {
        String botType = botRabbitConfig.getBotType();

        try {
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName(launchersPackage + "." + launcherName);
            AbstractLauncher launcher = (AbstractLauncher) launcherClass
                    .getConstructor(MainConfiguration.class)
                    .newInstance(new BOTICALoader().loadConfiguration());

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
    public static void handleReactiveBotAction(BotRabbitConfig botRabbitConfig, String launchersPackage, JSONObject messageData) {
        handleBotAction(botRabbitConfig, launchersPackage, messageData);
    }

    /**
     * Handles a bot proactive action.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botProperties   The bot's properties.
     */
    public static void handleProactiveBotAction(BotRabbitConfig botRabbitConfig, String launchersPackage) {
        handleBotAction(botRabbitConfig, launchersPackage, null);
    }

    /**
     * Handles a bot data to create a specific launcher.
     *
     * @param botType        The bot's type.
     */
    public static AbstractLauncher handleLauncherType(String launchersPackage, MainConfiguration mainConfiguration) {
        try {
            String launcherName = System.getenv("BOTICA_BOT_TYPE") + "Launcher";
            Class<?> launcherClass = Class.forName(launchersPackage + "." + launcherName);
            return (AbstractLauncher) launcherClass.getConstructor(MainConfiguration.class)
                    .newInstance(mainConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
