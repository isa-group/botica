package com.botica.launchers;

import static es.us.isa.restest.util.FileManager.createDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.json.JSONObject;

import com.botica.utils.property.PropertyReader;

import es.us.isa.restest.generators.*;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;
import es.us.isa.restest.writers.restassured.RESTAssuredWriter;

/**
 * The TestCaseGeneratorLauncher class serves as a utility for launching test
 * case generation processes and interacting with RabbitMQ. It connects to
 * RabbitMQ, receives and sends messages, and generates test cases based on the
 * specified generator type.
 */
public class TestCaseGeneratorLauncher extends AbstractLauncher {

    private static final String BOT_ID_JSON_KEY = "botId";

    private String propertyFilePath;
    private String botId;

    private RESTestLoader loader;
    private AbstractTestCaseGenerator absractTestCaseGenerator;
    private String generatorType;
    private String testCasesPath;

    public TestCaseGeneratorLauncher(String keyToPublish, String orderToPublish) {
        super(keyToPublish, orderToPublish);
    }

    public TestCaseGeneratorLauncher(String propertyFilePath, String botId, String keyToPublish, String orderToPublish) {
        super(keyToPublish, orderToPublish);
        this.propertyFilePath = propertyFilePath;
        this.botId = botId;
    }

    /**
     * Generates test cases based on the specified generator type.
     */
    @Override
    protected void botAction() {
        try {
            //PROBLEM HERE
            RESTestLoader botLoader = new RESTestLoader(this.propertyFilePath);
            this.loader = botLoader;

            String botGeneratorType = PropertyReader.readProperty(this.propertyFilePath, "generator");
            this.generatorType = botGeneratorType;

            AbstractTestCaseGenerator generator = getGenerator(loader, generatorType);
            this.absractTestCaseGenerator = generator;

            auxBotAction(loader, generator);
        
        }catch (RESTestException e){
            logger.error("Error launching test generator: {}", this.botId, e);
        }
    }

    @Override
    protected JSONObject createMessage() {
        JSONObject message = new JSONObject();
        message.put("order", orderToPublish);
        message.put(BOT_ID_JSON_KEY, this.botId);
        message.put("generatorType", generatorType);
        message.put("faultyRatio", absractTestCaseGenerator.getFaultyRatio());
        message.put("nTotalFaulty", absractTestCaseGenerator.getnFaulty());
        message.put("nTotalNominal", absractTestCaseGenerator.getnNominal());
        message.put("maxTriesPerTestCase", absractTestCaseGenerator.getMaxTriesPerTestCase());
        message.put("targetDirJava", loader.getTargetDirJava());
        message.put("allureReportsPath", loader.getAllureReportsPath());
        message.put("experimentName", loader.getExperimentName());
        message.put("propertyFilePath", this.propertyFilePath);
        message.put("testCasesPath", this.testCasesPath);

        return message;
    }

    private void auxBotAction(RESTestLoader loader, AbstractTestCaseGenerator generator){
        Collection<TestCase> testCases = null; 
        try{
            testCases = generator.generate();
        } catch (RESTestException e) {
            logger.error("Error generating test cases: {}", e.getMessage());
        }

        this.testCasesPath = loader.getTargetDirJava() + "/t.tmp";
        try (FileOutputStream fos = new FileOutputStream(this.testCasesPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(testCases);
        } catch (IOException e) {
            logger.error("Error writing test cases to file: {}", e.getMessage());
        }

        // Create target directory for test cases if it does not exist
        createDir(loader.getTargetDirJava());

        // Write (RestAssured) test cases
        RESTAssuredWriter writer = (RESTAssuredWriter) loader.createWriter();
        writer.write(testCases);
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
