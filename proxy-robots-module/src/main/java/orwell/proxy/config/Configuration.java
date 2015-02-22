package orwell.proxy.config;

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
	final static Logger logback = LoggerFactory.getLogger(Configuration.class); 

	private String filePath;
	ConfigModel configuration;
	public boolean isPopulated = false;

	public Configuration() {
		this("/configuration.xml");
	}

	public Configuration(String filePath) {
		this.filePath = filePath;
	}

	public ConfigModel getConfigModel() {
		return this.configuration;
	}

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
