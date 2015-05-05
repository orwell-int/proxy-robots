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
    public boolean isPopulated = false;
    ConfigModel configuration;
    private String filePath;
    private EnumConfigFileType enumConfigFileType;

    public Configuration(ConfigCli configCli) {
        this.filePath = configCli.getFilePath();
        this.enumConfigFileType = configCli.getEnumConfigFileType();
    }

    @Override
    public ConfigModel getConfigModel() {
        return this.configuration;
    }

    @Override
    public boolean populate() {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ConfigModel.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            switch (enumConfigFileType) {
                case RESOURCE:
                    InputStream xml = getClass().getResourceAsStream(filePath);
                    this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
                    isPopulated = true;
                    logback.info("Configuration loaded from resource file");
                    break;
                case FILE:
                    File file = new File(filePath);
                    this.configuration = (ConfigModel) unmarshaller.unmarshal(file);
                    isPopulated = true;
                    logback.info("Configuration loaded from external file");
                    break;
                default:
                    logback.error("Config file type of " + enumConfigFileType + " not handled");
                    break;
            }
        } catch (JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e.toString());
        }
        return isPopulated;
    }

}
