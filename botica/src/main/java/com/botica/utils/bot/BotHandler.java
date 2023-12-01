package com.botica.utils.bot;

import java.io.FileReader;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
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
            Class<?> launcherClass = Class.forName(getGroupId() + ".launchers." + launcherName);
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

    private static String getGroupId(){
        try{
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader("pom.xml"));
            return model.getGroupId();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null; //TODO: Review
    }
}
