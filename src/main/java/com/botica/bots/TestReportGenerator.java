package com.botica.bots;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.botica.utils.RESTestUtil;

import es.us.isa.restest.coverage.CoverageGatherer;
import es.us.isa.restest.coverage.CoverageMeter;
import es.us.isa.restest.reporting.AllureReportManager;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.AllureAuthManager;
import es.us.isa.restest.util.RESTestException;

/**
 * This class is responsible for generating test reports and
 * sending messages through RabbitMQ with relevant information.
 */
public class TestReportGenerator extends AbstractBot{

    private String propertyFilePath;
    private String testCasesPath;

    private static final String EXPERIMENT_NAME_PROPERTY = "experiment.name";
    
    /**
     * Constructor for the TestReportGenerator class.
     * 
     * @param propertyFilePath  The path to the properties file.
     * @param testCasesPath     The path to the test cases generated.
     * @param keyToPublish      The binding key to publish a message to the RabbitMQ
     *                          broker.
     * @param orderToPublish    The order to publish in the message.
     */
    public TestReportGenerator(String propertyFilePath, String testCasesPath, String keyToPublish, String orderToPublish) {
        super(keyToPublish, orderToPublish);
        this.propertyFilePath = propertyFilePath;
        this.testCasesPath = testCasesPath;
    }

    @Override
    protected void botAction() {
        Collection<TestCase> testCases = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(testCasesPath);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            readTestCasesFromObjectStream(ois, testCases);
        } catch (IOException e) {
            logger.error("Error writing test cases to file: {}", e.getMessage());
        }
        
        String experimentName = RESTestUtil.readProperty(propertyFilePath, EXPERIMENT_NAME_PROPERTY);
        RESTestLoader loader = new RESTestLoader(propertyFilePath);
        try{
            loader.createGenerator(); //TODO: FIX (It is necessary to assign value to spec property in the Loader class)
        }catch(RESTestException e){
            logger.error("Error creating generator: {}", e.getMessage());
        }

        AllureReportManager allureReportManager = createAllureReportManager(propertyFilePath);
        StatsReportManager statsReportManager = createStatsReportManager(propertyFilePath);
        
        allureReportManager.generateReport();
        statsReportManager.setTestCases(testCases);
        statsReportManager.generateReport(experimentName, true);
    }

    @Override
    protected JSONObject createMessage() {
        JSONObject message = new JSONObject();
        message.put("order", orderToPublish);

        return message;
    }

    private Collection<TestCase> readTestCasesFromObjectStream(ObjectInputStream ois, Collection<TestCase> testCases) throws IOException {
        try {
            Object obj = ois.readObject();
            if (obj instanceof Collection<?>) {
                Collection<?> aux = (Collection<?>) obj;
                // TODO: Check how to improve the performance of this loop
                for (Object o : aux) {
                    if (o instanceof TestCase) {
                        testCases.add((TestCase) o);
                    }
                }
            }
            return testCases;
        } catch (ClassNotFoundException e) {
            logger.error("Error reading test cases from file: {}", e.getMessage());
            return testCases;
        }
    }

    //TODO: Change (Own definition of createStatsReportManager)
    private static StatsReportManager createStatsReportManager(String propertyFilePath) {

        String experimentName = RESTestUtil.readProperty(propertyFilePath, EXPERIMENT_NAME_PROPERTY);
        String testDataDir = RESTestUtil.readProperty(propertyFilePath, "data.tests.dir") + "/" + experimentName;
        String coverageDataDir = RESTestUtil.readProperty(propertyFilePath, "data.coverage.dir") + "/" + experimentName;
        boolean enableCSVStats = Boolean.parseBoolean(RESTestUtil.readProperty(propertyFilePath, "stats.csv"));
        boolean enableInputCoverage = Boolean.parseBoolean(RESTestUtil.readProperty(propertyFilePath, "coverage.input"));
        boolean enableOutputCoverage = Boolean.parseBoolean(RESTestUtil.readProperty(propertyFilePath, "coverage.output"));
        String OAISpecPath = RESTestUtil.readProperty(propertyFilePath, "oas.path");

        OpenAPISpecification spec = new OpenAPISpecification(OAISpecPath);

		CoverageMeter coverageMeter = new CoverageMeter(new CoverageGatherer(spec));

		return new StatsReportManager(testDataDir, coverageDataDir, enableCSVStats, enableInputCoverage,
					enableOutputCoverage, coverageMeter);
	}

    //TODO: Change (Own definition of createAllureReportManager)
    private static AllureReportManager createAllureReportManager(String propertyFilePath) {
		AllureReportManager arm = null;
        String experimentName = RESTestUtil.readProperty(propertyFilePath, EXPERIMENT_NAME_PROPERTY);
        String allureResultsDir = RESTestUtil.readProperty(propertyFilePath, "allure.results.dir") + "/" + experimentName;
        String allureReportDir = RESTestUtil.readProperty(propertyFilePath, "allure.report.dir") + "/" + experimentName;
        String confPath = RESTestUtil.readProperty(propertyFilePath, "conf.path");
        String OAISpecPath = RESTestUtil.readProperty(propertyFilePath, "oas.path");
		
        //Find auth property names (if any)
        List<String> authProperties = AllureAuthManager.findAuthProperties(new OpenAPISpecification(OAISpecPath), confPath);

        arm = new AllureReportManager(allureResultsDir, allureReportDir, authProperties);
        arm.setEnvironmentProperties(propertyFilePath);
        arm.setHistoryTrend(true);
		
		return arm;
	}

}
