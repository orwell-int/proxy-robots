package orwell.proxy;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class Configuration {
	
	private String filePath;

	public Configuration()
	{
		this("orwell/proxy/configuration.xml");
	}

	public Configuration(String filePath)
	{
		this.filePath=filePath;
		
		try{
		File configFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(configFile);
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
	 
		System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
		
		NodeList nList = doc.getElementsByTagName("server-game");
		 
		System.out.println("----------------------------");
	 
		for (int temp = 0; temp < nList.getLength(); temp++) {
	 
			Node nNode = nList.item(temp);
	 
			System.out.println("\nCurrent Element: " + nNode.getNodeName());
	 
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) nNode;
	 
				System.out.println("name: " + eElement.getAttribute("name"));
				System.out.println("ip: " + eElement.getElementsByTagName("ip").item(0).getTextContent());
				System.out.println("pushPort: " + eElement.getElementsByTagName("pushPort").item(0).getTextContent());
				System.out.println("subPort: " + eElement.getElementsByTagName("subPort").item(0).getTextContent());	 
			}
	}
		
	
	} catch (Exception e) {
		e.printStackTrace();
    }

}
}

