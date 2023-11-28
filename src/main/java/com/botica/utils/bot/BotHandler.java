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

    /**
     * Handles a bot message.
     *
     * @param botRabbitConfig The bot's RabbitMQ configuration.
     * @param botProperties   The bot's properties.
     * @param messageData     The message data.
     */
    public static void handleBotMessage(BotRabbitConfig botRabbitConfig, Properties botProperties, JSONObject messageData) {
        
        String botType = botRabbitConfig.getBotType();
        String keyToPublish = botRabbitConfig.getKeyToPublish();
        String orderToPublish = botRabbitConfig.getOrderToPublish();

        try{
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName("com.botica.launchers." + launcherName);
            AbstractLauncher launcher = (AbstractLauncher) launcherClass.getConstructor(String.class, String.class, Properties.class).newInstance(keyToPublish, orderToPublish, botProperties);
            launcher.setMessageData(messageData);
            launcher.executeBotActionAndSendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Handles a bot data to create a specific launcher.
     *
     * @param botType           The bot's type.
     * @param keyToPublish      The key to publish.
     * @param orderToPublish    The order to publish.
     * @param botProperties     The bot's properties.
     */
    public static AbstractLauncher handleLauncherType(String botType, String keyToPublish, String orderToPublish, Properties botProperties) {

        try{
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName("com.botica.launchers." + launcherName);
            return (AbstractLauncher) launcherClass.getConstructor(String.class, String.class, Properties.class).newInstance(keyToPublish, orderToPublish, botProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
