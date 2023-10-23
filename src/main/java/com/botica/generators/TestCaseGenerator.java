package com.botica.generators;

import java.util.Collection;

import org.json.JSONObject;

import com.botica.RabbitMQManager;
import com.botica.interfaces.TestCaseGeneratorInterface;

import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

public class TestCaseGenerator implements TestCaseGeneratorInterface {

    private AbstractTestCaseGenerator testCaseGenerator;
    private RESTestLoader loader;
    private String botId;
    private String generatorType;
    private RabbitMQManager messageSender = new RabbitMQManager("");

    public TestCaseGenerator(AbstractTestCaseGenerator testCaseGenerator, RESTestLoader loader, String botId, String generatorType) {
        this.testCaseGenerator = testCaseGenerator;
        this.loader = loader;
        this.botId = botId;
        this.generatorType = generatorType;
    }

    @Override
    public Collection<TestCase> generate() throws RESTestException {
        Collection<TestCase> testCases = testCaseGenerator.generate();

        String message = generateJSONMessage();

        try{
            messageSender.connect();
            messageSender.sendMessageToExchange("testCasesGenerated", message);
            System.out.println("Message sent to RabbitMQ");
            System.out.println(message);
            messageSender.close();
        } catch (Exception e) {
            System.err.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }

        return testCases;
    }

    private String generateJSONMessage() {

        JSONObject message = new JSONObject();
        message.put("botId", this.botId);
        message.put("generatorType", generatorType);
        message.put("faultyRatio", testCaseGenerator.getFaultyRatio());
        message.put("nTotalFaulty", testCaseGenerator.getnFaulty());
        message.put("nTotalNominal", testCaseGenerator.getnNominal());
        message.put("maxTriesPerTestCase", testCaseGenerator.getMaxTriesPerTestCase());
        message.put("targetDirJava", loader.getTargetDirJava());
        message.put("getAllureReportsPath", loader.getAllureReportsPath());
        message.put("getExperimentName", loader.getExperimentName());

        return message.toString();
    }
    
}