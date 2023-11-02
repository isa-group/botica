package com.botica.generators;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.botica.interfaces.TestCaseGeneratorInterface;
import com.botica.utils.BotConfig;
import com.botica.utils.RabbitCommunicator;

import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;
import es.us.isa.restest.writers.restassured.RESTAssuredWriter;

import static es.us.isa.restest.util.FileManager.createDir;

/**
 * This class is responsible for generating test cases and
 * sending messages through RabbitMQ with relevant information.
 */
public class TestCaseGenerator implements TestCaseGeneratorInterface {

    private AbstractTestCaseGenerator absractTestCaseGenerator;
    private RESTestLoader loader;
    private String botId;
    private String generatorType;
    private String keyToPublish;
    private String orderToPublish;
    private String propertyFilePath;
    private RabbitCommunicator rabbitCommunicator;

    private static final Logger logger = LogManager.getLogger(TestCaseGenerator.class);

    /**
     * Constructor for the TestCaseGenerator class.
     * 
     * @param absractTestCaseGenerator The AbstractTestCaseGenerator class used to generate test cases.
     * @param loader                   The RESTestLoader class used to load the properties files.
     * @param botConfig                The BotConfig class containing the bot configuration.
     * @param generatorType            The test case generator type ('FT', 'RT', 'CBT' or 'ART').
     * @param propertyFilePath         The path to the properties file.
     */
    public TestCaseGenerator(AbstractTestCaseGenerator absractTestCaseGenerator, RESTestLoader loader, BotConfig botConfig, String generatorType, String propertyFilePath) {
        this.absractTestCaseGenerator = absractTestCaseGenerator;
        this.loader = loader;
        this.botId = botConfig.getBotId();
        this.generatorType = generatorType;
        this.keyToPublish = botConfig.getKeyToPublish();
        this.orderToPublish = botConfig.getOrderToPublish();
        this.propertyFilePath = propertyFilePath;
        this.rabbitCommunicator = new RabbitCommunicator(this.keyToPublish, logger);
    }

    /**
     * Generates test cases and sends a completion message through RabbitMQ.
     */
    @Override
    public Collection<TestCase> generate() throws RESTestException {
        Collection<TestCase> testCases = absractTestCaseGenerator.generate();

        String testCasesPath = loader.getTargetDirJava() + "/t.tmp";
        try (FileOutputStream fos = new FileOutputStream(testCasesPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(testCases);
        } catch (IOException e) {
            logger.error("Error writing test cases to file: {}", e.getMessage());
        }

        // TODO: Extract this to the TestCaseGeneratorLauncher class
        // Create target directory for test cases if it does not exist
        createDir(loader.getTargetDirJava());

        // Write (RestAssured) test cases
        RESTAssuredWriter writer = (RESTAssuredWriter) loader.createWriter();
        writer.write(testCases);
        //

        String message = generateJSONMessage(testCasesPath);
        rabbitCommunicator.sendMessage(message);
        
        return testCases;
    }

    /**
     * Generates a JSON message containing information about the test case
     * generation and RabbitMQ communication.
     * 
     * @return The JSON message as a string.
     */
    private String generateJSONMessage(String testCasesPath) {

        JSONObject message = new JSONObject();
        message.put("order", orderToPublish);
        message.put("botId", this.botId);
        message.put("generatorType", generatorType);
        message.put("faultyRatio", absractTestCaseGenerator.getFaultyRatio());
        message.put("nTotalFaulty", absractTestCaseGenerator.getnFaulty());
        message.put("nTotalNominal", absractTestCaseGenerator.getnNominal());
        message.put("maxTriesPerTestCase", absractTestCaseGenerator.getMaxTriesPerTestCase());
        message.put("targetDirJava", loader.getTargetDirJava());
        message.put("allureReportsPath", loader.getAllureReportsPath());
        message.put("experimentName", loader.getExperimentName());
        message.put("propertyFilePath", this.propertyFilePath);
        message.put("testCasesPath", testCasesPath);

        return message.toString();
    }
    
}