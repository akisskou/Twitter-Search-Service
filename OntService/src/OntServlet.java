import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.google.gson.Gson;


/**
 * Servlet implementation class OntServlet
 */
public class OntServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static OWLOntology ontology;
	private static String akalogic;
	private static OWLOntologyManager manager;
	private static IRI documentIRI;
	private static List<aClass> allClasses;
       
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
                    }
                }
        		else if(a.getProperty().isComment()) {
                    if(a.getValue() instanceof OWLLiteral) {
                        OWLLiteral val = (OWLLiteral) a.getValue();
                        f.comment = val.getLiteral();
                    }
                }
        		else{
        			if(a.getProperty().getIRI().getFragment().equals("aka")){
        				if(a.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) a.getValue();
                            f.aka = val.getLiteral();
                        }
        			}
        			else if(a.getProperty().getIRI().getFragment().equals("acronym")){
        				if(a.getValue() instanceof OWLLiteral) {
                            OWLLiteral val = (OWLLiteral) a.getValue();
                            f.acronym = val.getLiteral();
                        }
        			}
        		}
            }
        	allClasses.add(f);
        }
	}
	
	private static void findSubclasses(){
		for (final org.semanticweb.owlapi.model.OWLSubClassOfAxiom subClasse : ontology.getAxioms(AxiomType.SUBCLASS_OF))
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
				}
			}
			if(!(checked.acronym.equals(""))){
				List<String> acronymList = Arrays.asList(checked.acronym.split(",[ ]*"));
				for(int i=0; i<acronymList.size(); i++){
					keywords += "|"+acronymList.get(i)+" (acronym of "+checked.label+")";
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
					ac.put("li_attr",cont);
					ac.put("children", subs);
					
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
	    //documentIRI = IRI.create("file:///C:/", "social_media_search_ontology_v1_fin.owl");
		InputStream input = new FileInputStream(getServletContext().getRealPath("/WEB-INF/infos.properties"));
    	Properties prop = new Properties();
    	// load a properties file
        prop.load(input);
		documentIRI = IRI.create(getServletContext().getResource("/WEB-INF/"+prop.getProperty("owlFile").trim()));
		//documentIRI = IRI.create(getServletContext().getResource("/WEB-INF/social_media_search_ontology_v1_fin.owl"));
		try{
	        ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
            findClasses();
            findSubclasses();
		}
		catch (OWLOntologyCreationException e) {
	        e.printStackTrace();
			
		}
		JSONObject all = new JSONObject();
		List<JSONObject> ontology = new ArrayList<JSONObject>();
		ontology = fetchOntology(ontology);
		try {
			all.put("ontology", ontology);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw = response.getWriter();
		pw.print(all.toString());
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		InputClass inp = new Gson().fromJson(request.getReader(), InputClass.class);
		JSONObject all = new JSONObject();
		if(!inp.password.equals("1"+inp.username+"2!")) {
			all.put("errormessage", "Username and password don't match. Please check your credentials and try again.");
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.print(all.toString());
			pw.close();
		}else{
			if(inp.items.isEmpty() && inp.mykeywords.replace(",", "").trim().isEmpty()) {
				try {
					all.put("errormessage", "No keywords found. Please select or type some keywords and try again.");
					response.setContentType("text/html; charset=UTF-8");
					response.setCharacterEncoding("UTF-8");
					PrintWriter pw = response.getWriter();
					pw.print(all.toString());
					pw.close();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				akalogic = inp.akalogic;
				manager = OWLManager.createOWLOntologyManager();
		    	InputStream input = new FileInputStream(getServletContext().getRealPath("/WEB-INF/infos.properties"));
		    	Properties prop = new Properties();
	            // load a properties file
	            prop.load(input);
				documentIRI = IRI.create(getServletContext().getResource("/WEB-INF/"+prop.getProperty("owlFile").trim()));
			    try{
			        ontology = manager.loadOntologyFromOntologyDocument(documentIRI);
		            findClasses();
		            findSubclasses();
				}
				catch (OWLOntologyCreationException e) {
			        e.printStackTrace();
				}
				List<JSONObject> tweets = new ArrayList<JSONObject>();
				String querykeywords="";
				URL url = new URL("http://"+prop.getProperty("domain")+":"+prop.getProperty("port")+"/SMA_Adapter/TwitterServlet");
				List<String> keywordsList = new ArrayList<String>();
				if(!inp.items.isEmpty()){
					List<String> checkedList = Arrays.asList(inp.items.split(","));
					for(int j=0; j<checkedList.size(); j++){
						for(int i=0; i<allClasses.size(); i++){
							if(checkedList.get(j).equals(allClasses.get(i).id)){
								try{
									String keywords = getSubKeywords("", allClasses.get(i));
									keywordsList.add(keywords);
									if(querykeywords.equals("")) querykeywords = allClasses.get(i).label;
									else querykeywords += ", "+allClasses.get(i).label;
								}
								catch (Exception e) {
						   			System.out.println(e);
						   		}
								
								break;	
							}
						}	
					}
				}
				if(!inp.mykeywords.trim().isEmpty()){
					List<String> mykeywords = Arrays.asList(inp.mykeywords.trim().split(","));
					for(int j=0; j<mykeywords.size(); j++){
						keywordsList.add(mykeywords.get(j).trim());
						if(querykeywords.equals("")) querykeywords = mykeywords.get(j).trim();
						else querykeywords += ", "+mykeywords.get(j).trim();
					}
				}
				try{
					JSONObject params = new JSONObject();
					params.put("keywords", keywordsList);
				    params.put("place", "");
				    params.put("logic", inp.logic);
				    String postData = params.toString();
				    //System.out.println(postData);
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
				    System.out.println(resp);
				    List<String> myresp = Arrays.asList(resp.split("\\[",2));
				    String jsonarr = "[" + myresp.get(1);
				    JSONArray jsonarray = new JSONArray(jsonarr);
				    for (int k = 0; k < jsonarray.length(); k++) {
			            tweets.add(jsonarray.getJSONObject(k));
				    }
				    all.put("tweets", tweets);
					all.put("query", querykeywords);
					all.put("total", tweets.size());
					all.put("logic", inp.logic);
					all.put("akalogic", akalogic);
					
					response.setContentType("text/html; charset=UTF-8");
					response.setCharacterEncoding("UTF-8");
					PrintWriter pw = response.getWriter();
					pw.print(all.toString());
					pw.close();
					
					final Sardine upSardine = SardineFactory.begin(inp.username, inp.password);
			    	if(!upSardine.exists("http://83.212.104.6/hcloud/remote.php/webdav/Social-Media-Search-Results")){
			    		upSardine.createDirectory("http://83.212.104.6/hcloud/remote.php/webdav/Social-Media-Search-Results");
			    	}
			    	String inputJSON = "{\"smedia\":\""+inp.smedia+"\",\"items\":\""+inp.items+"\",\"mykeywords\":\""+inp.mykeywords.trim()+"\",\"logic\":\""+inp.logic+"\",\"akalogic\":\""+akalogic+"\"}";
			    	byte[] reqBytes = inputJSON.getBytes(StandardCharsets.UTF_8);
			    	Date date = new Date();
					Object param = new java.sql.Timestamp(date.getTime());
			    	String requestFile = inp.username+"-"+((Timestamp) param).toString().replace("-","").replace(" ","").replace(":","").split("\\.")[0]+"-Request.json";
			    	upSardine.put("http://83.212.104.6/hcloud/remote.php/webdav/Social-Media-Search-Results/"+requestFile, reqBytes);
			    	String outputJSON = "{\"results\":"+tweets+",\"results_count\":\""+tweets.size()+"\"}";
			    	byte[] respBytes = outputJSON.getBytes(StandardCharsets.UTF_8);
				    String responseFile = inp.username+"-"+((Timestamp) param).toString().replace("-","").replace(" ","").replace(":","").split("\\.")[0]+"-Response.json";
			    	upSardine.put("http://83.212.104.6/hcloud/remote.php/webdav/Social-Media-Search-Results/"+responseFile, respBytes);
			    	JSONObject dbData = setCohortHistory(inp.username, inp.password, ((Timestamp) param).toString().split("\\.")[0], requestFile, responseFile);
			    	System.out.println(dbData);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (Exception e) {
		   			System.out.println(e);
		   		}
			}
		}
	}
	
	private static JSONObject setCohortHistory(String username, String password, String submitDate, String jsonfileInput, String jsonfileOutput) throws IOException, JSONException{
		URL url = new URL("https://private.harmonicss.eu/index.php/apps/coh/api/1.0/history");
        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
		JSONObject params = new JSONObject();
		params.put("userId", username);
		params.put("serviceId", "6");
	    params.put("submitdate", submitDate);
	    
	    params.put("jsonfileInput", jsonfileInput);
	    params.put("jsonfileOutput", jsonfileOutput);
	    String postData = params.toString();
	    byte[] postDataBytes = postData.getBytes("UTF-8");
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/json");
	    conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
	    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	    conn.setDoOutput(true);
	    conn.getOutputStream().write(postDataBytes);
	    Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	    StringBuilder sb = new StringBuilder();
	    for (int c; (c = in.read()) >= 0;)
	        sb.append((char)c);
	    String resp = sb.toString();
	    JSONObject dbData = new JSONObject(resp);
	    return dbData;
    }

}
