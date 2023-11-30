package com.botica.utils.bot;

import java.util.Properties;

import org.json.JSONObject;

import com.botica.launchers.AbstractLauncher;

/**
 * This class handles bot messages and creates bot launchers.
 */
public class BotHandler {

    private BotHandler() {
    }

    private static void handleBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties, JSONObject messageData) {
        String botType = botRabbitConfig.getBotType();
        String keyToPublish = botRabbitConfig.getKeyToPublish();
        String orderToPublish = botRabbitConfig.getOrderToPublish();

        try {
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName("com.botica.launchers." + launcherName);
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
    public static void handleReactiveBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties, JSONObject messageData) {
        handleBotAction(botRabbitConfig, botProperties, messageData);
    }

    /**
     * Handles a bot proactive action.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botProperties   The bot's properties.
     */
    public static void handleProactiveBotAction(BotRabbitConfig botRabbitConfig, Properties botProperties) {
        handleBotAction(botRabbitConfig, botProperties, null);
    }
}
