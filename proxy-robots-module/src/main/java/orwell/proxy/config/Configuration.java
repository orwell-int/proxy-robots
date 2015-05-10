package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

public class Configuration implements IConfiguration {
    final static Logger logback = LoggerFactory.getLogger(Configuration.class);
    private final String filePath;
    private final EnumConfigFileType enumConfigFileType;
    public boolean isPopulated = false;
    ConfigModel configuration;

    public Configuration(final ConfigFactoryParameters configFactoryParameters) {
        this.filePath = configFactoryParameters.getFilePath();
        this.enumConfigFileType = configFactoryParameters.getEnumConfigFileType();
        populate();
    }

    @Override
    public ConfigModel getConfigModel() {
        return this.configuration;
    }

    private boolean populate() {
        final JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ConfigModel.class);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();

            switch (enumConfigFileType) {
                case RESOURCE:
                    final InputStream xml = getClass().getResourceAsStream(filePath);
                    this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
                    isPopulated = true;
                    logback.info("Configuration loaded from resource file");
                    break;
                case FILE:
                    final File file = new File(filePath);
                    this.configuration = (ConfigModel) unmarshaller.unmarshal(file);
                    isPopulated = true;
                    logback.info("Configuration loaded from external file");
                    break;
                default:
                    logback.error("Config file type of " + enumConfigFileType + " not handled");
                    break;
            }
        } catch (final JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e);
        }
        return isPopulated;
    }

}
