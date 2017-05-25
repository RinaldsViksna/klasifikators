import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class K2Weka {

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
			FastVector fvClassVal = new FastVector(3);

			// 2.2. No apmācības kopas izveidojam unigrams sarakstus (vienkārši sadalot tvītu tekstus pēc whitespace)
			String allTweets = "";
			
			List<String> tweetSentiments = Arrays.asList("positive", "negative", "neutral", "irrelevant", "unsure");
			Attribute tweetSentiment = new Attribute("sentiment", tweetSentiments);

			 
			for (Node node : trainingSet) {
				Element eNode = (Element) node;
				// label={"negative","positive","neutral"}
				String label = eNode.getAttribute("label");
				String content = eNode.getElementsByTagName("content").item(0).getTextContent();
				allTweets += content;
			}
			
			Set<String> allUniqueUnigramsSet = getUniqueUnigrams(allTweets);
			List<String> allUniqueUnigrams = new ArrayList<String>(allUniqueUnigramsSet);
			// Declare the feature vector  fvWekaAttributes
			FastVector tweetAttributes = new FastVector(allUniqueUnigrams.size() + 1);
			
			tweetAttributes.add(tweetSentiment);
			for (String unigram : allUniqueUnigrams) {
				Attribute attribute = new Attribute(unigram);
				tweetAttributes.add(attribute);
			}
			
			// 2.3. Ar Weka izveidojam Naive Bayes un Support Vector Machine modeļus
			// Create an empty training set
			Instances isTrainingSet = new Instances("ApmacibasKopa", tweetAttributes, trainingSet.size());
			// Set class index
			isTrainingSet.setClassIndex(0);
			
			for (Node node : trainingSet) {
				Element eNode = (Element) node;
				// label={"negative","positive","neutral","irrelevant"}
				String label = eNode.getAttribute("label");
				String content = eNode.getElementsByTagName("content").item(0).getTextContent();
				
				Set<String> tweetUniqueUnigramsSet = getUniqueUnigrams(content);
				List<String> tweetUniqueUnigrams = new ArrayList<String>(tweetUniqueUnigramsSet);
				// Create the instance
				Instance tweetInstance = new DenseInstance(allUniqueUnigrams.size()+1);
				tweetInstance.setValue((Attribute)tweetAttributes.elementAt(0), label);
				for (int i = 1; i < allUniqueUnigrams.size(); ++i) {
					boolean contains = tweetUniqueUnigrams.contains(allUniqueUnigrams.get(i));
					if (contains){
						tweetInstance.setValue((Attribute)tweetAttributes.elementAt(i), 1);
					}
				}

				// Add the instance
				isTrainingSet.add(tweetInstance);
			}
			System.out.println(isTrainingSet);
			// Create a naïve bayes classifier
			Classifier cModel = (Classifier)new NaiveBayes();
			try {
				cModel.buildClassifier(isTrainingSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// Create an empty test set
			Instances isTestSet = new Instances("ApmacibasKopa", tweetAttributes, trainingSet.size());
			// Set class index
			isTestSet.setClassIndex(0);
			
			for (Node node : testSet) {
				Element eNode = (Element) node;
				// label={"negative","positive","neutral"}
				String label = eNode.getAttribute("label");
				String content = eNode.getElementsByTagName("content").item(0).getTextContent();
				
				Set<String> tweetUniqueUnigrams = getUniqueUnigrams(content);
				// Create the instance
				
				Instance tweetInstance = new DenseInstance(allUniqueUnigrams.size()+1);
				tweetInstance.setValue((Attribute)tweetAttributes.elementAt(0), label);
				for (int i = 1; i < allUniqueUnigrams.size(); i++) {
					boolean contains = tweetUniqueUnigrams.contains(allUniqueUnigrams.get(i));
					if (contains){
						tweetInstance.setValue((Attribute)tweetAttributes.elementAt(i), 1);
					}
				}
				// Add the instance
				isTestSet.add(tweetInstance);
			}
			
			 // Test the model
			 
			 try {
				 Evaluation eTest = new Evaluation(isTrainingSet);
				 eTest.evaluateModel(cModel, isTestSet);
				 // Print the result à la Weka explorer:
				 String strSummary = eTest.toSummaryString();
				 System.out.println(strSummary);
				 
				 // Get the confusion matrix
				 double[][] cmMatrix = eTest.confusionMatrix();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}//for (int iteracija = 0; iteracija < 10; iteracija++){

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

	public static Set<String> getUniqueUnigrams(String content){
		String[] allUnigrams = content.split("\\s+");
		ArrayList<String> cleanedUnigrams = new ArrayList<String>();
		for (String unigram : allUnigrams) {
			if (unigram.contains("#") ){
				cleanedUnigrams.add("hashtag");
				continue;
			}
			if ( unigram.contains("@") ){
				cleanedUnigrams.add("user");
				continue;
			}
			if ( unigram.contains("http://") ){
				cleanedUnigrams.add("link");
				continue;
			}
			cleanedUnigrams.add(unigram);
		}

		Set<String> uniqueUnigrams = new HashSet<String>(cleanedUnigrams);
//		System.out.println("Total unigrams: " + cleanedUnigrams.size());
//		System.out.println("Unique unigrams: " + uniqueUnigrams.size());
		return uniqueUnigrams;
	}
}
