package com.botica.runners;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.json.JSONObject;

import com.botica.utils.RESTestUtil;

import es.us.isa.restest.coverage.CoverageGatherer;
import es.us.isa.restest.coverage.CoverageMeter;
import es.us.isa.restest.reporting.AllureReportManager;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.RESTestLoader;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

import java.util.ArrayList;
import java.util.Collection;

public class TestReporter {
    
    // Transform JSON to String:
    // {"nTotalNominal":20,"faultyRatio":0.05,"nTotalFaulty":0,"propertyFilePath":"src/main/resources/Examples/Ex5_CBTGeneration/user_config.properties","maxTriesPerTestCase":100,"targetDirJava":"src/main/resources/Examples/Ex5_CBTGeneration/test_cases","allureReportsPath":"src/main/resources/Examples/Ex5_CBTGeneration/allure_report","botId":"gen_5","experimentName":"anApiOfIceAndFire","order":"executeTestCases","generatorType":"CBT"}
    //private static String json = "{\"nTotalNominal\":20,\"faultyRatio\":0.05,\"nTotalFaulty\":0,\"propertyFilePath\":\"src/main/resources/Examples/Ex5_CBTGeneration/user_config.properties\",\"maxTriesPerTestCase\":100,\"targetDirJava\":\"src/main/resources/Examples/Ex5_CBTGeneration/test_cases\",\"allureReportsPath\":\"src/main/resources/Examples/Ex5_CBTGeneration/allure_report\",\"botId\":\"gen_5\",\"experimentName\":\"anApiOfIceAndFire\",\"order\":\"executeTestCases\",\"generatorType\":\"CBT\"}";

    // Transform JSON to String:
    // {"nTotalNominal":20,"testCasesPath":"src/main/resources/Examples/Ex5_CBTGeneration/test_cases/t.tmp","faultyRatio":0.05,"nTotalFaulty":0,"propertyFilePath":"src/main/resources/Examples/Ex5_CBTGeneration/user_config.properties","maxTriesPerTestCase":100,"targetDirJava":"src/main/resources/Examples/Ex5_CBTGeneration/test_cases","allureReportsPath":"src/main/resources/Examples/Ex5_CBTGeneration/allure_report","botId":"gen_5","experimentName":"anApiOfIceAndFire","order":"executeTestCases","generatorType":"CBT"}
    //private static String json = "{\"nTotalNominal\":20,\"testCasesPath\":\"src/main/resources/Examples/Ex5_CBTGeneration/test_cases/t.tmp\",\"faultyRatio\":0.05,\"nTotalFaulty\":0,\"propertyFilePath\":\"src/main/resources/Examples/Ex5_CBTGeneration/user_config.properties\",\"maxTriesPerTestCase\":100,\"targetDirJava\":\"src/main/resources/Examples/Ex5_CBTGeneration/test_cases\",\"allureReportsPath\":\"src/main/resources/Examples/Ex5_CBTGeneration/allure_report\",\"botId\":\"gen_5\",\"experimentName\":\"anApiOfIceAndFire\",\"order\":\"executeTestCases\",\"generatorType\":\"CBT\"}";

    // Transform JSON to String:
    // {"nTotalNominal":105,"testCasesPath":"src/main/resources/Examples/Ex4_CBTGeneration/test_cases/t.tmp","faultyRatio":0.05,"nTotalFaulty":0,"propertyFilePath":"src/main/resources/Examples/Ex4_CBTGeneration/user_config.properties","maxTriesPerTestCase":100,"targetDirJava":"src/main/resources/Examples/Ex4_CBTGeneration/test_cases","allureReportsPath":"src/main/resources/Examples/Ex4_CBTGeneration/allure_report","botId":"gen_4","experimentName":"restcountries","order":"executeTestCases","generatorType":"CBT"}
    private static String json = "{\"nTotalNominal\":105,\"testCasesPath\":\"src/main/resources/Examples/Ex4_CBTGeneration/test_cases/t.tmp\",\"faultyRatio\":0.05,\"nTotalFaulty\":0,\"propertyFilePath\":\"src/main/resources/Examples/Ex4_CBTGeneration/user_config.properties\",\"maxTriesPerTestCase\":100,\"targetDirJava\":\"src/main/resources/Examples/Ex4_CBTGeneration/test_cases\",\"allureReportsPath\":\"src/main/resources/Examples/Ex4_CBTGeneration/allure_report\",\"botId\":\"gen_4\",\"experimentName\":\"restcountries\",\"order\":\"executeTestCases\",\"generatorType\":\"CBT\"}";

    public static void main(String[] args) {
        
        JSONObject messageInfo = new JSONObject(json);
        String propertyFilePath = messageInfo.getString("propertyFilePath");
        String testCasesPath = messageInfo.getString("testCasesPath");
        Collection<TestCase> testCases = null;

        try (FileInputStream fis = new FileInputStream(testCasesPath);
                ObjectInputStream ois = new ObjectInputStream(fis)) {
            try {
                testCases = (Collection<TestCase>) ois.readObject();
            } catch (ClassNotFoundException e) {
                System.out.println("Error reading test cases from file: " + e.getMessage());
                testCases = new ArrayList<>();
            }
        } catch (IOException e) {
            System.out.println("Error writing test cases to file:" + e.getMessage());
        }
        
        String experimentName = RESTestUtil.readProperty(propertyFilePath, "experiment.name");
        RESTestLoader loader = new RESTestLoader(propertyFilePath);
        try{
            loader.createGenerator(); //TODO: FIX
        }catch(RESTestException e){
            System.out.println("Error creating generator: " + e.getMessage());
        }

        AllureReportManager allureReportManager = loader.createAllureReportManager();
        StatsReportManager statsReportManager = createStatsReportManager(propertyFilePath);
        
        allureReportManager.generateReport();
        statsReportManager.setTestCases(testCases);
        statsReportManager.generateReport(experimentName, true);

    }

    private static StatsReportManager createStatsReportManager(String propertyFilePath) {

        String experimentName = RESTestUtil.readProperty(propertyFilePath, "experiment.name");
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

}
