package twi_classes;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

import com.google.gson.Gson;

import twitter4j.MediaEntity;
import twitter4j.Status;

/**
 * Servlet implementation class TwitterServlet
 */
public class TwitterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int flag=0;  
	private static String stopwords = "i,me,my,myself,we,our,ours,ourselves,you,your,yours,yourself,yourselves,he,him,his,himself,she,her,hers,herself,it,its,itself,they,them,their,theirs,themselves,what,which,who,whom,never,this,that,these,those,am,is,are,was,were,be,been,being,have,has,had,having,do,does,did,doing,a,an,the,and,but,if,kung,or,because,as,until,while,of,at,by,for,with,about,against,between,into,through,during,before,after,above,below,to,from,up,down,in,out,on,off,over,under,again,further,then,once,here,there,when,where,why,how,long,all,any,both,each,few,more,delivering,most,other,some,such,no,nor,not,only,own,same,so,than,too,cry,very,s,t,can,lite,will,just,don,should,now";

	static ConfigureFile configureFile=null;
	static ConfigNRetrieve twitterRetrieval=null;
	static Thread t1;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TwitterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private int minDistance(String word1, String word2){
    	int distance=0;
    	char[] charword1 = word1.toCharArray();
    	char[] charword2 = word2.toCharArray();
    	if (charword1.length==charword2.length){
    		for(int i=0; i<charword1.length; i++){
    			if(charword1[i]==charword2[i] || charword1[i]+32==charword2[i] || charword1[i]-32==charword2[i]) continue;
    			else{
    				distance++;
    				if(distance>1) return distance;
    			}
    		}
    		return distance;
    	}
    	else return 2;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if(twitterRetrieval!=null){
			PrintWriter pw = response.getWriter();
			pw.print("[Get-method begin] has already been called.");
			pw.close();
		}
		else{
			try {
				t1 = new Thread(new ConfigNRetrieve ());
				  t1.start();  
				JSONObject jsonObject = new JSONObject();
				twitterRetrieval= new ConfigNRetrieve();
				jsonObject.put("Status:", "The Social-Media Adapter begun to retrieve tweets");
				System.out.println("End of begin method");
				PrintWriter pw = response.getWriter();
				pw.print(jsonObject.toString());
				pw.close();
				System.out.println(jsonObject.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			//System.out.println("begin tweets:"+ConfigNRetrieve.tweets);
			
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Produces("application/json; charset=UTF-8")
	@Consumes("application/json; charset=UTF-8")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (flag==0){
			flag=1;
			if(twitterRetrieval!=null){
				PrintWriter pw = response.getWriter();
				pw.print("You can configure the SMA adapter before the Call [Get-method begin].");
				pw.close();
			}
			else{
			try {
				Configurations config = new Gson().fromJson(request.getReader(), Configurations.class);

				configureFile = new ConfigureFile();
				  ConfigureFile.setAuthConsumerKey(config.AuthConsumerKey);
				  ConfigureFile.setAuthConsumerSecret(config.AuthConsumerSecret);
				  ConfigureFile.setAuthAccessToken(config.AuthAccessToken);
				  ConfigureFile.setAccessTokenSecret(config.AccessTokenSecret);
				  ConfigureFile.setTimedelay(Long.parseLong(config.TimeDelay));
				  ConfigureFile.setMax_tweets(Long.parseLong(config.Max_Tweets));
				  ConfigureFile.setArrayKeyWords((config.KeyWords).split(","));
				  

			      JSONObject jsonObject = new JSONObject();
				  jsonObject.put("Configuration", "Success"); 
				  System.out.println("The data are: "+ConfigureFile.getAccessTokenSecret()+" "+ConfigureFile.getAuthAccessToken()+" "+ConfigureFile.getAuthConsumerKey()+" "+ConfigureFile.getAuthConsumerSecret());
				  System.out.println(ConfigureFile.getMax_tweets()+" "+ConfigureFile.getTimedelay()+" "+ConfigureFile.getArrayKeyWords()[3]);
				  PrintWriter pw = response.getWriter();
				  pw.print(jsonObject.toString());
				  pw.close();
				  System.out.println(jsonObject.toString());
			}catch (Exception e) {
			   	System.out.println(e);
			}
			}
		}
		else{
			if(twitterRetrieval==null){
				PrintWriter pw = response.getWriter();
				pw.print("Call [Get-method begin] or just wait a few seconds to initialize.");
				pw.close();
			}
			else{
			
				try {
					Search req = new Gson().fromJson(request.getReader(), Search.class);
					List<String[]> keywordsList = new ArrayList<String[]>();
					//List<String[]> languageList = new ArrayList<String[]>();
					for(int i=0; i<req.keywords.length; i++){
						keywordsList.add((req.keywords)[i].split("\\|"));
						//languageList.add((req.languages)[i].split("\\|"));
					}
					/*String[] mykeywords = new String[req.mykeywords.length];
					for(int i=0; i<req.mykeywords.length; i++){
						mykeywords[i]=(req.mykeywords)[i];
					}*/
				  String placeString = (req.place);
				  String logic = req.logic;
				
				  List<JSONObject> listJSONobj = new ArrayList<JSONObject>();
				  System.out.println("The number of tweets is: "+ConfigNRetrieve.tweets.size());
				  
				for (Status tweet : ConfigNRetrieve.tweets) {
				  //if(tweet.getLang().equals("en")){
					String myKeyWord="";
					Boolean TweetContainKeywords =true;
					//List<Boolean> isStemmed = new ArrayList<Boolean>();
					List<List<Boolean>> isStemmed = new ArrayList<List<Boolean>>();
					String actualKeyword = "";
					
					if(logic.equals("and")){
						
						 for(String[] keyWords: keywordsList){	
							List<Boolean> keywordStemmed = new ArrayList<Boolean>();
							int found=0;
							/*for(String keywordString: keyWords) {
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
									
								//if(tweetText.contains(" "+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+",") || tweetText.contains(" "+myKeywordString+".") || tweetText.contains("\n"+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+"\n") || tweetText.contains("\n"+myKeywordString+"\n") || tweetText.contains("-"+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+"-")){
								if(tweetText.contains(myKeywordString) && (!allCapitals || ((tweetText.indexOf(myKeywordString)==0 || ((tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z'))) && (tweetText.indexOf(myKeywordString)+myKeywordString.length()==tweetText.length() || ((tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'Z')))))){ //&& ( !allCapitals  || tweetText.contains(" "+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+",") || tweetText.contains(" "+myKeywordString+"."))){	
									found=1;
									if(myKeyWord.equals("")) {myKeyWord+=keywordString; isStemmed.add(false);}
									else{
										String[] myKeys = myKeyWord.split("\\|[ ]*");
										Boolean exists = false;
										for(int keys=0; keys<myKeys.length; keys++){
											if(keywordString.equalsIgnoreCase(myKeys[keys])){
												exists = true;
												break;
											}
										}
										if(!exists) {myKeyWord+="| "+keywordString; isStemmed.add(false);}
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
									if(tweetText.contains(u)){ //&& (tweetText.indexOf(u)==0 || (tweetText.charAt(tweetText.indexOf(u)-1)<'a' || tweetText.charAt(tweetText.indexOf(u)-1)>'z') && (tweetText.charAt(tweetText.indexOf(u)-1)<'A' || tweetText.charAt(tweetText.indexOf(u)-1)>'Z'))){	
										found=1;
										if(myKeyWord.equals("")) {myKeyWord+=keywordString; isStemmed.add(true);}
										else{
											String[] myKeys = myKeyWord.split("\\|[ ]*");
											Boolean exists = false;
											for(int keys=0; keys<myKeys.length; keys++){
												if(keywordString.equalsIgnoreCase(myKeys[keys])){
													exists = true;
													break;
												}
											}
											if(!exists) {myKeyWord+="| "+keywordString; isStemmed.add(true);}
										}
										if(actualKeyword.equals("")) actualKeyword+=u;
										else actualKeyword+="| "+u;
										break;
									}
								}
							}
							if (found==1)
								continue;
							else{*/
								String tweetText = tweet.getText();
								String[] tweetwords = tweetText.split(" [#]*");
								for(String keywordString: keyWords) {
									
									boolean allCapitals = false;
									String myKeywordString = keywordString.trim();
									if (keywordString.toLowerCase().indexOf(" (aka")>=0 || keywordString.toLowerCase().indexOf(" (acronym")>=0){
										myKeywordString = keywordString.split("\\(")[0].trim();
									}
									//if(myKeywordString.toUpperCase().equals(myKeywordString)) allCapitals=true;
									if(minDistance(myKeywordString,myKeywordString.toUpperCase())<=1) allCapitals=true;
									if(allCapitals){
										if(tweetText.contains(myKeywordString) && ((tweetText.indexOf(myKeywordString)==0 || ((tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z'))) && (tweetText.indexOf(myKeywordString)+myKeywordString.length()==tweetText.length() || ((tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'Z'))))){ //&& ( !allCapitals  || tweetText.contains(" "+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+",") || tweetText.contains(" "+myKeywordString+"."))){	
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
										if (keywordString.toLowerCase().indexOf(" (aka")>=0 || keywordString.toLowerCase().indexOf(" (acronym")>=0){
											continue;
										}
										
									}
									myKeywordString = myKeywordString.toLowerCase();
									if(stopwords.contains(","+myKeywordString+",")) continue;
									if(tweetText.toLowerCase().contains(myKeywordString)){ //&& (!allCapitals || ((tweetText.indexOf(myKeywordString)==0 || ((tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z'))) && (tweetText.indexOf(myKeywordString)+myKeywordString.length()==tweetText.length() || ((tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'Z')))))){ //&& ( !allCapitals  || tweetText.contains(" "+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+",") || tweetText.contains(" "+myKeywordString+"."))){	
										found=1;
										if(myKeyWord.equals("")) myKeyWord+=myKeywordString;
										else{
											String[] myKeys = myKeyWord.split("\\|[ ]*");
											Boolean exists = false;
											for(int keys=0; keys<myKeys.length; keys++){
												if(keywordString.equalsIgnoreCase(myKeys[keys])){
													exists = true;
													break;
												}
											}
											if(!exists) myKeyWord+="| "+myKeywordString;
										}
										if(actualKeyword.equals("")) actualKeyword+=myKeywordString;
										else actualKeyword+="| "+myKeywordString;
										break;
									}
									//String[] wordsOfKeywordString = myKeywordString.split(" ");
									List<String> tempwords = new ArrayList<String>(Arrays.asList(myKeywordString.toLowerCase().split(" ")));
									for(int j=0; j<tempwords.size(); j++){
										if(stopwords.contains(","+tempwords.get(j)+",")) tempwords.remove(j--);  
									}
									String[] wordsOfKeywordString = new String[tempwords.size()]; 
									for(int j=0; j<tempwords.size(); j++){
										wordsOfKeywordString[j] = tempwords.get(j);
									}
									boolean aWordNotFound=true;
									String[] actualWords = new String[wordsOfKeywordString.length];
									int wordscounter =0;
									for(String aWord: wordsOfKeywordString){
										/*if(!allCapitals){
											Stemmer s = new Stemmer(); 
											char[] stemming = aWord.toCharArray();
											for(int st=0; st<stemming.length; st++){
												s.add(stemming[st]);
											}
											s.stem();
											aWord = s.toString();
										}*/
										
										aWordNotFound=true;
										for(String aTweetWord: tweetwords){
											aTweetWord = aTweetWord.toLowerCase();
											if(aTweetWord.contains(",")){
												String[] myaWord = aTweetWord.split(",");
												aTweetWord=myaWord[0];	
											}
											if(stopwords.contains(","+aTweetWord+",")) continue;
											if(minDistance(aTweetWord,aWord)<=1){
												aWordNotFound=false;
												actualWords[wordscounter++]=aTweetWord;
												//keywordStemmed.add(false);
												break;
											}
											else if(aTweetWord.contains(aWord)){ //|| ed.minDistance(aTweetWord.toLowerCase(), aWord.toLowerCase())==1))){
												aWordNotFound=false;
												actualWords[wordscounter++]=aWord;
												//keywordStemmed.add(false);
												break;
											}
											else{
												/*if(aTweetWord.contains(",")){
													String[] myaWord = aTweetWord.split(",");
													aTweetWord=myaWord[0];	
												}*/
												/*if(aTweetWord.contains("#")){
													String[] myaWord = aTweetWord.split("#");
													aTweetWord=myaWord[1];	
												}*/
												String actualTweetWord = aTweetWord;
												if(aWord.contains("-")){
													String[] myaWord = aWord.split("-");
													aWord="";
													for(int k=0; k<myaWord.length; k++) aWord+=myaWord[k];					
												}
												if(aWord.contains("\'")){
													String[] myaWord = aWord.split("\'");
													aWord = myaWord[0];
													//aWord="";
													//for(int k=0; k<myaWord.length; k++) aWord+=myaWord[k];					
												}
												if(aTweetWord.contains("-")){
													String[] myaWord = aTweetWord.split("-");
													aTweetWord="";
													for(int k=0; k<myaWord.length; k++) aTweetWord+=myaWord[k];					
												}
												if(aTweetWord.contains("\'")){
													String[] myaWord = aTweetWord.split("\'");
													aTweetWord = myaWord[0];
													//aTweetWord="";
													//for(int k=0; k<myaWord.length; k++) aTweetWord+=myaWord[k];					
												}
												
												if (minDistance(aWord,aTweetWord)<=1 || aTweetWord.contains(aWord)){
													aWordNotFound=false;
													actualWords[wordscounter++]=actualTweetWord;
													//keywordStemmed.add(false);
													break;
												}
											}
										}
										if (aWordNotFound){
											//if(!allCapitals){
												Stemmer s = new Stemmer(); 
												char[] stemming = aWord.toCharArray();
												for(int st=0; st<stemming.length; st++){
													s.add(stemming[st]);
												}
												s.stem();
												aWord = s.toString();
												
												for(String aTweetWord: tweetwords){
													aTweetWord = aTweetWord.toLowerCase();
													if(aTweetWord.contains(",")){
														String[] myaWord = aTweetWord.split(",");
														aTweetWord=myaWord[0];	
													}
													if(stopwords.contains(","+aTweetWord+",")) continue;
												/*if(aTweetWord.equals(aWord) || (aTweetWord.toLowerCase().contains(aWord.toLowerCase()))){ //|| ed.minDistance(aTweetWord.toLowerCase(), aWord.toLowerCase())==1))){
													aWordNotFound=false;
													actualWords[wordscounter++]=aTweetWord;
													//keywordStemmed.add(true);
													break;
												}*/
													if(minDistance(aTweetWord,aWord)<=1 || aTweetWord.contains(aWord)){
														aWordNotFound=false;
														actualWords[wordscounter++]=aTweetWord;
														//keywordStemmed.add(false);
														break;
													}
													/*else if(aTweetWord.contains(aWord)){ //|| ed.minDistance(aTweetWord.toLowerCase(), aWord.toLowerCase())==1))){
														aWordNotFound=false;
														actualWords[wordscounter++]=aWord;
														//keywordStemmed.add(false);
														break;
													}*/
												else{
													/*if(aTweetWord.contains(",")){
														String[] myaWord = aTweetWord.split(",");
														aTweetWord=myaWord[0];	
													}*/
													String actualTweetWord = aTweetWord;
													/*if(aWord.contains("-")){
														String[] myaWord = aWord.split("-");
														aWord="";
														for(int k=0; k<myaWord.length; k++) aWord+=myaWord[k];					
													}*/
													if(aTweetWord.contains("-")){
														String[] myaWord = aTweetWord.split("-");
														aTweetWord="";
														for(int k=0; k<myaWord.length; k++) aTweetWord+=myaWord[k];					
													}
													/*if(aWord.contains("\'")){
														String[] myaWord = aWord.split("\'");
														aWord="";
														for(int k=0; k<myaWord.length; k++) aWord+=myaWord[k];					
													}*/
													
													if (minDistance(aWord,aTweetWord)<=1 || aTweetWord.contains(aWord)){
														aWordNotFound=false;
														actualWords[wordscounter++]=actualTweetWord;
														//keywordStemmed.add(true);
														break;
													}
												}
											} 
										//}
										}
										
										if (aWordNotFound) break; 
									}
									if(aWordNotFound){
										//keywordStemmed.clear();
										continue;
									}
									else{
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
										for(String anActual: actualWords){
											//isStemmed.add(true);
											if(actualKeyword.equals("")) actualKeyword+=anActual;
											else if(!actualKeyword.toLowerCase().contains(anActual.toLowerCase())) actualKeyword+="| "+anActual;
										}
										//isStemmed.add(keywordStemmed);
										break;
									}
								}
								if(found==1) continue;
								else{
									TweetContainKeywords=false;
									break;
								}
							//}
						}
				}
					 /*else{
					  
							int found=0;
							int langlist=0;
							for(String[] keyWords: keywordsList){
								int langcount = 0;
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
									if(tweetText.contains(myKeywordString) && (!allCapitals || ((tweetText.indexOf(myKeywordString)==0 || ((tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)-1)<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)-1)>'Z'))) && (tweetText.indexOf(myKeywordString)+myKeywordString.length()==tweetText.length() || ((tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'a' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'z') && (tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())<'A' || tweetText.charAt(tweetText.indexOf(myKeywordString)+myKeywordString.length())>'Z')))))){ //&& ( !allCapitals  || tweetText.contains(" "+myKeywordString+" ") || tweetText.contains(" "+myKeywordString+",") || tweetText.contains(" "+myKeywordString+"."))){	
										found=1;
										myKeyWord=keywordString;
										isStemmed.add(false);
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
										if(tweetText.contains(u)){// && (tweetText.indexOf(u)==0 || (tweetText.charAt(tweetText.indexOf(u)-1)<'a' || tweetText.charAt(tweetText.indexOf(u)-1)>'z') && (tweetText.charAt(tweetText.indexOf(u)-1)<'A' || tweetText.charAt(tweetText.indexOf(u)-1)>'Z'))){	
											found=1;
											myKeyWord=keywordString;
											isStemmed.add(true);
											actualKeyword=u;
											break;
										}
									}
								}
								if (found==1) break;
								else{
									String tweetText = tweet.getText();
									String[] tweetwords = tweetText.split(" ");
									for(String keywordString: keyWords) {
										boolean allCapitals = false;
										String myKeywordString = keywordString.trim();
										if ((keywordString.toLowerCase().indexOf(" (aka")>=0) || keywordString.toLowerCase().indexOf(" (acronym")>=0){
											myKeywordString = keywordString.split("\\(")[0].trim();
											
										}
										if(myKeywordString.toUpperCase().equals(myKeywordString)) allCapitals=true;
										if(allCapitals) continue;
										List<String> tempwords = new ArrayList<String>(Arrays.asList(myKeywordString.toLowerCase().split(" ")));
										for(int j=0; j<tempwords.size(); j++){
											if(stopwords.contains(","+tempwords.get(j)+",")) tempwords.remove(j--);  
										}
										String[] wordsOfKeywordString = new String[tempwords.size()]; 
										for(int j=0; j<tempwords.size(); j++){
											wordsOfKeywordString[j] = tempwords.get(j);
										}
										boolean aWordNotFound=true;
										String[] actualWords = new String[wordsOfKeywordString.length];
										int wordscounter =0;
										for(String aWord: wordsOfKeywordString){
											
											aWordNotFound=true;
											for(String aTweetWord: tweetwords){
												if(stopwords.contains(","+aTweetWord+",")) continue;
												if(aTweetWord.equals(aWord) || (aTweetWord.toLowerCase().contains(aWord.toLowerCase()))){ //|| ed.minDistance(aTweetWord.toLowerCase(), aWord.toLowerCase())==1))){
													aWordNotFound=false;
													actualWords[wordscounter++]=aWord;
													break;
												}
												else{
													if(aWord.contains("-")){
														String[] myaWord = aWord.split("-");
														aWord="";
														for(int k=0; k<myaWord.length; k++) aWord+=myaWord[k];					
													}
													if(aTweetWord.contains("-")){
														String[] myaWord = aTweetWord.split("-");
														aTweetWord="";
														for(int k=0; k<myaWord.length; k++) aTweetWord+=myaWord[k];					
													}
													if(aTweetWord.contains(",")){
														String[] myaWord = aTweetWord.split(",");
														aTweetWord=myaWord[0];	
													}
													if (minDistance(aWord,aTweetWord)<=1){
														aWordNotFound=false;
														actualWords[wordscounter++]=aTweetWord;
														break;
													}
												}
											}
											if(aWordNotFound) break;
										}
										if(aWordNotFound) continue;
										else{
											found=1;
											
											myKeyWord=keywordString;
											
											for(String anActual: actualWords){
												isStemmed.add(true);
												if(actualKeyword.equals("")) actualKeyword+=anActual;
												else if(!actualKeyword.toLowerCase().contains(anActual.toLowerCase())) actualKeyword+="| "+anActual;
											}
											break;
										}
									}
									if(found==1) break;
								
								
								}
							}
							
							if (found==0) TweetContainKeywords=false;
					 }*/
						
						if(placeString.length()>1)
							if(!(tweet.getUser().getLocation()).toLowerCase().contains(placeString.toLowerCase())) TweetContainKeywords=false;
						
						if(TweetContainKeywords) {
							MediaEntity[] media = tweet.getMediaEntities(); //get the media entities from the status
							String images="";
							for(MediaEntity m : media){  //search trough your entities
								images+=(m.getMediaURL())+" "; } //get your url!
							
							JSONObject jsonObject = new JSONObject();
							jsonObject.put("sourceImg", tweet.getUser().getBiggerProfileImageURL());
							jsonObject.put("poster", tweet.getUser().getName());
							jsonObject.put("postDate", tweet.getCreatedAt());
							String[] myKeywordsArray = actualKeyword.split("\\|[ ]*");
							String tweetText = tweet.getText();
							//int stemcount = 0;
							for(String aKeyword: myKeywordsArray){
								boolean allCapitals = false;
								boolean isakaoracronym = false;
								if (aKeyword.toLowerCase().indexOf(" (aka")>=0 || aKeyword.toLowerCase().indexOf(" (acronym")>=0){
									aKeyword = aKeyword.split("\\(")[0].trim();
									isakaoracronym=true;
								}
								if(minDistance(aKeyword,aKeyword.toUpperCase())<=1) allCapitals=true;
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
								
								if(!allCapitals || !isakaoracronym){
									
									startIndex = tweetText.toLowerCase().indexOf(aKeyword.toLowerCase());
									
								}
								else startIndex = tweetText.indexOf(aKeyword);
								endIndex = startIndex + aKeyword.length();
								/*if(isStemmed.get(stemcount)==true){
									while(endIndex<tweetText.length() && ((tweetText.charAt(endIndex)>='a' && tweetText.charAt(endIndex)<='z') || (tweetText.charAt(endIndex)>='A' && tweetText.charAt(endIndex)<='Z'))) endIndex++;
								}
								stemcount++;*/
								tweetText = tweetText.substring(0,startIndex) + "<mark>" + tweetText.substring(startIndex,endIndex) + "</mark>" + tweetText.substring(endIndex,tweetText.length());
							}
							jsonObject.put("postText", tweetText);
							jsonObject.put("postPhoto", images);
							jsonObject.put("keyword", myKeyWord.replace('|', ','));
							listJSONobj.add(jsonObject);
						}
				  	  //}
					}
				  
						
					
				  
					  
					
					
				  
				  
					JSONObject result = new JSONObject();
					result.put("listJSONobj", listJSONobj);
					PrintWriter pw = response.getWriter();
					pw.print(result.toString());
					pw.close();
					System.out.println(result.toString());
				}catch (Exception e) {
				   	System.out.println(e);
				}
			}
			
		}
			
			
		    
	}
}