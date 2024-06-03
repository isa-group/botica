package es.us.isa.botica.runners;

import es.us.isa.botica.configuration.MainConfiguration;
import es.us.isa.botica.util.configuration.JacksonConfigurationFileLoader;
import java.io.File;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class ShutdownLoader extends AbstractLoader {
    private static final Logger logger = LogManager.getLogger(ShutdownLoader.class);

    private final MainConfiguration configurationFile;

    public ShutdownLoader(File file) {
        this(new JacksonConfigurationFileLoader().load(file, MainConfiguration.class));
    }

    public ShutdownLoader(MainConfiguration configurationFile) {
        this.configurationFile = configurationFile;
    }
}
