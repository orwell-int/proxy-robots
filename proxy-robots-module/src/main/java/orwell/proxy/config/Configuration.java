package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orwell.proxy.Cli;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

public class Configuration implements IConfiguration {
    private final static Logger logback = LoggerFactory.getLogger(Configuration.class);
    private final static String DEFAULT_CONFIG_FILEPATH_INSIDE_JAR = "/config.defaults.xml";
    private final String filePath;
    private final EnumConfigFileType enumConfigFileType;
    private boolean isPopulated = false;
    private ConfigModel configuration;

    public Configuration(final ConfigFactoryParameters configFactoryParameters) {
        this.filePath = configFactoryParameters.getFilePath();
        this.enumConfigFileType = configFactoryParameters.getEnumConfigFileType();
        populate();
    }

    @Override
    public ConfigModel getConfigModel() {
        return this.configuration;
    }

    private void populate() {
        final JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ConfigModel.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();

            switch (enumConfigFileType) {
                case RESOURCE:
                    onResource(unmarshaller);
                    break;
                case FILE:
                    onFile(unmarshaller);
                    break;
                default:
                    onDefault();
                    break;
            }
        } catch (final JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e);
        }
    }

    private void onDefault() {
        logback.error("Config file type of " + enumConfigFileType + " not handled");
    }

    private void onFile(final Unmarshaller unmarshaller) throws JAXBException {
        final File file = new File(filePath);
        this.configuration = (ConfigModel) unmarshaller.unmarshal(file);
        isPopulated = true;
        logback.info("Configuration loaded from external file: " + filePath);
    }

    private void onResource(final Unmarshaller unmarshaller) throws JAXBException {
        logback.debug("NINININ");
        InputStream xml = getClass().getResourceAsStream(filePath);
        if (null == xml) {
            logback.info("Configuration " + filePath + " not found in Jar." +
                    " Tip: You can create your own custom file in " +
                    "src/main/resources" + Cli.CUSTOM_CONFIG_FILEPATH_INSIDE_JAR);
            xml = getClass().getResourceAsStream(DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
            if (null == xml) {
                logback.error("Failed to find default configuration file: " +
                        DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
                isPopulated = false;
                return;
            } else {
                logback.info("Configuration found in default resource file: " +
                        DEFAULT_CONFIG_FILEPATH_INSIDE_JAR);
            }
        }
        this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
        isPopulated = true;
        logback.info("Configuration loaded");
    }


    public boolean isPopulated() {
        return isPopulated;
    }
}
