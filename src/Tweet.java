import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Klase reprezentēs twītus, iespējams no dažādiem avotiem
 */
public class Tweet {
	private double tweetid;	// ID no twittera
	private String label;	// Noskaņojums
	private String content;	// Twīta teksts
	private String target;	// 
	private String username;// Lietotājs, kas tvīto
	
	public Tweet(Node node){
		Element eNode = (Element) node;
//		this.tweetid = Double.parseDouble(eNode.getAttribute("tweetid"));
		this.label = eNode.getAttribute("label");
		this.content = eNode.getElementsByTagName("content").item(0).getTextContent();
		this.target = eNode.getAttribute("target");
		this.username = eNode.getAttribute("username");
		
	}
	public double getTweetid() {
		return tweetid;
	}
	public String getLabel() {
		return label;
	}
	public String getContent() {
		return content;
	}
	public String getTarget() {
		return target;
	}
	public String getUsername() {
		return username;
	}
	
	
	

}
