package com.botica.examples;

import com.botica.launchers.TestCaseGeneratorLauncher;

import es.us.isa.restest.util.RESTestException;

public class TestCaseGeneration {

    public static final String PROPERTY_FILE_PATH="src/main/resources/Examples/Ex1_RandomGeneration/user_config.properties";		// Path to user properties file with configuration options
    public static final String BOT_ID = "bot_1";

    public static void main(String[] args) throws RESTestException {
       TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
       launcher.launchTestCases(PROPERTY_FILE_PATH, BOT_ID);
    }
}
