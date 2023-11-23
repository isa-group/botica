package com.botica.launchers;

import static es.us.isa.restest.util.FileManager.createDir;
import static es.us.isa.restest.util.FileManager.deleteDir;

import org.json.JSONObject;

import es.us.isa.restest.runners.RESTestExecutor;

/**
 * This class is a launcher for executing test cases generated by generators.
 */
public class TestCaseExecutorLauncher extends AbstractLauncher{

    private String propertyFilePath;
    private String testCasesPath;

    public TestCaseExecutorLauncher(String keyToPublish, String orderToPublish) {
        super(keyToPublish, orderToPublish);
    }

    public TestCaseExecutorLauncher(String propertyFilePath, String testCasesPath, String keyToPublish, String orderToPublish) {
        super(keyToPublish, orderToPublish);
        this.propertyFilePath = propertyFilePath;
        this.testCasesPath = testCasesPath;
    }

    /**
     * Executes test cases generated.
     */
    @Override
    protected void botAction() {
        RESTestExecutor executor = new RESTestExecutor(this.propertyFilePath);

        // TODO: Check if is correct
        // Create directories to store test data extracted from the execution
        String experimentName = PropertyReader.readProperty(this.propertyFilePath, "experiment.name");

        String testDataDir = PropertyReader.readProperty(this.propertyFilePath, "data.tests.dir") + "/" + experimentName;
        String coverageDataDir = PropertyReader.readProperty(this.propertyFilePath, "data.coverage.dir") + "/" + experimentName;
        String allureResultsDir = PropertyReader.readProperty(this.propertyFilePath, "allure.results.dir") + "/" + experimentName;
        String allureReportDir = PropertyReader.readProperty(this.propertyFilePath, "allure.report.dir") + "/" + experimentName;

        String deletePreviousResults = PropertyReader.readProperty(this.propertyFilePath, "deletepreviousresults");
        if (deletePreviousResults != null && Boolean.parseBoolean(deletePreviousResults)) {
            deleteDir(testDataDir);
            deleteDir(coverageDataDir);
            deleteDir(allureResultsDir);
            deleteDir(allureReportDir);
        }

        createDir(testDataDir);
        createDir(coverageDataDir);
        //

        auxBotAction(executor);
    }

    private void auxBotAction(RESTestExecutor executor) {
        String allureResultsDirPath = PropertyReader.readProperty(this.propertyFilePath, "allure.results.dir");
        String experimentName = PropertyReader.readProperty(this.propertyFilePath, "experiment.name");
        System.setProperty("allure.results.directory", allureResultsDirPath + "/" + experimentName);
        executor.execute();
    }

    @Override
    protected JSONObject createMessage() {
        JSONObject message = new JSONObject();
        message.put("order", this.orderToPublish);
        message.put("propertyFilePath", this.propertyFilePath);
        message.put("testCasesPath", this.testCasesPath);

        return message;
    }
}
