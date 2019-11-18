package twi_classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import twitter4j.MediaEntity;
import twitter4j.Status;

//@Path("/twiservice")
@Path("")
public class RestService {
	static ConfigureFile configureFile=null;
	static ConfigNRetrieve twitterRetrieval=null;
	static Thread t1;
	  
	  @GET
	  @Produces("application/json")
	  @Path("/begin")
	  public Response begin() throws JSONException {
		  if(twitterRetrieval!=null) return Response.status(500).entity("[Get-method begin] has already been called.").build();
		  
		  t1 = new Thread(new ConfigNRetrieve ());
		  t1.start();  
		JSONObject jsonObject = new JSONObject();
		twitterRetrieval= new ConfigNRetrieve();
		jsonObject.put("Status:", "The Social-Media Adapter begun to retrieve tweets"); 
		String result = "@Produces(\"application/json\") \n\n" + jsonObject;	
		//System.out.println("begin tweets:"+ConfigNRetrieve.tweets);
		System.out.println("End of begin method");
		return Response.status(200).entity(result).build();
	  }
	   
	  @POST
	  @Path("/retrieve")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public Response retrieveTweets(String input) throws IOException, JSONException {
		  if(twitterRetrieval==null) return Response.status(500).entity("Call [Get-method begin] or just wait a few seconds to initialize.").build();
		  Search req = new Gson().fromJson(input, Search.class);
		  List<String[]> keywordsList = new ArrayList<String[]>();
		  for(int i=0; i<req.keywords.length; i++){
				keywordsList.add((req.keywords)[i].split("\\|"));
		  }
		  String placeString = (req.place);
		  String logic = req.logic;
		  
		  List<JSONObject> listJSONobj = new ArrayList<JSONObject>();
		  System.out.println("The number of tweets is: "+ConfigNRetrieve.tweets.size());
		  
		  for (Status tweet : ConfigNRetrieve.tweets) {
				String myKeyWord="";
				Boolean TweetContainKeywords =true;
				String actualKeyword = "";
				if(logic.equals("and")){
					
					 for(String[] keyWords: keywordsList){	
						
						int found=0;
						for(String keywordString: keyWords) {
							boolean allCapitals = false;
							String tweetText = tweet.getText();
							String myKeywordString = keywordString.trim();
							if (keywordString.toLowerCase().indexOf(" (aka")>=0 || keywordString.toLowerCase().indexOf(" (acronym")>=0){
								myKeywordString = keywordString.split("\\(")[0].trim();
							}
							if(myKeywordString.toUpperCase().equals(myKeywordString)) allCapitals=true;
							if(!allCapitals){
								myKeywordString = myKeywordString.toLowerCase();
								tweetText = tweetText.toLowerCase();
							}
								
							if(tweetText.contains(myKeywordString) && (tweetText.indexOf(myKeywordString)==0 || (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z')) && (!allCapitals || tweetText.contains(" "+myKeywordString+" "))){	
								found=1;
								if(myKeyWord.equals("")) myKeyWord+=keywordString;
								else{
									String[] myKeys = myKeyWord.split("\\|[ ]*");
									Boolean exists = false;
									for(int keys=0; keys<myKeys.length; keys++){
										if(keywordString.equalsIgnoreCase(myKeys[keys])){
											exists = true;
											break;
										}
									}
									if(!exists) myKeyWord+="| "+keywordString;
								}
								if(actualKeyword.equals("")) actualKeyword+=keywordString;
								else actualKeyword+="| "+keywordString;
								break;
							}
							if(!allCapitals){
								Stemmer s = new Stemmer(); 
								char[] stemming = myKeywordString.toCharArray();
								for(int st=0; st<stemming.length; st++){
									s.add(stemming[st]);
								}
								s.stem();
								String u = s.toString();
								if(tweetText.contains(u) && (tweetText.indexOf(u)==0 || (tweetText.charAt(tweetText.indexOf(u)-1)<'a' || tweetText.charAt(tweetText.indexOf(u)-1)>'z') && (tweetText.charAt(tweetText.indexOf(u)-1)<'A' || tweetText.charAt(tweetText.indexOf(u)-1)>'Z'))){	
									found=1;
									if(myKeyWord.equals("")) myKeyWord+=keywordString;
									else{
										String[] myKeys = myKeyWord.split("\\|[ ]*");
										Boolean exists = false;
										for(int keys=0; keys<myKeys.length; keys++){
											if(keywordString.equalsIgnoreCase(myKeys[keys])){
												exists = true;
												break;
											}
										}
										if(!exists) myKeyWord+="| "+keywordString;
									}
									if(actualKeyword.equals("")) actualKeyword+=u;
									else actualKeyword+="| "+u;
									break;
								}
							}
						}
						if (found==1)
							continue;
						else{
							TweetContainKeywords=false;
							break;
						}
					}
				}
				 else{
				  
						int found=0;
						for(String[] keyWords: keywordsList){
							
							for(String keywordString: keyWords) {
								boolean allCapitals = false;
								String tweetText = tweet.getText();
								String myKeywordString = keywordString.trim();
								if (keywordString.toLowerCase().indexOf(" (aka")>=0 || keywordString.toLowerCase().indexOf(" (acronym")>=0){
									myKeywordString = keywordString.split("\\(")[0].trim();
								}
								if(myKeywordString.toUpperCase().equals(myKeywordString)) allCapitals=true;
								if(!allCapitals){
									myKeywordString = myKeywordString.toLowerCase();
									tweetText = tweetText.toLowerCase();
								}
								if(tweetText.contains(myKeywordString) && (tweetText.indexOf(myKeywordString)==0 || (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z')) && (!allCapitals || tweetText.contains(" "+myKeywordString+" "))){	
									found=1;
									myKeyWord=keywordString;
									actualKeyword=keywordString;
									break;
								}
								if(!allCapitals){
									Stemmer s = new Stemmer(); 
									char[] stemming = myKeywordString.toCharArray();
									for(int st=0; st<stemming.length; st++){
										s.add(stemming[st]);
									}
									s.stem();
									String u = s.toString();
									if(tweetText.contains(u) && (tweetText.indexOf(u)==0 || (tweetText.charAt(tweetText.indexOf(u)-1)<'a' || tweetText.charAt(tweetText.indexOf(u)-1)>'z') && (tweetText.charAt(tweetText.indexOf(u)-1)<'A' || tweetText.charAt(tweetText.indexOf(u)-1)>'Z'))){	
										found=1;
										myKeyWord=keywordString;
										actualKeyword=u;
										break;
									}
								}
							}
							if (found==1) break;
						}
						if (found==0) TweetContainKeywords=false;
				 }
					
					if(placeString.length()>1)
						if(!(tweet.getUser().getLocation()).toLowerCase().contains(placeString.toLowerCase())) TweetContainKeywords=false;
					
					if(TweetContainKeywords) {
						MediaEntity[] media = tweet.getMediaEntities(); //get the media entities from the status
						String images="";
						for(MediaEntity m : media){  //search trough your entities
							images+=(m.getMediaURL())+" "; } //get your url!
						
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("source", tweet.getSource()); 
						jsonObject.put("sourceImg", tweet.getUser().getBiggerProfileImageURL());
						jsonObject.put("poster", tweet.getUser().getName());
						jsonObject.put("postDate", tweet.getCreatedAt());
						String[] myKeywordsArray = actualKeyword.split("\\|[ ]*");
						String tweetText = tweet.getText();
						for(String aKeyword: myKeywordsArray){
							boolean allCapitals = false;
							if (aKeyword.toLowerCase().indexOf(" (aka")>=0 || aKeyword.toLowerCase().indexOf(" (acronym")>=0){
								aKeyword = aKeyword.split("\\(")[0].trim();
							}
							if(aKeyword.toUpperCase().equals(aKeyword)) allCapitals=true;
							else{
								boolean substring =false;
								for(int i=0; i<myKeywordsArray.length; i++){
									if (aKeyword.equals(myKeywordsArray[i])) continue;
									else{
										if (myKeywordsArray[i].toLowerCase().contains(aKeyword.toLowerCase()) && !myKeywordsArray[i].toLowerCase().contains("aka") && !myKeywordsArray[i].toLowerCase().contains("acronym")){
											substring=true;
											break;
										}
									}
								}
								if (substring==true) continue;
							}
							
							int startIndex, endIndex;
							
							if(!allCapitals){
								
								startIndex = tweetText.toLowerCase().indexOf(aKeyword.toLowerCase());
								
							}
							else startIndex = tweetText.indexOf(aKeyword);
							endIndex = startIndex + aKeyword.length();
							while(endIndex<tweetText.length() && ((tweetText.charAt(endIndex)>='a' && tweetText.charAt(endIndex)<='z') || (tweetText.charAt(endIndex)>='A' && tweetText.charAt(endIndex)<='Z'))) endIndex++;
							tweetText = tweetText.substring(0,startIndex) + "<mark>" + tweetText.substring(startIndex,endIndex) + "</mark>" + tweetText.substring(endIndex,tweetText.length());
						}
						jsonObject.put("postText", tweetText);
						jsonObject.put("postPhoto", images);
						jsonObject.put("keyword", myKeyWord.replace('|', ','));
						listJSONobj.add(jsonObject);
					}
				}			
			String result = "@Produces(\"application/json\") Output: \n\nF to C-Results Converter Output: \n\n" + listJSONobj;
			return Response.status(200).entity(result).build();
	  }//============================== End of retrieveTweets() ===============================	  

	  @POST
	  @Path("/configure")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  public static Response configure(String input) throws IOException, JSONException {
		  if(twitterRetrieval!=null) return Response.status(500).entity("You can configure the SMA adapter before the Call [Get-method begin].").build();
		  
		  JSONObject jo = new JSONObject(input);
		  
		  configureFile = new ConfigureFile();
		  ConfigureFile.setAuthConsumerKey(jo.getString("AuthConsumerKey")) ;
		  ConfigureFile.setAuthConsumerSecret(jo.getString("AuthConsumerSecret")) ;
		  ConfigureFile.setAuthAccessToken(jo.getString("AuthAccessToken")) ;
		  ConfigureFile.setAccessTokenSecret(jo.getString("AccessTokenSecret")) ;
		  ConfigureFile.setTimedelay(Long.parseLong(jo.getString("TimeDelay")));
		  ConfigureFile.setMax_tweets(Long.parseLong(jo.getString("Max_Tweets")));
		  ConfigureFile.setArrayKeyWords((jo.getString("KeyWords").split(",")));

	      JSONObject jsonObject = new JSONObject();
		  jsonObject.put("Configuration", "Success"); 
		  System.out.println("The data are: "+ConfigureFile.getAccessTokenSecret()+" "+ConfigureFile.getAuthAccessToken()+" "+ConfigureFile.getAuthConsumerKey()+" "+ConfigureFile.getAuthConsumerSecret());
		  System.out.println(ConfigureFile.getMax_tweets()+" "+ConfigureFile.getTimedelay()+" "+ConfigureFile.getArrayKeyWords()[1]);
		  String result = "@Produces(\"application/json\") Output: \n\nF to C-Results Converter Output: \n\n" + jsonObject;
		  return Response.status(200).entity(result).build();
	  }	  
}
