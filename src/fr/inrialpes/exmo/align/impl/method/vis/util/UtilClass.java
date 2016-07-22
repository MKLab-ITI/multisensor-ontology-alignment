package fr.inrialpes.exmo.align.impl.method.vis.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import gr.iti.multisensor.matrix.MSMappingList;
import gr.iti.multisensor.ui.AlignmentSteps;
import gr.iti.multisensor.ui.matches.AlignmentModelList;
import gr.iti.multisensor.ui.utils.Constants;
import gr.iti.multisensor.ui.utils.Constants.AlignmentParams;

public class UtilClass {
	public static String[] stopWords = {"a","about","above","across","after","afterwards","again","against","all","almost","alone","along","already","also","although","always","am","among","amongst","amoungst","amount","an","and","another","any","anyhow","anyone","anything","anyway","anywhere","are","around","as","at","back","be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","below","beside","besides","between","beyond","bill","both","bottom","but","by","call","can","cannot","cant","co","computer","con","could","couldnt","cry","de","describe","detail","do","done","down","due","during","each","eg","eight","either","eleven","else","elsewhere","empty","enough","etc","even","ever","every","everyone","everything","everywhere","except","few","fifteen","fify","fill","find","fire","first","five","for","former","formerly","forty","found","four","from","front","full","further","get","give","go","had","has","hasnt","have","he","hence","her","here","hereafter","hereby","herein","hereupon","hers","herself","him","himself","his","how","however","hundred","i","ie","if","in","inc","indeed","interest","into","is","it","its","itself","keep","last","latter","latterly","least","less","ltd","made","many","may","me","meanwhile","might","mill","mine","more","moreover","most","mostly","move","much","must","my","myself","name","namely","neither","never","nevertheless","next","nine","no","nobody","none","noone","nor","not","nothing","now","nowhere","of","off","often","on","once","one","only","onto","or","other","others","otherwise","our","ours","ourselves","out","over","own","part","per","perhaps","please","put","rather","re","same","see","seem","seemed","seeming","seems","serious","several","she","should","show","side","since","sincere","six","sixty","so","some","somehow","someone","something","sometime","sometimes","somewhere","still","such","system","take","ten","than","that","the","their","them","themselves","then","thence","there","thereafter","thereby","therefore","therein","thereupon","these","they","thick","thin","third","this","those","though","three","through","throughout","thru","thus","to","together","too","top","toward","towards","twelve","twenty","two","un","under","until","up","upon","us","very","via","was","we","well","were","what","whatever","when","whence","whenever","where","whereafter","whereas","whereby","wherein","whereupon","wherever","whether","which","while","whither","who","whoever","whole","whom","whose","why","will","with","within","without","would","yet","you","your","yours","yourself","yourselves"};
	public static Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
	
	static {
	//public static void makeStopWordIndex() {
		for (int i=0;i<stopWords.length;i++) {
			String index = String.valueOf(stopWords[i].charAt(0));
			if (indexMap.get(index) == null) {
				List<String> temp = new ArrayList<String>();
				temp.add(stopWords[i].trim());
				indexMap.put(index, temp);
			}
			else {
				indexMap.get(index).add(stopWords[i].trim());
			}
		}
	//}
	}
	
	public static List<String> removeStopWords(List<String> input) {
		List<String> ret = new ArrayList<String>();
		
		for (int i=0;i<input.size();i++) {
			if (input.get(i).length() > 0) {
				String temp = input.get(i).trim();
				temp = temp.toLowerCase();
				List<String> list = indexMap.get(String.valueOf(temp.charAt(0)));
				if (list != null) {
					if (!list.contains(temp)) {
						ret.add(temp);
					}
				}
			}
		}
		
		return ret;
	}
	
	public static List<String> removeAlphaNumeric(List<String> input) {
		List<String> ret = new ArrayList<String>();
		
		for (int i=0;i<input.size();i++) {
			if (!input.get(i).matches("^(?=.*[\\pL])(?=.*[\\pN])[\\pL\\pN]+$")) {
				ret.add(input.get(i));
			}
		}
		
		return ret;
	}
	
	public void downloadImagesFromList(List<String> list, String dir) {
		
		Lock lock = new Lock();
		
		File dirFile = new File(dir);
		if (!dirFile.exists())
			dirFile.mkdirs();
		
		int counter = 0;
		for (String url : list) {
			try {
				ImageDownloaderThread idt = new ImageDownloaderThread(url, dir+"/image"+(counter++)+".jpg", lock);
				//System.out.println("Downloading to location: "+dir+"/image"+(counter++)+".jpg");
				idt.start();
			} catch (Exception e) {
				System.out.println("Image "+url+" cannot be downloaded.");
			}
		}
 
		try {
			while (lock.getRunningThreadsNumber() > 0) {
				synchronized(lock) {
					lock.wait();
				}
				//System.out.println("Waiting for "+lock.getRunningThreadsNumber()+" thread(s) to finish");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public AlignmentSteps performAlignment(String onto1, String onto2, String[] alignmentAlgs) {
		final AlignmentSteps alignment = new AlignmentSteps();
		final List<AlignmentParams> alignmentAlgorithms = new ArrayList<AlignmentParams>();
		
		for (int i=0;i<alignmentAlgs.length;i++)
			alignmentAlgorithms.add(new AlignmentParams(alignmentAlgs[i]));
				
		alignment.setOnto1(onto1);
		alignment.setOnto2(onto2);
		alignment.clearSimilarityMatrix();
		
		for (int i=0;i<alignmentAlgorithms.size();i++)
			System.out.println(alignmentAlgorithms.get(i));
		
		alignment.performMatches(alignmentAlgorithms);
		
		return alignment;
	}
	
	public AlignmentModelList performWeightingAndThresholding(AlignmentSteps alignment, double threshold) {
		final List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		final AlignmentModelList aml = new AlignmentModelList();
		
		if (threshold > 0) {
			alignment.setThreshold(threshold*alignment.getSimilarityMatrixList().size());
		}
		else
			alignment.setThreshold(threshold);
		
		mappingLists.addAll(alignment.performWeighting());
		
		aml.setOntology1(alignment.getOntology1());
		aml.setOntology2(alignment.getOntology2());
		aml.populateList(mappingLists);
		aml.initBasicAlignment();
		
		return aml;
	}
	
	public AlignmentModelList performWeightingAndThresholdingOneAlignment(AlignmentSteps alignment, double threshold) {
		final List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		final AlignmentModelList aml = new AlignmentModelList();
		
		if (threshold > 0) {
			alignment.setThreshold(threshold*alignment.getSimilarityMatrixList().size());
		}
		else
			alignment.setThreshold(threshold);
		
		mappingLists.addAll(alignment.msOverallMappings(alignment.getSimilarityMatrixList(), threshold));
				
		aml.setOntology1(alignment.getOntology1());
		aml.setOntology2(alignment.getOntology2());
		aml.populateList(mappingLists);
		aml.initBasicAlignment();
		
		return aml;
	}
	
	public AlignmentModelList fuseMultipleAlignments(String onto1, String onto2, String[] alignmentAlgs) {
		final AlignmentSteps alignment = new AlignmentSteps();
		final List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		final AlignmentModelList aml = new AlignmentModelList();
		final List<AlignmentParams> alignmentAlgorithms = new ArrayList<AlignmentParams>();
		
		for (int i=0;i<alignmentAlgs.length;i++)
			alignmentAlgorithms.add(new AlignmentParams(alignmentAlgs[i]));
				
		alignment.setOnto1(onto1);
		alignment.setOnto2(onto2);
		alignment.clearSimilarityMatrix();
		
		for (int i=0;i<alignmentAlgorithms.size();i++)
			System.out.println(alignmentAlgorithms.get(i));
		
		alignment.performMatches(alignmentAlgorithms);
		
		if (mappingLists.size() > 0)
			mappingLists.clear();
		
		//alignment.setThreshold(-1);
		alignment.setThreshold(0.6*((double)mappingLists.size()));
		mappingLists.addAll(alignment.performWeighting());
		//mappingLists.addAll(alignment.performWeighting(alignment.getThreshold()));
		
		aml.setOntology1(alignment.getOntology1());
		aml.setOntology2(alignment.getOntology2());
		aml.populateList(mappingLists);
		aml.initBasicAlignment();
		
		return aml;
	}
	
	public void saveAlignment(String outputfilename, String rendererClass, AlignmentModelList aml) {
		PrintWriter writer = null;
		OutputStream stream;
		try {
			AlignmentVisitor renderer = null;
			if ( outputfilename != null ) {
				stream = new FileOutputStream( outputfilename );
				writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter( stream, "UTF-8" )), true);

				try {
					Class[] cparams = { PrintWriter.class };
					Constructor rendererConstructor = Class.forName(rendererClass).getConstructor( cparams );
					Object[] mparams = { (Object)writer };
					renderer = (AlignmentVisitor) rendererConstructor.newInstance( mparams );
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				// Output
				if (aml != null)
					aml.getBasicAlignment().render(renderer);
			}
		} catch ( Exception ex ) {
			ex.printStackTrace();
		} finally {
			if ( writer != null ) {
				writer.flush();
				writer.close();
			}
		}
	}

	
	public void extractAlignmentFromOntology(Object onto1, Object onto2) {
		OWLOntology ontology1 = (OWLOntology)onto1;
		OWLOntology ontology2 = (OWLOntology)onto2;
		String rendererClass = "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor";
		
		URIAlignment alignment = new URIAlignment();
		try {
			alignment.init(ontology1.getOntologyID().getOntologyIRI().toURI(), ontology2.getOntologyID().getOntologyIRI().toURI());
		} catch (AlignmentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			
			for (OWLClass c1 : ontology1.getClassesInSignature()) {
				URI c1URI = c1.getIRI().toURI();
				for (OWLClassExpression ex : c1.getEquivalentClasses(ontology1)) {
					if (!ex.isAnonymous()) {
						URI c2URI = ex.asOWLClass().getIRI().toURI();
						if (c2URI.toString().contains("cidoc"))
							alignment.addAlignCell(c1URI, c2URI, "=", 1.0);
					}
				}
			}
			for (OWLObjectProperty p1 : ontology1.getObjectPropertiesInSignature()) {
				URI p1URI = p1.getIRI().toURI();
				for (OWLObjectPropertyExpression ex : p1.getEquivalentProperties(ontology1)) {
					if (!ex.isAnonymous()) {
						URI p2URI = ex.asOWLObjectProperty().getIRI().toURI();
						if (p2URI.toString().contains("cidoc"))
							alignment.addAlignCell(p1URI, p2URI, "=", 1.0);
					}
				}
				for (OWLObjectPropertyExpression ex : p1.getSuperProperties(ontology1)) {
					if (!ex.isAnonymous()) {
						URI p2URI = ex.asOWLObjectProperty().getIRI().toURI();
						if (p2URI.toString().contains("cidoc"))
							alignment.addAlignCell(p1URI, p2URI, "=", 1.0);
					}
				}
			}
			for (OWLDataProperty p1 : ontology1.getDataPropertiesInSignature()) {
				URI p1URI = p1.getIRI().toURI();
				for (OWLDataPropertyExpression ex : p1.getEquivalentProperties(ontology1)) {
					if (!ex.isAnonymous()) {
						URI p2URI = ex.asOWLDataProperty().getIRI().toURI();
						if (p2URI.toString().contains("cidoc"))
							alignment.addAlignCell(p1URI, p2URI, "=", 1.0);
					}
				}
				for (OWLDataPropertyExpression ex : p1.getSuperProperties(ontology1)) {
					if (!ex.isAnonymous()) {
						URI p2URI = ex.asOWLDataProperty().getIRI().toURI();
						if (p2URI.toString().contains("cidoc"))
							alignment.addAlignCell(p1URI, p2URI, "=", 1.0);
					}
				}
			}
			
			AlignmentVisitor renderer;
			OutputStream stream;
		    stream = new FileOutputStream( "culture-align.rdf" );
		    
		    PrintWriter writer = new PrintWriter (
				  new BufferedWriter(
				       new OutputStreamWriter( stream, "UTF-8" )), true);

		    // Result printing (to be reimplemented with a default value)
		    try {
			Class[] cparams = { PrintWriter.class };
			Constructor rendererConstructor = Class.forName(rendererClass).getConstructor( cparams );
			    Object[] mparams = { (Object)writer };
			    renderer = (AlignmentVisitor) rendererConstructor.newInstance( mparams );
		    } catch (Exception ex) {
			throw ex;
		    }
		    
		    // Output
		    alignment.render(renderer);
		    if (writer != null) {
		    	writer.flush();
		    	writer.close();
		    }
			
			/*for (Object o : onto1.getClasses()) {
				
				for (Object c : onto1.getProperties(o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED)) {
					if (onto1.getEntityURI(c).toString().trim().equals("http://www.w3.org/2002/07/owl#equivalentClass")) {
						//alignment.addAlignCell(onto1.getEntityURI(o), onto2.getEntityURI(c), "=", 1.0);
					}
				}
				
			}
			for (Object o : onto1.getProperties()) {
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

