import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.SparseInstance;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@SuppressWarnings("deprecation")
public class K2Weka {
	public static final List<String> tweetSentiments = Arrays.asList("positive", "negative", "neutral");
//	public static final List<String> tweetSentiments = Arrays.asList("positive", "negative");
	
	public static void main(String[] args) {
		// 1. Ielādējam train.xml, randomizējam tvītu secību, sadalām datus 10 vienāda izmēra apakškopās 
		//	(ir ok, ja dažās apakškopas būs par vienu tvītu lielākas, ja tvītu skaits nedalās ar 10).
		List <Node> alltweets = getTweets();
		System.out.println("Izmantojamo vietu skaits failā " + alltweets.size());

		// Statistikai Confusion Matrix
		double[][] bayesMultiResults = new double[tweetSentiments.size()][tweetSentiments.size()]; 
		double[][] svmResults = new double[tweetSentiments.size()][tweetSentiments.size()]; 
		double[][] majResults = new double[tweetSentiments.size()][tweetSentiments.size()]; 
		
		// 2. Darām 10 iterācijas 10-fold Cross-Validation ciklam. Katrā iekšā notiek:
		for (int iteracija = 0; iteracija < 10; iteracija++){
			System.out.println("Iterācija "+ iteracija);
			
			// 2.1. Izveidojam apmācības kopu no 9 apakškopām un validēšanas kopu no pārpalikušās vienas
			List <Node> trainingSet = new ArrayList<Node>();
			List <Node> testSet = new ArrayList<Node>();
			
			for (int temp = 0; temp < alltweets.size(); temp++) {
				if (temp % 10 == iteracija){
					testSet.add(alltweets.get(temp));
				} else {
					trainingSet.add(alltweets.get(temp));
				}
			}

			// 2.2. No apmācības kopas izveidojam unigrams sarakstus (vienkārši sadalot tvītu tekstus pēc whitespace)
			String allTweets = "";
			 
			for (Node node : trainingSet) {
				Element eNode = (Element) node;
				//String label = eNode.getAttribute("label");
				String content = eNode.getElementsByTagName("content").item(0).getTextContent();
				
				allTweets += content;
			}
			
			List<String> allUniqueUnigrams = getUniqueUnigrams(allTweets);
			System.out.println("Total number of unique unigrams: "+ allUniqueUnigrams.size());
			// Ģenerē apmācības kopu
			Instances isTrainingSet = generateSet("ApmacibasKopa", trainingSet, allUniqueUnigrams);
			// Ģenerē testa kopu
			Instances isTestSet = generateSet("TestaKopa",testSet, allUniqueUnigrams);
			
			// 2.3. Ar Weka izveidojam Naive Bayes un Support Vector Machine modeļus
			
			// Izveidojam Naive Bayes klasifikatoru
			Classifier nbModel = (Classifier)new NaiveBayesMultinomial();
			try {
				nbModel.buildClassifier(isTrainingSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Izveidojam SupportVectorMachine klasifikatoru
			//https://sourceforge.net/projects/weka/files/weka-packages/LibSVM1.0.10.zip/download?use_mirror=kent&download=
			LibSVM svmModel = new LibSVM();
			try {
				svmModel.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
				svmModel.buildClassifier(isTrainingSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Izveidojam Majority klasifikatoru
			MajorityKlasifikators majModel = new MajorityKlasifikators();
			majModel.buildClassifier(isTrainingSet);
			
			// Notestē modeļus un pieglabā rezultātus
			
			double[][] bayesMultiResult = testModelConfusion(nbModel, isTestSet);
			double[][] svmResult = testModelConfusion(svmModel, isTestSet);
			double[][] majResult = testModelConfusion(majModel, isTestSet);
			
			bayesMultiResults = addConfusionMatrix(bayesMultiResults, bayesMultiResult);
			svmResults =  addConfusionMatrix(svmResults, svmResult);
			majResults =  addConfusionMatrix(majResults, majResult);
			
		}//for (int iteracija = 0; iteracija < 10; iteracija++){

		System.out.println("Bayes total results: " + Arrays.deepToString(bayesMultiResults));
		System.out.println("Accuracy " + getAccuracy(bayesMultiResults)+ " F1 " + getF1(bayesMultiResults));
		System.out.println("SVM total results: "+ Arrays.deepToString(svmResults));
		System.out.println("Accuracy " + getAccuracy(svmResults)+ " F1 " + getF1(svmResults));
		System.out.println("Majority total results: "+ Arrays.deepToString(majResults));
		System.out.println("Accuracy " + getAccuracy(majResults)+ " F1 " + getF1(majResults));
	}
	
	public static List <Node> getTweets(){
		List <Node> result = new ArrayList<Node>();
		String[] files = {"hcr/train.xml", "hcr/dev.xml", "hcr/test.xml"};
		try {
			for (String string : files) {
				File fXmlFile = new File(string);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("item");

				// Lietosim tikai tvītus ar sentimentu "positive", "negative", "neutral" vai arī tikai "positive", "negative"
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					Element eElement = (Element) nNode;
					if ( tweetSentiments.contains( eElement.getAttribute("label") ) )
					{
						result.add(nNode);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<String> getUniqueUnigrams(String content){

		List<String> cleanedUnigrams = getUnigrams(content);
		
		Set<String> uniqueUnigrams = new HashSet<String>(cleanedUnigrams);
//		System.out.println("Total unigrams: " + cleanedUnigrams.size());
//		System.out.println("Unique unigrams: " + uniqueUnigrams.size());
		List<String> allUniqueUnigrams = new ArrayList<String>(uniqueUnigrams);
		return allUniqueUnigrams;
	}
	
	public static List<String> getUnigrams(String content){
		String[] allUnigrams = content.split("\\s+");
		List<String> cleanedUnigrams = new ArrayList<String>();
		for (String unigram : allUnigrams) {
			// replace words containing # @ and links with placeholders 
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
			unigram = unigram.toLowerCase();
			unigram = unigram.replaceAll("[^A-Za-z0-9]", "");
			cleanedUnigrams.add(unigram);
		}
		
		return cleanedUnigrams;
	}

	public static Instances generateSet(String title,  List <Node> nodeList, List<String> allUniqueUnigrams){

		Attribute tweetSentiment = new Attribute("sentiment", tweetSentiments);
		
		// Izveido feature vector 
		FastVector tweetAttributes = new FastVector(allUniqueUnigrams.size() + 1);
		System.out.println("Declare the feature vector...");
		for (String unigram : allUniqueUnigrams) {
			Attribute attribute = new Attribute(unigram);
			tweetAttributes.add(attribute);
		}
		tweetAttributes.add(tweetSentiment);
		
		// Izveido jaunu piemēru kopu
		Instances iSet = new Instances(title, tweetAttributes, nodeList.size());
		// Set class index - Let it be last one
		iSet.setClassIndex(allUniqueUnigrams.size() );
		System.out.println("Create instances set...");
		for (Node node : nodeList) {
			Element eNode = (Element) node;
			String label = eNode.getAttribute("label");
			String content = eNode.getElementsByTagName("content").item(0).getTextContent();
			
			List<String> tweetUniqueUnigrams = getUniqueUnigrams(content);
			List<String> tweetUnigrams = getUnigrams(content);
			// Izveido piemēru
			Instance tweetInstance = new SparseInstance(allUniqueUnigrams.size()+1);
			for (int i = 0; i < allUniqueUnigrams.size(); i++) {
				int occurences = Collections.frequency(tweetUnigrams, allUniqueUnigrams.get(i));
				tweetInstance.setValue((Attribute)tweetAttributes.elementAt(i), occurences);
//				boolean contains = tweetUniqueUnigrams.contains(allUniqueUnigrams.get(i));
//				if (contains){
//					tweetInstance.setValue((Attribute)tweetAttributes.elementAt(i), 1);
//				} else {
//					tweetInstance.setValue((Attribute)tweetAttributes.elementAt(i), 0);
//				}
			}
			// Pievieno klases atribūtu pēdējā pozīcijā
			tweetInstance.setValue((Attribute)tweetAttributes.elementAt(allUniqueUnigrams.size()), label);
			// Pievieno piemēru kopai
			iSet.add(tweetInstance);
//			System.out.println(content);
//			System.out.println(tweetInstance);
		}
		return iSet;
	}
	
	/**
	 * Lieto lai pieskaitītu confusion matricu pēc katras iterācijas
	 * */
	public static double[][] addConfusionMatrix(double[][] total, double[][] addition){
		int rows = total.length;
		for (int i = 0; i < rows; i++) {
			int columns = total[i].length;
			for (int j = 0; j < columns; j++) {
				total[i][j] += addition[i][j];
			}
		}
		return total;
	}
	
	/**
	 * Pārbauda modeli, izveido confusion matricu
	 */
	public static double[][] testModelConfusion(Classifier model, Instances instances){
		int classes = instances.classAttribute().numValues();
		int actual;		// ROW
		int prognosis;	// COLUMN
		double[][] result = new double[classes][classes];
		
        for (int row = 0; row < classes; row ++){
        	for (int col = 0; col < classes; col++){
        		result[row][col] = 0;
        	}
        }
		
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
			try {
				prognosis = (int)model.classifyInstance(instance);
				actual = (int)instance.classValue();
//				System.out.println(instances.classAttribute().value((int) prognosis));
//				System.out.println("Predicted: " + prognosis + " Actual: "+ instance.classValue());
				result[actual][prognosis] = result[actual][prognosis] + 1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		return result;
	}
	
	/**
	 * Aprēķina precizitāti
	 */
	public static double getAccuracy(double[][] matrix){
		double accuracy = 0;
		double correct = 0; 
		double total = 0;
		int rows = matrix.length;
		int columns = matrix.length;
		
        for (int row = 0; row < rows; row ++){
        	for (int col = 0; col < columns; col++){
        		if (row == col ){
        			correct = correct + matrix[row][col];
        		}
        		total = total + matrix[row][col];
        	}
        }
        accuracy = (total == 0) ? 0 : correct / total ;
		return accuracy;
	}
	
	/**
	 * Aprēķina F1
	 * https://stats.stackexchange.com/questions/51296/how-do-you-calculate-precision-and-recall-for-multiclass-classification-using-co
	 */
		public static double getF1(double[][] matrix){
		double f1 = 0;
		int rows = matrix.length; //actual;
		int columns = matrix.length; // predicted
		double precision = 0;
		double recall = 0;
		
        for (int row = 0; row < rows; row ++){			// Katrai rindai atrod precision, recall
        	double truePositive = 0;
        	double tPFP = 0;
        	double tPFN = 0;
        	for (int col = 0; col < columns; col++){
        		if (row == col){
        			truePositive = matrix[row][col];
        		}
        		tPFP = tPFP + matrix[col][row];			// TP+FP atrod sasummējot kolonnas (prediction) elementus
        		tPFN = tPFN + matrix[row][col];			// TP+FN atrod sasummējot rindas (actual) elementus
        	}
        	precision = (tPFP == 0) ? precision : precision + truePositive / tPFP ; // Sasummē visas precizitātes
        	recall = (tPFN == 0) ? recall : recall + truePositive / tPFN ;
        }
        precision = precision / rows; // Atrod vidējo precizitāti
        recall = recall / rows;
        
//        System.out.println("Precision " + precision + " Recall "+ recall);
        
		f1 = 2 * precision * recall / (precision + recall);
		return f1;
	}
}
