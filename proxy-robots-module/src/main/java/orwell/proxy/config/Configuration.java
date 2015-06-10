package orwell.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Created by Michaël Ludmann on 6/9/15.
 */
public abstract class Configuration {
    private final static Logger logback = LoggerFactory.getLogger(Configuration.class);
    protected Unmarshaller unmarshaller;
    private boolean isPopulated;
    private ConfigModel configModel;

    public void populate() {
        setIsPopulated(false);
        final JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(ConfigModel.class);
            unmarshaller = jc.createUnmarshaller();
            configModel = populateFromSource();
        } catch (final JAXBException e) {
            logback.error("Configuration:populate(): Error in configuration population: "
                    + e);
            configModel = null;
        }
    }

    public boolean isPopulated() {
        return isPopulated;
    }

    protected void setIsPopulated(final boolean isPopulated) {
        this.isPopulated = isPopulated;
    }

    protected abstract ConfigModel populateFromSource() throws JAXBException;

    public ConfigModel getConfigModel() {
        return configModel;
    }
}
