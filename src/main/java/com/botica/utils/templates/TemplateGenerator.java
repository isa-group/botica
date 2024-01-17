package com.botica.utils.templates;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.directory.DirectoryOperations;

public class TemplateGenerator {
    private static final Logger logger = LogManager.getLogger(TemplateGenerator.class);
    private static final List<String> DEFINED_TEMPLATES = List.of("RESTest");
    private static final List<String> DEFINED_TEMPLATE_TYPES = List.of("bots-definition", "properties");

    private TemplateGenerator() {
    }

    public static void generateTemplate(String templateTypeString, String templateName, String outpoutPath) {

        TemplateType templateType = null;

        if (templateTypeString != null && templateTypeString.equals("bots-definition")) {
            templateType = TemplateType.BOTS_DEFINITION;
        } else if (templateTypeString != null && templateTypeString.equals("properties")) {
            templateType = TemplateType.PROPERTIES;
        } else if (templateTypeString == null) {
            logger.error("You need to specify the template type. Please, use one of the following template types: {}.", DEFINED_TEMPLATE_TYPES);
            return;
        } else {
            logger.error("The template type {} is not defined. Please, use one of the following template types: {}.", templateTypeString, DEFINED_TEMPLATE_TYPES);
            return;
        }

        if (templateName != null && templateName.equals("RESTest")) {
            generateRESTestTemplate(templateType, outpoutPath);
        } else if (templateName == null) {
            logger.error("You need to specify the template. Please, use one of the following templates: {}.", DEFINED_TEMPLATES);
        } else {
            logger.error("The template {} is not defined. Please, use one of the following templates: {}.", templateName, DEFINED_TEMPLATES);
        }

    }

    private static void generateRESTestTemplate(TemplateType templateType, String outpoutPath) {

        try {
            if (templateType.equals(TemplateType.BOTS_DEFINITION)) {
                String botsDefinitionTemplate = getRESTestBotsDefinitionTemplate();
                Path botsDefinitionPath = Path.of(outpoutPath, "bots-definition.json");
                DirectoryOperations.createDir(botsDefinitionPath);
                Files.writeString(botsDefinitionPath, botsDefinitionTemplate, StandardOpenOption.CREATE);
                logger.info("RESTest bots definition template generated successfully!");
            } else if (templateType.equals(TemplateType.PROPERTIES)) {
                String configurationPropertiesTemplate = getRESTestConfigurationPropertiesTemplate();
                Path configurationPropertiesPath = Path.of(outpoutPath, "configuration-setup.properties");
                DirectoryOperations.createDir(configurationPropertiesPath);
                Files.writeString(configurationPropertiesPath, configurationPropertiesTemplate, StandardOpenOption.CREATE);
                logger.info("RESTest configuration properties template generated successfully!");

                String collectorPropertiesPropertiesTemplate = getRESTestCollectorPropertiesTemplate();
                Path collectorPropertiesPath = Path.of(outpoutPath, "collector.properties");
                Files.writeString(collectorPropertiesPath, collectorPropertiesPropertiesTemplate, StandardOpenOption.CREATE);
                logger.info("RESTest collector properties template generated successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRESTestConfigurationPropertiesTemplate(){
        return "bots.definition.path=src/main/java/com/restestica/bots/bots-definition.json\n" +
                "bots.properties.path=src/main/resources/ConfigurationFiles/\n" +
                "\n" +
                "rabbitmq.username=admin\n" +
                "rabbitmq.password=testing1\n" +
                "rabbitmq.host=rabbitmq\n" +
                "rabbitmq.amqp.port=5671\n" +
                "rabbitmq.ui.port=15671\n" +
                "rabbitmq.exchange=restest_exchange\n" +
                "\n" +
                "rabbitmq.configuration.path=rabbitmq/definitions.json\n" +
                "rabbitmq.ports.configuration.path=rabbitmq/rabbitmq.conf\n" +
                "rabbitmq.connection.path=rabbitmq/server-config.json\n" +
                "\n" +
                "docker.compose.path=docker-compose.yml\n" +
                "\n" +
                "dummy.dockerfile.path=docker/Dockerfile\n" +
                "botica.dockerfile.path=Dockerfile\n" +
                "jar.file.name=restestica\n" +
                "\n" +
                "init.volume.script.path=docker/init-volume.sh\n" +
                "botica.image.name=bot-ica\n" +
                "main.unix.launch.script=launch_botica.sh\n" +
                "main.windows.launch.script=launch_botica.bat\n";
    }

    private static String getRESTestCollectorPropertiesTemplate(){
        return "paths.to.observe=/target/allure-results,/target/coverage-data,/target/test-data,/src/main/resources/Examples/Ex4_CBTGeneration/allure_report,/src/main/resources/Examples/Ex5_CBTGeneration/allure_report\n" +
                "\n" +
                "local-path.to.copy=tmp/collector\n" +
                "container.name=collector\n" +
                "image.name=dummy\n" +
                "windows.docker.host=tcp://127.0.0.1:2375\n" +
                "\n" +
                "initial-delay.to.collect=10\n" +
                "period.to.collect=60";
    }

    private static String getRESTestBotsDefinitionTemplate(){
        return "[\n" +
                "    {\n" +
                "        \"botType\": \"TestCaseGenerator\",\n" +
                "        \"dockerImage\": \"bot-ica\",\n" +
                "        \"autonomy\": {\n" +
                "            \"type\": \"proactive\",\n" +
                "            \"initialDelay\": 60,\n" +
                "            \"period\": 60\n" +
                "        },\n" +
                "        \"keyToPublish\": \"testCasesGenerated\",\n" +
                "        \"orderToPublish\": \"executeTestCases\",\n" +
                "        \"rabbitOptions\": {\n" +
                "            \"mainQueue\": \"testCaseGenerator\",\n" +
                "            \"bindings\": [\n" +
                "                \"testOraclesGenerated\"\n" +
                "            ],\n" +
                "            \"queueByBot\": true\n" +
                "        },\n" +
                "        \"requiredPaths\": [\n" +
                "            \"./src/main/resources/Examples\",\n" +
                "            \"./src/main/resources/config.properties\",\n" +
                "            \"./src/main/resources/fuzzing-dictionary.json\",\n" +
                "            \"./target\"\n" +
                "        ],\n" +
                "        \"bots\": [\n" +
                "            {\n" +
                "                \"botId\": \"gen_4\",\n" +
                "                \"propertyFilePath\": \"src/main/resources/Examples/Ex4_CBTGeneration/user_config.properties\",\n" +
                "                \"isPersistent\": true,\n" +
                "                \"autonomy\": {\n" +
                "                    \"initialDelay\": 120,\n" +
                "                    \"period\": 120\n" +
                "                }\n" +
                "            },\n" +
                "            {\n" +
                "                \"botId\": \"gen_5\",\n" +
                "                \"propertyFilePath\": \"src/main/resources/Examples/Ex5_CBTGeneration/user_config.properties\",\n" +
                "                \"isPersistent\": true\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"botType\": \"TestCaseExecutor\",\n" +
                "        \"dockerImage\": \"bot-ica\",\n" +
                "        \"autonomy\": {\n" +
                "            \"type\": \"reactive\",\n" +
                "            \"order\": \"executeTestCases\"\n" +
                "        },\n" +
                "        \"keyToPublish\": \"testCasesExecuted\",\n" +
                "        \"orderToPublish\": \"generateTestReport\",\n" +
                "        \"rabbitOptions\": {\n" +
                "            \"mainQueue\": \"testCaseExecutor\",\n" +
                "            \"bindings\": [\n" +
                "                \"testCasesGenerated\"\n" +
                "            ],\n" +
                "            \"queueByBot\": false\n" +
                "        },\n" +
                "        \"requiredPaths\": [\n" +
                "            \"./target\"\n" +
                "        ],\n" +
                "        \"bots\": [\n" +
                "            {\n" +
                "                \"botId\": \"ex_1\",\n" +
                "                \"isPersistent\": true\n" +
                "            },\n" +
                "            {\n" +
                "                \"botId\": \"ex_2\",\n" +
                "                \"isPersistent\": true\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"botType\": \"TestReporter\",\n" +
                "        \"dockerImage\": \"bot-ica\",\n" +
                "        \"autonomy\": {\n" +
                "            \"type\": \"reactive\",\n" +
                "            \"order\": \"generateTestReport\"\n" +
                "        },\n" +
                "        \"keyToPublish\": \"testReportGenerated\",\n" +
                "        \"orderToPublish\": \"test\",\n" +
                "        \"rabbitOptions\": {\n" +
                "            \"mainQueue\": \"testReporter\",\n" +
                "            \"bindings\": [\n" +
                "                \"testCasesExecuted\"\n" +
                "            ],\n" +
                "            \"queueByBot\": false\n" +
                "        },\n" +
                "        \"requiredPaths\": [\n" +
                "            \"./allure\",\n" +
                "            \"./src/main/resources/allure-categories.json\",\n" +
                "            \"./target\"\n" +
                "        ],\n" +
                "        \"bots\": [\n" +
                "            {\n" +
                "                \"botId\": \"re_1\",\n" +
                "                \"isPersistent\": true\n" +
                "            },\n" +
                "            {\n" +
                "                \"botId\": \"re_2\",\n" +
                "                \"isPersistent\": true\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";
    }
}
