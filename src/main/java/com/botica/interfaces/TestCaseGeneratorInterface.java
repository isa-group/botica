package com.botica.interfaces;

import java.util.Collection;

import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

/**
 * This TestCaseGeneratorInterface specifies the methods that should be provided
 * by classes implementing a test case generator.
 */
public interface TestCaseGeneratorInterface {
    Collection<TestCase> generate() throws RESTestException;
}
