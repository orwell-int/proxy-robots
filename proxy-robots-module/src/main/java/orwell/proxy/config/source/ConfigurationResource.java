package orwell.proxy.config.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.Cli;
import orwell.proxy.config.ConfigModel;
import orwell.proxy.config.Configuration;

import javax.xml.bind.JAXBException;
import java.io.InputStream;

/**
 * Created by Michaël Ludmann on 6/9/15.
 */
public class ConfigurationResource extends Configuration {
    private final static Logger logback = LoggerFactory.getLogger(ConfigurationResource.class);
    private final static String DEFAULT_CONFIG_FILEPATH_INSIDE_JAR = "/config.defaults.xml";
    private final String filePath;

    public ConfigurationResource(final String filePath) {
        this.filePath = filePath;
        populate();
    }

    @Override
    protected ConfigModel populateFromSource() throws JAXBException {
        InputStream xml = getClass().getResourceAsStream(filePath);
        if (null == xml) {
            logback.info("Configuration " + filePath + " not found in Jar." +
                    " Tip: You can create your own custom file in " +
                    "src/main/resources" + Cli.CUSTOM_CONFIG_FILEPATH_INSIDE_JAR);
            xml = getClass().getResourceAsStream(DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
            if (null == xml) {
                logback.error("Failed to find default configuration file: " +
                        DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
                setIsPopulated(false);
                return null;
            } else {
                logback.info("Configuration found in default resource file: " +
                        DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
            }
        }
        final ConfigModel configModel = (ConfigModel) unmarshaller.unmarshal(xml);
        setIsPopulated(true);
        logback.info("Configuration loaded");

        return configModel;
    }
}
