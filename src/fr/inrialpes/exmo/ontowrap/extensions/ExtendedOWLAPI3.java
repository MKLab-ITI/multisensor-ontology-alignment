package fr.inrialpes.exmo.ontowrap.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;

public class ExtendedOWLAPI3 extends OWLAPI3Ontology implements IExtendedOntology<OWLOntology> {
	
	public static String JENAMODEL = "JENA";
	
	@Override
	public Object toModel(String model) {
		if (model == null)
			return null;
		
		if (model.equals(JENAMODEL)) {
			try{
			    ByteArrayOutputStream out = new ByteArrayOutputStream();

			    OWLOntologyManager owlmanager = this.onto.getOWLOntologyManager();

			    String format = "TURTLE";
			    //String format = "RDF/XML";

			    if(format.equals("TURTLE")||format.equals("RDF/XML")){

			        if(format.equals("TURTLE"))
			          owlmanager.setOntologyFormat(this.onto, new TurtleOntologyFormat());
			        else if(format.equals("RDF/XML"))
			          owlmanager.setOntologyFormat(this.onto,new RDFXMLOntologyFormat());

			        OWLOntologyFormat owlformat = owlmanager.getOntologyFormat(this.onto);

			        owlmanager.saveOntology(this.onto, owlformat, out);

			        OntModel jenamodel = ModelFactory.createOntologyModel();
			        OWLOntologyID id = this.onto.getOntologyID();
			        jenamodel.read(new ByteArrayInputStream(out.toByteArray()),id.toString().replace("<","").replace(">",""),format);
			        
			        return jenamodel;
			    }
		    }catch (OWLOntologyStorageException eos){
		        System.err.print("ModelOwlToJenaConvert::: ");
		        eos.printStackTrace();
		        return null;
		    }
		}
		
		return null;
	}

}
