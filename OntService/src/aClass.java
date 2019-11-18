import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

public class aClass {
	OWLClass name;
	String id;
	String label;
	String comment="";
	String aka="";
	String acronym="";
	List<aClass> subClasses = new ArrayList<aClass>();
	boolean isSubClass = false;
}
