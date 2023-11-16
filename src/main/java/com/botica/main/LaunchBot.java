package com.botica.main;

import com.botica.runners.BOTICALoader;

public class LaunchBot {

    private static String botPropertiesFilePath = System.getenv("BOT_PROPERTY_FILE_PATH");

    public static void main(String[] args) {

        if (args.length == 1){
            botPropertiesFilePath = args[0];
        }

        BOTICALoader loader = new BOTICALoader(botPropertiesFilePath, true);
        
        try {
            loader.connectBotToRabbit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
