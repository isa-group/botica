package com.botica.generators;

import java.util.Collection;

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

        Float faultyRatio = testCaseGenerator.getFaultyRatio();
        int nTotalFaulty = testCaseGenerator.getnFaulty();
        int nTotalNominal = testCaseGenerator.getnNominal();
        int maxTriesPerTestCase = testCaseGenerator.getMaxTriesPerTestCase();
        String targetDirJava = loader.getTargetDirJava();
        String allureReportsPath = loader.getAllureReportsPath();
        String experimentName = loader.getExperimentName();

        String message = "{\"botId\":" + this.botId + ",\"generatorType\":" + generatorType + ",\"faultyRatio\":" + faultyRatio + ",\"nTotalFaulty\":" + nTotalFaulty + ",\"nTotalNominal\":" + nTotalNominal + ",\"maxTriesPerTestCase\":" + maxTriesPerTestCase + ",\"targetDirJava\":\"" + targetDirJava + "\",\"getAllureReportsPath\":\"" + allureReportsPath + "\",\"getExperimentName\":\"" + experimentName + "\"}";

        try{
            messageSender.connect();
            messageSender.sendMessageToExchange("testCasesGenerated", message);
            System.out.println("Message sent to RabbitMQ");
            System.out.println(message);
            messageSender.close();
        } catch (Exception e) {
            System.out.println("Error sending message to RabbitMQ");
            e.printStackTrace();
        }

        return testCases;
    }
    
}