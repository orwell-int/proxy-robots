package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

public class Configuration implements IConfiguration {
    final static Logger logback = LoggerFactory.getLogger(Configuration.class);
    public boolean isPopulated = false;
    ConfigModel configuration;
    private String filePath;

    public Configuration() {
        this("/configuration.xml");
    }

    public Configuration(String filePath) {
        this.filePath = filePath;
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
            InputStream xml = getClass().getResourceAsStream(filePath);
            /*if (!xml.exists()) {
                logback.error("Configuration:populate(): File "
						+ this.filePath + " does not exist");
				throw new FileNotFoundException(filePath);
			}*/
            this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
        } catch (JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e.toString());
            throw e;
        }
        isPopulated = true;
    }

}
