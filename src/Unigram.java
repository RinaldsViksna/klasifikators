import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servisa klase, pašlaik tikai sadala stringus unigramos
 */
public class Unigram {
	
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
			// remove empty unigrams
//			if ( unigram.equals("") ){
//				continue;
//			}
			cleanedUnigrams.add(unigram);
		}
		
		return cleanedUnigrams;
	}
}
