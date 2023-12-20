package com.template.main;

import com.botica.runners.ConfigurationLoader;
import com.botica.utils.configuration.CreateConfiguration;

public class ConfigurationSetup {

    private static String configurationPropertiesFilePath = "src/main/resources/BOTICAConfig/configuration-setup.properties";

    public static void main(String[] args) {

        if (args.length == 1) {
            configurationPropertiesFilePath = args[0];
        }

        ConfigurationLoader configurationLoader = new ConfigurationLoader(configurationPropertiesFilePath, true);

        String botsDefinitionPath = configurationLoader.getBotsDefinitionPath();
        String botsPropertiesPath = configurationLoader.getBotsPropertiesPath();
        String rabbitMQExchange = configurationLoader.getRabbitMQExchange();
        String rabbitMQConfigurationPath = configurationLoader.getRabbitMQConfigurationPath();
        String rabbitMQConnectionPath = configurationLoader.getRabbitMQConnectionPath();
        String rabbitMQUsername = configurationLoader.getRabbitMQUsername();
        String rabbitMQPassword = configurationLoader.getRabbitMQPassword();
        String rabbitMQHost = configurationLoader.getRabbitMQHost();
        Integer rabbitMQPort = configurationLoader.getRabbitMQPort();
        String dockerComposePath = configurationLoader.getDockerComposePath();
        String dummyDockerfilePath = configurationLoader.getDummyDockerfilePath();
        String boticaDockerfilePath = configurationLoader.getBoticaDockerfilePath();
        String jarFileName = configurationLoader.getJarFileName();
        String initVolumeScriptPath = configurationLoader.getInitVolumeScriptPath();
        String mainLaunchScript = configurationLoader.getMainLaunchScript();
        String boticaImageName = configurationLoader.getBoticaImageName();

        CreateConfiguration.createBotPropertiesFiles(botsDefinitionPath, botsPropertiesPath);
        CreateConfiguration.createRabbitMQConfigFile(rabbitMQExchange, rabbitMQConfigurationPath);
        CreateConfiguration.createRabbitMQConnectionFile(rabbitMQConnectionPath, rabbitMQUsername, rabbitMQPassword, rabbitMQHost, rabbitMQPort, rabbitMQExchange);
        CreateConfiguration.createDockerCompose(dockerComposePath);
        CreateConfiguration.createDummyDockerfile(dummyDockerfilePath);
        CreateConfiguration.createBoticaDockerfile(boticaDockerfilePath, jarFileName);
        CreateConfiguration.createInitVolumeScript(initVolumeScriptPath);
        CreateConfiguration.createMainScript(mainLaunchScript, dummyDockerfilePath, initVolumeScriptPath, dockerComposePath, boticaDockerfilePath, boticaImageName);
    }
}
