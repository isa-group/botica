package com.botica;

import java.util.Collection;

import es.us.isa.restest.configuration.pojos.Operation;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;
import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.util.RESTestException;

public class AbstractTestCaseGeneratorAdapter extends AbstractTestCaseGenerator {

    public AbstractTestCaseGeneratorAdapter(OpenAPISpecification spec, TestConfigurationObject conf, int nTests) {
        super(spec, conf, nTests);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected Collection<TestCase> generateOperationTestCases(Operation testOperation) throws RESTestException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateOperationTestCases'");
    }

    @Override
    public TestCase generateNextTestCase(Operation testOperation) throws RESTestException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateNextTestCase'");
    }

    @Override
    protected boolean hasNext() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasNext'");
    }
    
}