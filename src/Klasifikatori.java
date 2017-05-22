import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Klasifikatori {

	public static void main(String[] args) {
		// 1. Ielādējam train.xml, randomizējam tvītu secību, sadalām datus 10 vienāda izmēra apakškopās 
		//	(ir ok, ja dažās apakškopas būs par vienu tvītu lielākas, ja tvītu skaits nedalās ar 10).
		NodeList alltweets = getTweets();

		// 2. Darām 10 iterācijas 10-fold Cross-Validation ciklam. Katrā iekšā notiek:
		for (int iteracija = 0; iteracija < 10; iteracija++){
			
			// 2.1. Izveidojam apmācības kopu no 9 apakškopām un validēšanas kopu no pārpalikušās vienas
			List <Node> trainingSet = new ArrayList<Node>();
			List <Node> testSet = new ArrayList<Node>();
			for (int temp = 0; temp < alltweets.getLength(); temp++) {
				if (temp % 10 == iteracija){
					testSet.add(alltweets.item(temp));
				} else {
					trainingSet.add(alltweets.item(temp));
				}
			}
			
			String positiveHeap = "";
			String negativeHeap = "";
			String neutralHeap = "";
			for (Node node : trainingSet) {
				Element eNode = (Element) node;
				// label={"negative","positive","neutral"}
				String label = eNode.getAttribute("label");
				String content = eNode.getElementsByTagName("content").item(0).getTextContent();
				if (label.equals("positive")){
					positiveHeap += content;
				} else if (label.equals("negative")){
					negativeHeap += content;
				} else {
					neutralHeap += content;
				}
				
//				System.out.println("label : " + label);
//				System.out.println("content : " + content);
				
			} 
			String[] str = neutralHeap.split(" ");
			System.out.println( iteracija+ " "+ str[22]);

			
		}

	}
	
	public static NodeList getTweets(){
		NodeList nList = null;
		try {
			File fXmlFile = new File("hcr/train.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
			
//			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			nList = doc.getElementsByTagName("item");

//			System.out.println("----------------------------");
//			for (int temp = 0; temp < nList.getLength(); temp++) {
//				Node nNode = nList.item(temp);
//				System.out.println("\nCurrent Element :" + nNode.getNodeName());
//				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//					Element eElement = (Element) nNode;
//					System.out.println("tweetid : " + eElement.getAttribute("tweetid"));
//					System.out.println("label : " + eElement.getAttribute("label"));
//					System.out.println("target : " + eElement.getAttribute("target"));
//					System.out.println("username : " + eElement.getAttribute("username"));
//					System.out.println("content : " + eElement.getElementsByTagName("content").item(0).getTextContent());
//				}
//			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return nList;
	}//public static NodeList getTweets()

}
