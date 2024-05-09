package es.us.isa.botica.runners;

import es.us.isa.botica.utils.property.PropertyManager;

public abstract class AbstractLoader {

    protected String propertiesFilePath;
    protected Boolean hasGlobalPropertiesPath;

    // Read the parameter values from the .properties file.
    protected abstract void readProperties();

    // Read the parameter value from the user property file (if provided). If the
    // value is not found, look for it in the global .properties file
    // (config.properties)
    protected String readProperty(String propertyName) {

        // Read property from user property file (if provided)
        String value = PropertyManager.readProperty(propertiesFilePath, propertyName);

        // If null, read property from global property file (config.properties)
        if (value == null && hasGlobalPropertiesPath) {
            value = PropertyManager.readProperty(propertyName);
        }

        return value;
    }
}
