package com.botica.utils.bot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.botica.utils.directory.DirectoryOperations;

public class BotDefinitionTemplate {

    private static final Logger logger = LogManager.getLogger(BotDefinitionTemplate.class);
    private static final List<String> DEFINED_TEMPLATES = List.of("RESTest");

    private BotDefinitionTemplate() {
    }

    public static void generateTemplate(String template, String outpoutPath) {

        if (template.equals("RESTest")) {
            generateRESTestTemplate(outpoutPath);
        } else {
            logger.error("The template {} is not defined. Please, use one of the following templates: {}.", template, DEFINED_TEMPLATES);
        }

    }

    private static void generateRESTestTemplate(String outpoutPath) {
        
        Path filePath = Path.of(outpoutPath);
        DirectoryOperations.createDir(filePath);

        String content = getRESTestTemplate();

        try {
            Files.writeString(filePath, content, StandardOpenOption.CREATE);
            logger.info("RESTest generated successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRESTestTemplate(){
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
