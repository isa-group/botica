package com.botica.examples;

import es.us.isa.restest.util.RESTestException;

import com.botica.launchers.TestCaseGeneratorLauncher;

public class TestCaseGeneration {

    public static final String PROPERTY_FILE_PATH="src/main/resources/Examples/Ex1_RandomGeneration/user_config.properties"; 		// Path to user properties file with configuration options

    public static void main(String[] args) throws RESTestException {
       TestCaseGeneratorLauncher launcher = new TestCaseGeneratorLauncher();
       launcher.launchTestCases(PROPERTY_FILE_PATH);
    }
}
