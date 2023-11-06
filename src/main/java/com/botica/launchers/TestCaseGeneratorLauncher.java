package com.botica.launchers;

import java.util.logging.Level;

import org.json.JSONObject;

import com.botica.bots.TestCaseGenerator;
import com.botica.utils.BotConfig;
import com.botica.utils.RESTestUtil;

import es.us.isa.restest.generators.*;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.util.RESTestException;

/**
 * The TestCaseGeneratorLauncher class serves as a utility for launching test
 * case generation processes and interacting with RabbitMQ. It connects to
 * RabbitMQ, receives and sends messages, and generates test cases based on the
 * specified generator type.
 */
public class TestCaseGeneratorLauncher extends BaseLauncher {

    private static final String BOT_ID_JSON_KEY = "botId";
    private static final String BOT_TYPE = "testCaseGenerator";

    /**
     * Launches test case generator based on bot data provided, and sends and 
     * receives messages through RabbitMQ.
     *
     * @param botData           The JSON object containing bot data.
     * @param order             The order that identifies the message sent.
     * @param keyToPublish      The binding key for publishing messages to RabbitMQ.
     * @param orderToPublish    The order to send in the message.
     */
    public void launchTestGenerator(JSONObject botData, String order, String keyToPublish, String orderToPublish) {
        
        BotConfig botConfig = new BotConfig(null, order, keyToPublish, orderToPublish, BOT_TYPE);
        String queueName = botData.getString(BOT_ID_JSON_KEY);
        String bindingKey = "testCaseGenerator." + queueName;
        launchBot(botData, botConfig, queueName, bindingKey, true);
    }

    /**
     * Generates test cases based on the specified generator type.
     *
     * @param propertyFilePath The path to the property file.
     * @param botId            The test case generator identifier.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     */
    public static void generateTestCases(String propertyFilePath, String botId, String keyToPublish, String orderToPublish) {
        try {
            //PROBLEM HERE
            RESTestLoader loader = new RESTestLoader(propertyFilePath);

            String generatorType = RESTestUtil.readProperty(propertyFilePath, "generator");

            AbstractTestCaseGenerator generator = getGenerator(loader, generatorType);

            BotConfig botConfig = new BotConfig(botId, null, keyToPublish, orderToPublish, BOT_TYPE);

            TestCaseGenerator testGenerator = new TestCaseGenerator(generator, loader, botConfig, generatorType, propertyFilePath);

            testGenerator.executeBotActionAndSendMessage();
        
        }catch (RESTestException e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static AbstractTestCaseGenerator getGenerator(RESTestLoader loader, String generatorType) throws RESTestException {
        AbstractTestCaseGenerator generator = null;

        switch (generatorType){
            case "FT":
                generator = (FuzzingTestCaseGenerator) loader.createGenerator();
                break;
            case "RT":
                generator = (RandomTestCaseGenerator) loader.createGenerator();
                break;
            case "CBT":
                generator = (ConstraintBasedTestCaseGenerator) loader.createGenerator();
                break;
            case "ART":
                generator = (ARTestCaseGenerator) loader.createGenerator();
                break;
            default:
                throw new RESTestException("Property 'generator' must be one of 'FT', 'RT', 'CBT' or 'ART'");
        }
        return generator;
    }

}
