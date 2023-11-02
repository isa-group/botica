package com.botica.utils;

import es.us.isa.restest.util.PropertyManager;

public class RESTestUtil {

    private RESTestUtil() {
        throw new IllegalStateException("Utility class");
    }

    // TODO: Check if is correct
    public static String readProperty(String userPropertiesFilePath, String propertyName) {

        // Read property from user property file (if provided)
        String value = PropertyManager.readProperty(userPropertiesFilePath, propertyName);

        // If null, read property from global property file (config.properties)
        if (value == null)
            value = PropertyManager.readProperty(propertyName);

        return value;
    }
}
