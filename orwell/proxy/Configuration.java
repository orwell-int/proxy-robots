package orwell.proxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;

public class Configuration {
	
	private String filePath;
	ConfigModel configuration;
	public boolean isPopulated = false;

	public Configuration()
	{
		this("orwell/proxy/configuration.xml");
	}

	public Configuration(String filePath) {
		this.filePath = filePath;
	}
	
	public ConfigModel getConfigModel()
	{
		return this.configuration;
	}
	
	public void populate() throws FileNotFoundException, JAXBException
	{
		JAXBContext jc;
		try {
			jc = JAXBContext.newInstance(ConfigModel.class);
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        File xml = new File(filePath);
	        if(!xml.exists())
	        {
				System.out.println("Configuration:populate(): File " + this.filePath + " does not exist");
	        	throw new FileNotFoundException(filePath);
	        }
	        this.configuration = (ConfigModel) unmarshaller.unmarshal(xml);
		} catch (JAXBException e) {
			System.out.println("Configuration:populate(): Error in configurationBOM population: " + e.toString());
			throw e;
		}
		isPopulated = true;
	}
	
	
}
