package com.botica.launchers;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botica.examples.TestCaseGeneration;
import com.botica.generators.TestCaseGenerator;
import com.botica.RabbitMQManager;

import es.us.isa.restest.generators.ARTestCaseGenerator;
import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.generators.ConstraintBasedTestCaseGenerator;
import es.us.isa.restest.generators.FuzzingTestCaseGenerator;
import es.us.isa.restest.generators.RandomTestCaseGenerator;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.PropertyManager;
import es.us.isa.restest.util.RESTestException;
import es.us.isa.restest.writers.restassured.RESTAssuredWriter;

import static es.us.isa.restest.util.FileManager.createDir;

public class TestCaseGeneratorLauncher {

    public String PROPERTY_FILE_PATH = "";
    public static final Logger logger = Logger.getLogger(TestCaseGeneration.class.getName());

    private RabbitMQManager messageSender = new RabbitMQManager("testCaseGenerator", "admin", "testing1", "/", "localhost", 5672);

    public void launchTestCases(String propertyFilePath, String botId) {
        try {
            this.PROPERTY_FILE_PATH = propertyFilePath;
            messageSender.connect();
            System.out.println("Connected to RabbitMQ");
            messageSender.receiveMessage(PROPERTY_FILE_PATH, botId, "testCaseGenerator");
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void generateTestCases(String propertyFilePath, String botId) {
        try {
            RESTestLoader loader = new RESTestLoader(propertyFilePath);

            String generatorType = PropertyManager.readProperty(propertyFilePath, "generator");

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

            TestCaseGenerator testGenerator = new TestCaseGenerator(generator, loader, botId, generatorType);

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
}