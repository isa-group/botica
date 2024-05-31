package es.us.isa.botica.runners;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import es.us.isa.botica.utils.configuration.CreateConfiguration;
import lombok.Getter;

import java.io.File;

@Getter
public class BOTICALoader extends AbstractLoader {

    public BOTICALoader() {
	}

    public MainConfiguration loadConfiguration() {
        File file = new File("/run/secrets", CreateConfiguration.BOTICA_CONFIG_SECRET);
        JacksonConfigurationFileLoader configurationFileLoader = new JacksonConfigurationFileLoader();
        return configurationFileLoader.load(file, MainConfiguration.class);
    }
}