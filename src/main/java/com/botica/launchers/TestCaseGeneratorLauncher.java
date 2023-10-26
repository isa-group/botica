package com.botica.launchers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.botica.generators.TestCaseGenerator;
import com.botica.RabbitMQManager;

import es.us.isa.restest.generators.*;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.PropertyManager;
import es.us.isa.restest.util.RESTestException;
import es.us.isa.restest.writers.restassured.RESTAssuredWriter;

import static es.us.isa.restest.util.FileManager.createDir;

/**
 * The TestCaseGeneratorLauncher class serves as a utility for launching test
 * case generation processes and interacting with RabbitMQ. It connects to
 * RabbitMQ, receives and sends messages, and generates test cases based on the
 * specified generator type.
 */
public class TestCaseGeneratorLauncher {

    public static final Logger logger = Logger.getLogger(TestCaseGeneratorLauncher.class.getName());
    private static final String BOT_ID_JSON_KEY = "botId";

    private RabbitMQManager messageSender = new RabbitMQManager();

    /**
     * Launches test case generator based on bot data provided, and sends and 
     * receives messages through RabbitMQ.
     *
     * @param botData      The JSON object containing bot data.
     * @param order        The order that identifies the message sent.
     * @param keyToPublish The binding key for publishing messages to RabbitMQ.
     */
    public void launchTestCases(JSONObject botData, String order, String keyToPublish) {
        
        String queueName = botData.getString(BOT_ID_JSON_KEY);
        
        try {
            connectToRabbitMQ(queueName);
            messageSender.receiveMessage(queueName, botData, "testCaseGenerator", order, keyToPublish);
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void connectToRabbitMQ(String queueName) throws IOException, TimeoutException {
        List<Boolean> queueOptions = Arrays.asList(true, false, true);
        messageSender.connect(queueName, "testCaseGenerator." + queueName, queueOptions); //TODO: Change the binding key depending on the bot
        logger.log(Level.INFO, "{0} connected to RabbitMQ", new Object[]{queueName});
    }

    /**
     * Generates test cases based on the specified generator type.
     *
     * @param propertyFilePath The path to the property file for test case
     *                         generator.
     * @param botId            The test case generator identifier.
     * @param keyToPublish     The binding key for publishing messages to RabbitMQ.
     */
    public static void generateTestCases(String propertyFilePath, String botId, String keyToPublish) {
        try {
            RESTestLoader loader = new RESTestLoader(propertyFilePath);

            String generatorType = PropertyManager.readProperty(propertyFilePath, "generator");

            AbstractTestCaseGenerator generator = getGenerator(loader, generatorType);

            TestCaseGenerator testGenerator = new TestCaseGenerator(generator, loader, botId, generatorType, keyToPublish);

            Collection<TestCase> testCases = testGenerator.generate();

            // Create target directory for test cases if it does not exist
            createDir(loader.getTargetDirJava());

            // Write (RestAssured) test cases
            RESTAssuredWriter writer = (RESTAssuredWriter) loader.createWriter();
            writer.write(testCases);

            if (logger.isLoggable(Level.INFO)) {
                String message = String.format("%d test cases generated and written to %s", testCases.size(), loader.getTargetDirJava());
                logger.info(message);
            }
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
