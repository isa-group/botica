package com.botica;

import es.us.isa.restest.configuration.pojos.TestConfigurationObject;
import es.us.isa.restest.generators.RandomTestCaseGenerator;
import es.us.isa.restest.specification.OpenAPISpecification;

public class RandomTestCaseGeneratorAdapter extends RandomTestCaseGenerator {

    public RandomTestCaseGeneratorAdapter(OpenAPISpecification spec, TestConfigurationObject conf, int nTests) {
        super(spec, conf, nTests);
        //TODO Auto-generated constructor stub
    }
    
}
