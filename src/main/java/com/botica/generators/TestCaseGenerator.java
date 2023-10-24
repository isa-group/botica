package com.botica.generators;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.botica.RabbitMQManager;
import com.botica.interfaces.TestCaseGeneratorInterface;
import com.botica.utils.Utils;

import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

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
    private RabbitMQManager messageSender = new RabbitMQManager();

    private static final Logger logger = LogManager.getLogger(TestCaseGenerator.class);

    /**
     * Constructor for the TestCaseGenerator class.
     * 
     * @param absractTestCaseGenerator The AbstractTestCaseGenerator class used to generate test cases.
     * @param loader                   The RESTestLoader class used to load the properties files.
     * @param botId                    The test case generator id.
     * @param generatorType            The test case generator type ('FT', 'RT', 'CBT' or 'ART').
     * @param keyToPublish             The binding key to publish a message to the RabbitMQ broker.
     */
    public TestCaseGenerator(AbstractTestCaseGenerator absractTestCaseGenerator, RESTestLoader loader, String botId, String generatorType, String keyToPublish) {
        this.absractTestCaseGenerator = absractTestCaseGenerator;
        this.loader = loader;
        this.botId = botId;
        this.generatorType = generatorType;
        this.keyToPublish = keyToPublish;
    }

    @Override
    public Collection<TestCase> generate() throws RESTestException {
        Collection<TestCase> testCases = absractTestCaseGenerator.generate();

        String message = generateJSONMessage();
        sendMessage(message);
        
        return testCases;
    }

    /**
     * Generates a JSON message containing information about the test case
     * generation and RabbitMQ communication.
     * 
     * @return The JSON message as a string.
     */
    private String generateJSONMessage() {

        JSONObject message = new JSONObject();
        message.put("botId", this.botId);
        message.put("generatorType", generatorType);
        message.put("faultyRatio", absractTestCaseGenerator.getFaultyRatio());
        message.put("nTotalFaulty", absractTestCaseGenerator.getnFaulty());
        message.put("nTotalNominal", absractTestCaseGenerator.getnNominal());
        message.put("maxTriesPerTestCase", absractTestCaseGenerator.getMaxTriesPerTestCase());
        message.put("targetDirJava", loader.getTargetDirJava());
        message.put("getAllureReportsPath", loader.getAllureReportsPath());
        message.put("getExperimentName", loader.getExperimentName());

        return message.toString();
    }

    /**
     * Sends a message through RabbitMQ with relevant information about the test
     * case generation.
     * 
     * @param message The message to send.
     */
    private void sendMessage(String message){
        try{
            List<Boolean> queueOptions = Arrays.asList(true, false, false);
            messageSender.connect("", null, queueOptions);
            messageSender.sendMessageToExchange(keyToPublish, message);
            logger.info("Message sent to RabbitMQ: {}", message);
            messageSender.close();
        } catch (Exception e) {
            Utils.handleException(logger, "Error sending message to RabbitMQ", e);
        }
    }
    
}