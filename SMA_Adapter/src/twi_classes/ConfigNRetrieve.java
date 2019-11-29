package twi_classes;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class ConfigNRetrieve implements Runnable{
public static List<Status> tweets;
	 public static void main(String[] args) throws TwitterException, FileNotFoundException{}

	public  void run(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(ConfigureFile.getAuthConsumerKey())
		  .setOAuthConsumerSecret(ConfigureFile.getAuthConsumerSecret())
		  .setOAuthAccessToken(ConfigureFile.getAuthAccessToken())
		  .setOAuthAccessTokenSecret(ConfigureFile.getAccessTokenSecret())
		  .setTweetModeExtended(true); //new to take all tweet text!
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<String> keywords= Arrays.asList(ConfigureFile.getArrayKeyWords());//Arrays.asList("sjogren", "sjögren", "sjogren’s","sjögren’s","sjogrens","sjögrens");
	    HashSet<Float> twitersIDs = new HashSet<Float>();

	    Query query = new Query(keywords.get(0)); 
	    query.setCount(1);
	    QueryResult result=null;
		try {
			result = twitter.search(query);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	    tweets = result.getTweets(); 
	    twitersIDs.add((float) tweets.get(0).getId());
	    int keyword_index=0;
	    Boolean oldTweetsExist=true;
	    
	    long maxID = -1;
	    while(oldTweetsExist) { //We are in the case of retrieving old tweets.
	    	try {
				Thread.sleep(ConfigureFile.getTimedelay());//(6000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	
		    if(keyword_index==keywords.size()) keyword_index=0;
		    
		    query = new Query(keywords.get(keyword_index));
		    query.setCount(100);
		    query.resultType(Query.RECENT);//.resultType("recent");
		    
		    if (maxID != -1)
			{
				query.setMaxId(maxID - 1);
			}
		    
			try {
				result = twitter.search(query);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		    
			for(Status tweet :result.getTweets()) {
				if (maxID == -1 || tweet.getId() < maxID)
				{
					maxID = tweet.getId();
				}

		    	if (!(twitersIDs.contains((float) tweet.getId()))) {
		    		twitersIDs.add((float) tweet.getId());
		    		tweets.add(tweet);		
				}
			}
			if (result.getTweets().size() == 0){
				System.out.println("Term-"+keywords.get(keyword_index)+": New tweets that we found: "+result.getTweets().size());
				keyword_index+=1;
				maxID = -1;
				if(keyword_index==keywords.size())
					oldTweetsExist=false;			// Nothing? We must be done
			}
		 System.out.println("Old Tweets: We have "+tweets.size()+" tweets."); 	
	    	
	    }
	    
	    
	    while(true) { //We are in the case of waiting for new tweets.
	    	try {
				Thread.sleep(ConfigureFile.getTimedelay());//6000
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	
		    if(keyword_index==keywords.size()) keyword_index=0;
		    
		    query = new Query(keywords.get(keyword_index));
		    keyword_index+=1;
		    query.setCount(100);
		    query.resultType(Query.RECENT);//.resultType("recent");
		    
			try {
				result = twitter.search(query);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		    
			for(Status tweet :result.getTweets()) {
		    	if (!(twitersIDs.contains((float) tweet.getId()))) {
		    		twitersIDs.add((float) tweet.getId());
		    		tweets.add(tweet);		
				}
			}
	    
		 System.out.println("New Tweets: We have "+tweets.size()+" tweets.");   
		}
	    

	}
	
	public  void old_run(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("KlgQdmfSffRfH7Bj25v0zXoeW")
		  .setOAuthConsumerSecret("5ZTFwwPIEGq5a0WY75raXNtTMGQM1GRdozvY1K3VE4T5H4UkmP")
		  .setOAuthAccessToken("273049818-Ez9NWMfgO9UzLpLjlxCVRfoToQPDMWqxvKTpRNLQ")
		  .setOAuthAccessTokenSecret("TV2lUGRJCDo0dzX3Fmy307yyIU4yvxBks57mWd8lpJzuH")
		  .setTweetModeExtended(true); //new to take all tweet text!
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<String> keywords= Arrays.asList("sjogren", "sjögren", "sjogren’s","sjögren’s","sjogrens","sjögrens");
	    HashSet<Float> twitersIDs = new HashSet<Float>();

	    Query query = new Query(keywords.get(0));
	    query.setCount(1);
	    QueryResult result=null;
		try {
			result = twitter.search(query);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	    tweets = result.getTweets(); 
	    twitersIDs.add((float) tweets.get(0).getId());
	    int keyword_index=0;
	    
	    while(true) {
	    	try {
				Thread.sleep(ConfigureFile.getTimedelay());//(6000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    	
		    if(keyword_index==keywords.size()) keyword_index=0;
		    
		    query = new Query(keywords.get(keyword_index));
		    keyword_index+=1;
		    query.setCount(100);
		    
			try {
				result = twitter.search(query);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		    
			for(Status tweet :result.getTweets()) {
		    	if (!(twitersIDs.contains((float) tweet.getId()))) {
		    		twitersIDs.add((float) tweet.getId());
		    		tweets.add(tweet);		
				}
			}
	    
		 System.out.println("we have "+tweets.size()+" tweets.");   
		}

	}
	
}
