package com.botica.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.botica.runners.BOTICALoader;

public class LaunchBot {

    private static String botPropertiesFilePath = System.getenv("BOT_PROPERTY_FILE_PATH");  // The path to the bot's properties file.

    public static void main(String[] args) {

        if (args.length == 1){
            botPropertiesFilePath = args[0];
        }

        BOTICALoader loader = new BOTICALoader(botPropertiesFilePath, true);
        String autonomyType = loader.getAutonomyType();

        try {

            loader.connectBotToRabbit();

            if (autonomyType.equals("proactive")) {
                Integer initialDelay = loader.getInitialDelay();
                Integer period = loader.getPeriod();
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(loader::connectBotToRabbit, initialDelay, period, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
