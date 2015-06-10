package orwell.proxy.config.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.Configuration;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Michaël Ludmann on 6/9/15.
 */
public class ConfigurationFile extends Configuration {
    private final static Logger logback = LoggerFactory.getLogger(ConfigurationFile.class);
    private final String filePath;

    public ConfigurationFile(final String filePath) throws FileNotFoundException {
        this.filePath = filePath;

        final File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(filePath);
        }
        logback.info("Using configuration file given as parameter: " + filePath);

        populate();
    }

    @Override
    protected ConfigModel populateFromSource() throws JAXBException {
        final File file = new File(filePath);
        final ConfigModel configModel = (ConfigModel) unmarshaller.unmarshal(file);
        setIsPopulated(true);
        logback.info("Configuration loaded from external file: " + filePath);
        return configModel;
    }
}
