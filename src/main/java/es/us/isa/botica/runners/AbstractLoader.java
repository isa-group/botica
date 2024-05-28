package es.us.isa.botica.runners;

import es.us.isa.botica.utils.property.PropertyManager;

public abstract class AbstractLoader {

    protected String propertiesFilePath;
    protected Boolean hasGlobalPropertiesPath;

    // Read the parameter values from the .properties file.
    protected void readProperties() {
    }

    // Read the parameter value from the user property file (if provided). If the
    // value is not found, look for it in the global .properties file
    // (config.properties)
    protected String readProperty(String propertyName) {
        return PropertyManager.readProperty(propertyName);
    }
}
