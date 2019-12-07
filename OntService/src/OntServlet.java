import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;


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
	//private static String languages = "";
       
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
                       //f.language = val.getLang();
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
		//if(languages.equals("")) languages += checked.language;
		//else languages += "|"+checked.language;
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
	    documentIRI = IRI.create("file:///C:/", "social_media_search_ontology_v1_fin.owl");
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
		List<JSONObject> tweets = new ArrayList<JSONObject>();
		String querykeywords="";
		String checked = request.getParameter("items");
		String myKeywords = request.getParameter("mykeywords");
		
		String logic = request.getParameter("logic");
		akalogic = request.getParameter("akalogic");
		if(checked.equals("null") && myKeywords.equals("null")) ontology = fetchOntology(ontology);
		else{
			//URL url = new URL("http://ponte.grid.ece.ntua.gr:8080/SMA_Adapter/retrieve");
			URL url = new URL("http://localhost:8080/SMA_Adapter/TwitterServlet");
			List<String> keywordsList = new ArrayList<String>();
			//List<String> languageList = new ArrayList<String>();
			if(!checked.equals("null")){
			List<String> checkedList = Arrays.asList(checked.split(","));
			
			ontology = fetchOntology(ontology);
			for(int j=0; j<checkedList.size(); j++){
				for(int i=0; i<allClasses.size(); i++){
					if(checkedList.get(j).equals(allClasses.get(i).id)){
						try{
							String keywords = getSubKeywords("", allClasses.get(i));
							keywordsList.add(keywords);
							if(querykeywords.equals("")) querykeywords = allClasses.get(i).label;
							else querykeywords += ", "+allClasses.get(i).label;
							//languageList.add(languages);
							//languages="";
						}
						catch (Exception e) {
				   			System.out.println(e);
				   		}
						
						break;	
					}
				}	
			}
			}
			if(!myKeywords.equals("null")){
				List<String> mykeywords = Arrays.asList(myKeywords.split(","));
				for(int j=0; j<mykeywords.size(); j++){
					keywordsList.add(mykeywords.get(j).trim());
					//languageList.add("en");
					if(querykeywords.equals("")) querykeywords = mykeywords.get(j).trim();
					else querykeywords += ", "+mykeywords.get(j).trim();
				}
			}
			try{
				JSONObject params = new JSONObject();
				params.put("keywords", keywordsList);
				//params.put("languages", languageList);
				//System.out.println(languageList);
			    params.put("place", "");
			    params.put("logic", logic);
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
			}
		
			
			catch (Exception e) {
	   			System.out.println(e);
	   		}
		}
		try {
			all.put("ontology", ontology);
			all.put("tweets", tweets);
			all.put("query", querykeywords);
			all.put("total", tweets.size());
			all.put("logic", logic);
			all.put("akalogic", akalogic);
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
	}

}
