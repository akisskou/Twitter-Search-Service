import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

import sun.rmi.runtime.Log;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Servlet implementation class OntServlet
 */
public class OntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static OWLOntology ontology;
	private static String akalogic;
	private static OWLOntologyManager manager;
	private static IRI documentIRI;
	private static OWLDataFactory df;
	private static List<aClass> allClasses;
	private static long sleepTime = 15*60*1000;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OntServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    @SuppressWarnings("deprecation")
	private static void findClasses(){
		Set<OWLClass> ontClasses = new HashSet<OWLClass>(); 
        ontClasses = ontology.getClassesInSignature();
        allClasses = new ArrayList<aClass>();
        
        for (Iterator<OWLClass> it = ontClasses.iterator(); it.hasNext(); ) {
        	aClass f = new aClass();
        	f.name = it.next();
        	f.id = f.name.getIRI().getFragment();
        	for(OWLAnnotationAssertionAxiom a : ontology.getAnnotationAssertionAxioms(f.name.getIRI())) {
        		if(a.getProperty().isLabel()) {
                    if(a.getValue() instanceof OWLLiteral) {
                        OWLLiteral val = (OWLLiteral) a.getValue();
                        f.label = val.getLiteral();
                        //System.out.println(f.label);
                    }
                }
        		else if(a.getProperty().isComment()) {
                    if(a.getValue() instanceof OWLLiteral) {
                        OWLLiteral val = (OWLLiteral) a.getValue();
                        f.comment = val.getLiteral();
                        //System.out.println(f.comment);
                    }
                }
        		else{
        			//System.out.println(a.getProperty().getIRI().getFragment());
        			if(a.getProperty().getIRI().getFragment().equals("aka")){
        				if(a.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) a.getValue();
                            f.aka = val.getLiteral();
                            //System.out.println(val.getLiteral());
                        }
        			}
        			else if(a.getProperty().getIRI().getFragment().equals("acronym")){
        				if(a.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) a.getValue();
                            f.acronym = val.getLiteral();
                            //System.out.println(val.getLiteral());
                        }
        			}
        		}
            }
        	allClasses.add(f);
        }
	}
	
	private static void findSubclasses(){
		for (final OWLSubClassOfAxiom subClasse : ontology.getAxioms(AxiomType.SUBCLASS_OF))
        {
        	OWLClass sup = (OWLClass) subClasse.getSuperClass();
        	OWLClass sub = (OWLClass) subClasse.getSubClass();
        	
            if (sup instanceof OWLClass && sub instanceof OWLClass)
            {
            	int i;
            	for(i=0; i<allClasses.size(); i++){
            		if (sup.equals(allClasses.get(i).name)) break;
            	}
            	int j;
            	for(j=0; j<allClasses.size(); j++){
            		if (sub.equals(allClasses.get(j).name)){
            			allClasses.get(i).subClasses.add(allClasses.get(j));
            			allClasses.get(j).isSubClass = true;
            			break;
            		}
            	}
            	
            }
        }
        /*for(int i=0; i<allClasses.size(); i++){
        	System.out.println(allClasses.get(i).name.getIRI().getFragment());
        	for(int j=0; j<allClasses.get(i).subClasses.size(); j++){
        		System.out.println(allClasses.get(i).subClasses.get(j).name.getIRI().getFragment() + " extends " + allClasses.get(i).name.getIRI().getFragment());
        	}
        }*/
	}
	
	private static void findSubJson(List<JSONObject> subs, aClass ac){
		for(int i=0; i<ac.subClasses.size(); i++){
			JSONObject ac1 = new JSONObject();
			List<JSONObject> subs1 = new ArrayList<JSONObject>();
			findSubJson(subs1, ac.subClasses.get(i));
			try {
				String content="";
				ac1.put("id", ac.subClasses.get(i).id);
				ac1.put("text", ac.subClasses.get(i).label);
				JSONObject cont = new JSONObject();
				content+=ac.subClasses.get(i).label;
				if(!(ac.subClasses.get(i).comment.equals(""))) content+="<br/>"+ac.subClasses.get(i).comment;
				if(!(ac.subClasses.get(i).aka.equals(""))) content+="<br/>aka: "+ac.subClasses.get(i).aka;
				if(!(ac.subClasses.get(i).acronym.equals(""))) content+="<br/>acronym: "+ac.subClasses.get(i).acronym;				
				cont.put("content", content);
				ac1.put("li_attr",cont);
				ac1.put("children", subs1);
				
				/*ac1.put("label", ac.subClasses.get(i).label);
				ac1.put("comment", ac.subClasses.get(i).comment);
				ac1.put("aka", ac.subClasses.get(i).aka);
				//ac1.put("name", ac.subClasses.get(i).name.getIRI().getFragment());
				ac1.put("subs", subs1);*/
				subs.add(ac1);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static String getSubKeywords(String keywords, aClass checked){
		if(keywords.equals("")) keywords += checked.label;			
		else keywords += "|"+checked.label;
		if(akalogic.equals("yes")){
			if(!(checked.aka.equals(""))){
				List<String> akaList = Arrays.asList(checked.aka.split(",[ ]*"));
				for(int i=0; i<akaList.size(); i++){
					keywords += "|"+akaList.get(i)+" (aka of "+checked.label+")";
					//System.out.println(akaList.get(i));
				}
			}
			if(!(checked.acronym.equals(""))){
				List<String> acronymList = Arrays.asList(checked.acronym.split(",[ ]*"));
				for(int i=0; i<acronymList.size(); i++){
					keywords += "|"+acronymList.get(i)+" (acronym of "+checked.label+")";
					//System.out.println(acronymList.get(i));
				}
			}

		}
		for(int i=0; i<checked.subClasses.size(); i++){
			keywords = getSubKeywords(keywords, checked.subClasses.get(i));
		}
		return keywords;
	}
	
	private static List<JSONObject> fetchOntology(List<JSONObject> ontology)
	{
		for(int i=0; i<allClasses.size(); i++){
			if(!allClasses.get(i).isSubClass){
				try {
					JSONObject ac = new JSONObject();
					JSONObject cont = new JSONObject();
					String content="";
					List<JSONObject> subs = new ArrayList<JSONObject>();
					findSubJson(subs, allClasses.get(i));
					ac.put("id", allClasses.get(i).id);
					ac.put("text", allClasses.get(i).label);
					content+=allClasses.get(i).label;
					if(!(allClasses.get(i).comment.equals(""))) content+="<br/>"+allClasses.get(i).comment;
					if(!(allClasses.get(i).aka.equals(""))) content+="<br/>aka: "+allClasses.get(i).aka;
					if(!(allClasses.get(i).acronym.equals(""))) content+="<br/>acronym: "+allClasses.get(i).acronym;
					cont.put("content", content);
					//cont.put("content", allClasses.get(i).label+"</br>"+allClasses.get(i).comment+"</br>aka: "+allClasses.get(i).aka+"</br>");
					
					ac.put("li_attr",cont);
					ac.put("children", subs);
					/*ac.put("label", allClasses.get(i).label);
					ac.put("comment", allClasses.get(i).comment);
					ac.put("aka", allClasses.get(i).aka);
					ac.put("subs", subs);*/
					ontology.add(ac);					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		return ontology;
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		// TODO Auto-generated method stub
		
		manager = OWLManager.createOWLOntologyManager();
	    documentIRI = IRI.create("file:///C:/", "social_media_search_ontology_v1_fin.owl");
		try{
	        ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
            df = manager.getOWLDataFactory();
            findClasses();
            findSubclasses();
		}
		catch (OWLOntologyCreationException e) {
	        e.printStackTrace();
			
		}
		JSONObject all = new JSONObject();
		List<JSONObject> ontology = new ArrayList<JSONObject>();
		//JSONObject resp = new JSONObject();
		List<JSONObject> tweets = new ArrayList<JSONObject>();
		String querykeywords="";
		String checked = request.getParameter("items");
		String logic = request.getParameter("logic");
		akalogic = request.getParameter("akalogic");
		if(checked.equals("null")){
		/*for(int i=0; i<allClasses.size(); i++){
			if(!allClasses.get(i).isSubClass){
				try {
					JSONObject ac = new JSONObject();
					JSONObject cont = new JSONObject();
					String content="";
					List<JSONObject> subs = new ArrayList<JSONObject>();
					findSubJson(subs, allClasses.get(i));
					ac.put("id", allClasses.get(i).id);
					ac.put("text", allClasses.get(i).label);
					content+=allClasses.get(i).label;
					if(!(allClasses.get(i).comment.equals(""))) content+="<br/>"+allClasses.get(i).comment;
					if(!(allClasses.get(i).aka.equals(""))) content+="<br/>aka: "+allClasses.get(i).aka;
					cont.put("content", content);
					
					ac.put("li_attr",cont);
					ac.put("children", subs);
					ontology.add(ac);					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}*/
			ontology = fetchOntology(ontology);
		}
		else{
		//if(!checked.equals("null")){
			//URL url = new URL("http://ponte.grid.ece.ntua.gr:8080/SMA_Adapter/retrieve");
			URL url = new URL("http://localhost:8080/SMA_Adapter/TwitterServlet");
			List<String> checkedList = Arrays.asList(checked.split(","));
			List<String> keywordsList = new ArrayList<String>();
			ontology = fetchOntology(ontology);
			for(int j=0; j<checkedList.size(); j++){
				for(int i=0; i<allClasses.size(); i++){
					if(checkedList.get(j).equals(allClasses.get(i).id)){
						try{
							/*JSONObject ac = new JSONObject();
							JSONObject cont = new JSONObject();
							String content="";
							List<JSONObject> subs = new ArrayList<JSONObject>();
							findSubJson(subs, allClasses.get(i));
							ac.put("id", allClasses.get(i).id);
							ac.put("text", allClasses.get(i).label);*/
							String keywords = getSubKeywords("", allClasses.get(i));
							keywordsList.add(keywords);
							if(querykeywords.equals("")) querykeywords = allClasses.get(i).label;
							else querykeywords += ", "+allClasses.get(i).label;
						    /*content+=allClasses.get(i).label;
							if(!(allClasses.get(i).comment.equals(""))) content+="<br/>"+allClasses.get(i).comment;
							if(!(allClasses.get(i).aka.equals(""))){
								content+="<br/>aka: "+allClasses.get(i).aka;
							}
							cont.put("content", content);
							
							ac.put("li_attr",cont);
							ac.put("children", subs);
							ontology.add(ac);*/	
							
						}
						catch (Exception e) {
				   			System.out.println(e);
				   		}
						
						break;	
					}
				}	
			}
			try{
				JSONObject params = new JSONObject();
				params.put("keywords", keywordsList);
			    params.put("place", "");
			    params.put("logic", logic);
			    params.put("akalogic", akalogic);
			    String postData = params.toString();
			    System.out.println(postData);
			    byte[] postDataBytes = postData.getBytes("UTF-8");
			    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			    conn.setRequestMethod("POST");
			    conn.setRequestProperty("Content-Type", "application/json");
			    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			    conn.setDoOutput(true);
			    conn.getOutputStream().write(postDataBytes);
			    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    StringBuilder sb = new StringBuilder();
			    for (int c; (c = in.read()) >= 0;)
			        sb.append((char)c);
			    String resp = sb.toString();
			    List<String> myresp = Arrays.asList(resp.split("\\[",2));
			    String jsonarr = "[" + myresp.get(1);
			    System.out.println(jsonarr);
			    JSONArray jsonarray = new JSONArray(jsonarr);
			    for (int k = 0; k < jsonarray.length(); k++) {
			    
			    	/*JSONObject newTweet = new JSONObject();
			        JSONObject jsonobject = jsonarray.getJSONObject(k);
			        newTweet.put("poster", jsonobject.getString("poster"));
					newTweet.put("sourceImg", jsonobject.getString("sourceImg"));
					newTweet.put("postPhoto", jsonobject.getString("postPhoto"));
					newTweet.put("postText", jsonobject.getString("postText"));	
					newTweet.put("postDate", jsonobject.getString("postDate"));
					newTweet.put("source", jsonobject.getString("source"));
					newTweet.put("keyword", jsonobject.getString("keyword"));*/
		            tweets.add(jsonarray.getJSONObject(k));
			    }
			}
		
			
			catch (Exception e) {
	   			System.out.println(e);
	   		}
			
			/*ConfigurationBuilder cb = new ConfigurationBuilder();
		    cb.setDebugEnabled(true).
		    setOAuthConsumerKey("KlgQdmfSffRfH7Bj25v0zXoeW").setOAuthConsumerSecret("5ZTFwwPIEGq5a0WY75raXNtTMGQM1GRdozvY1K3VE4T5H4UkmP").setOAuthAccessToken("273049818-Ez9NWMfgO9UzLpLjlxCVRfoToQPDMWqxvKTpRNLQ").setOAuthAccessTokenSecret("TV2lUGRJCDo0dzX3Fmy307yyIU4yvxBks57mWd8lpJzuH")
		    .setTweetModeExtended(true);
		    TwitterFactory tf = new TwitterFactory(cb.build());
		    Twitter t = tf.getInstance();
		    
			List<String> checkedList = Arrays.asList(checked.split(","));
			for(int j=0; j<checkedList.size(); j++){
				for(int i=0; i<allClasses.size(); i++){
					if(checkedList.get(j).equals(allClasses.get(i).id)){
						try{
							JSONObject ac = new JSONObject();
							JSONObject cont = new JSONObject();
							String content="";
							List<JSONObject> subs = new ArrayList<JSONObject>();
							findSubJson(subs, allClasses.get(i));
							ac.put("id", allClasses.get(i).id);
							ac.put("text", allClasses.get(i).label);
							
						    try {
						    	Query topicQuery = new Query(allClasses.get(i).label);
						    	topicQuery.setCount(100);
						    	topicQuery.setLang("en");
						    	
							
								QueryResult queryResult1 = t.search(topicQuery);
								
								
								
								
									List<Status> mytweets = queryResult1.getTweets();
									
								for (Status aStatus : mytweets){
								if (!aStatus.isRetweet()){
									JSONObject newTweet = new JSONObject();
									newTweet.put("poster", aStatus.getUser().getName());
									newTweet.put("sourceImg", aStatus.getUser().getProfileImageURL());
									newTweet.put("postPhoto", "");
									MediaEntity[] media = aStatus.getMediaEntities();
									for(MediaEntity m : media){ //search trough your entities
									    newTweet.put("postPhoto", m.getMediaURL());
									}
									newTweet.put("postText", aStatus.getText());	
									newTweet.put("postDate", aStatus.getCreatedAt());
									newTweet.put("source", (aStatus.getSource().split(">"))[1].split("<")[0]);
						            tweets.add(newTweet);
								}
								}
								
							} catch (TwitterException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							content+=allClasses.get(i).label;
							if(!(allClasses.get(i).comment.equals(""))) content+="<br/>"+allClasses.get(i).comment;
							if(!(allClasses.get(i).aka.equals(""))){
								content+="<br/>aka: "+allClasses.get(i).aka;
								/*try {
									Query topicQuery = new Query(allClasses.get(i).aka);
									topicQuery.setCount(100);
							    	topicQuery.setLang("en");
									QueryResult queryResult1 = t.search(topicQuery);
									for (Status aStatus : queryResult1.getTweets()){
									if (!aStatus.isRetweet()){
										JSONObject newTweet = new JSONObject();
										newTweet.put("poster", aStatus.getUser().getName());
										newTweet.put("sourceImg", aStatus.getUser().getBiggerProfileImageURL());
										newTweet.put("postPhoto", "");
										MediaEntity[] media = aStatus.getMediaEntities();
										for(MediaEntity m : media){ //search trough your entities
										    newTweet.put("postPhoto", m.getMediaURL());
										}
										newTweet.put("postText", aStatus.getText());	
										newTweet.put("postDate", aStatus.getCreatedAt());
										newTweet.put("source", (aStatus.getSource()/*.split(">"))[1].split("<")[0]));
							            tweets.add(newTweet);
									}
							        }
									
								} catch (TwitterException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							cont.put("content", content);
							
							ac.put("li_attr",cont);
							ac.put("children", subs);
							ontology.add(ac);					
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
						
					}
				}
			}*/
		}
		try {
			all.put("ontology", ontology);
			all.put("tweets", tweets);
			all.put("query", querykeywords);
			all.put("total", tweets.size());
			all.put("logic", logic);
			all.put("akalogic", akalogic);
			//System.out.println(tweets.size());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintWriter pw = response.getWriter();
		pw.print(all.toString());
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
