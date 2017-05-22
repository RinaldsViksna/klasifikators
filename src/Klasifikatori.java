import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/* @Author: https://gist.github.com/jbzdak/61398b8ad795d22724dd */
public class Klasifikatori {

	public static void main(String[] args) {

		try {
			
			File fXmlFile = new File("hcr/train.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
			
			
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("item");

			System.out.println("----------------------------");
			int items = 0;
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				items = temp;
//				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					
//					System.out.println("tweetid : " + eElement.getAttribute("tweetid"));
//					System.out.println("label : " + eElement.getAttribute("label"));
//					System.out.println("target : " + eElement.getAttribute("target"));
//					System.out.println("username : " + eElement.getAttribute("username"));
//					System.out.println("content : " + eElement.getElementsByTagName("content").item(0).getTextContent());

				}
			}
			System.out.println("\nTotal elements :" + items);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
