package com.botica.interfaces;

import java.util.Collection;

import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

public interface TestCaseGeneratorInterface {
    Collection<TestCase> generate() throws RESTestException;
}
