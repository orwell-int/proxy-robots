package orwell.proxy.config;

import javax.xml.bind.JAXBException;

/**
 * Created by parapampa on 03/05/15.
 */
public interface IConfiguration {

    ConfigModel getConfigModel();

    boolean populate() throws JAXBException;
}
