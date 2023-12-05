package com.template.main;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.botica.launchers.AbstractLauncher;
import com.botica.runners.BOTICALoader;

public class LaunchBot {

    private static String botPropertiesFilePath = System.getenv("BOT_PROPERTY_FILE_PATH");  // The path to the bot's properties file.
    private static String launchersPackage = "com.template.launchers";                           // The package where the launchers are located.

    public static void main(String[] args) {

        if (args.length == 1){
            botPropertiesFilePath = args[0];
        }

        BOTICALoader loader = new BOTICALoader(botPropertiesFilePath, true);

        String botType = loader.getBotType();
        String keyToPublish = loader.getKeyToPublish();
        String orderToPublish = loader.getOrderToPublish();
        Properties botProperties = loader.getBotProperties();
        AbstractLauncher launcher = handleLauncherType(botType, keyToPublish, orderToPublish, botProperties);

        if (launcher == null){
            throw new NullPointerException("Bot launcher does not exist");
        }

        launcher.setLauncherPackage(launchersPackage);

        try {
            String autonomyType = loader.getAutonomyType();
            if (autonomyType.equals("reactive")){
                loader.connectBotToRabbit(launcher);
            }else if (autonomyType.equals("proactive")) {
                launcher.checkBrokerConnection();
                Integer initialDelay = loader.getInitialDelay();
                Integer period = loader.getPeriod();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                Runnable runnable = () -> loader.connectBotToRabbit(launcher);
                scheduler.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.SECONDS);
            }
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
    private static AbstractLauncher handleLauncherType(String botType, String keyToPublish, String orderToPublish, Properties botProperties) {

        try{
            String launcherName = botType + "Launcher";
            Class<?> launcherClass = Class.forName(launchersPackage + "." + launcherName);
            return (AbstractLauncher) launcherClass.getConstructor(String.class, String.class, Properties.class).newInstance(keyToPublish, orderToPublish, botProperties);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
