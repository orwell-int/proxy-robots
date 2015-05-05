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
    public void populate() throws JAXBException {
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ConfigModel.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            if (EnumConfigFileType.RESOURCE == enumConfigFileType) {
                InputStream xml = getClass().getResourceAsStream(filePath);
                this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
                logback.info("Configuration loaded from resource file");
            } else if (EnumConfigFileType.FILE == enumConfigFileType) {
                File file = new File(filePath);
                this.configuration = (ConfigModel) unmarshaller.unmarshal(file);
                logback.info("Configuration loaded from external file");
            } else {
                logback.error("Config file type of " + enumConfigFileType + " not handled");
            }
        } catch (JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e.toString());
            throw e;
        }
        isPopulated = true;
    }

}
