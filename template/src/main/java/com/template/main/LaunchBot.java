package com.template.main;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.botica.launchers.AbstractLauncher;
import com.botica.runners.BOTICALoader;
import com.botica.utils.bot.BotHandler;

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
        AbstractLauncher launcher = BotHandler.handleLauncherType(botType, keyToPublish, orderToPublish, botProperties, launchersPackage);

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
}
