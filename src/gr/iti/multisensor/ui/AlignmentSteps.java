package gr.iti.multisensor.ui;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import gr.iti.multisensor.matrix.Constants;
import gr.iti.multisensor.matrix.HungarianMethod;
import gr.iti.multisensor.matrix.MSMappingList;
import gr.iti.multisensor.matrix.MSMappingPair;
import gr.iti.multisensor.matrix.MSSimilarityMatrix;
import gr.iti.multisensor.matrix.MSWeighting;
import gr.iti.multisensor.ui.utils.Constants.AlignmentParams;
import gr.iti.multisensor.ui.utils.UtilClass;

public class AlignmentSteps {
	URI onto1 = null;
	URI onto2 = null;
	AlignmentProcess result = null;
	String cutMethod = "hard";
	String initName = null;
	Alignment init = null;
	//String alignmentClassName = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
	String alignmentClassName = "fr.inrialpes.exmo.align.impl.alg.StringSimAlignment";
	String rendererClass = "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor";
	PrintWriter writer = null;
	AlignmentVisitor renderer = null;
	double threshold = 0.2;
	
	List<MSSimilarityMatrix> similarityMatrixList = new ArrayList<MSSimilarityMatrix>();
	
	public AlignmentProcess getAlignment() {
		return result;
	}
	
	public void setRendererClass(String r) {
		rendererClass = r;
	}
	
	public void setAlignmentClassName(String i) {
		alignmentClassName = i;
	}
	
	public void setThreshold(double t) {
		threshold = t;
	}
	public double getThreshold() {
		return threshold;
	}
	
	public void clearSimilarityMatrix() {
		similarityMatrixList.clear();
	}
	
	public void setOnto1(String onto1String) {
		try {
			onto1 = new URI(onto1String);
		} catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
	}
	public LoadedOntology<Object> getOntology1() {
		if (result != null) {
			if (result instanceof DistanceAlignment) {
				DistanceAlignment da = (DistanceAlignment)result;
				return da.ontology1();
			}
		}
		return null;
	}
	
	public void setOnto2(String onto2String) {
		try {
			onto2 = new URI(onto2String);
		} catch (URISyntaxException e) {
			System.out.println(e.getMessage());
		}
	}
	public LoadedOntology<Object> getOntology2() {
		if (result != null) {
			if (result instanceof DistanceAlignment) {
				DistanceAlignment da = (DistanceAlignment)result;
				return da.ontology2();
			}
		}
			//return (LoadedOntology<Object>)result.getOntology2();
		return null;
	}
	
	public List<MSSimilarityMatrix> getSimilarityMatrixList() {
		return similarityMatrixList;
	}
	
	public String[] splitAlignmentClassNames() {
		return alignmentClassName.split(";");
	}
	
	public void initOntologies() {
		initOntologies(alignmentClassName);
	}
	public void initOntologies(String alignmentClassN) {
		try {
			Class<?> alignmentClass = Class.forName( alignmentClassN );
		    Class[] cparams = {};
		    Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		    Object[] mparams = {};
		    result = (AlignmentProcess)alignmentConstructor.newInstance( mparams );
		    result.init( onto1, onto2 );
		}catch (Exception e) {
			e.printStackTrace();
		}
	    
	}
	
	public List<MSMappingList> performWeighting() {
		List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		//mappingLists.addAll(myOverallMappings(matList, 2.5));
		
		MSWeighting weighting = new MSWeighting();
		weighting.init(similarityMatrixList);
		weighting.calcAndApplyWeightsToMatrixList();
		//similarityMatrixList = weighting.getSimilarityMatrixList();
		
		if (threshold < 0)
			threshold = similarityMatrixList.size()/5.0;
		
		UtilClass.msg(getClass(), "matlistSize="+similarityMatrixList.size()+", threshold is: "+threshold+"\n");
		
		mappingLists.addAll(msOverallMappings(similarityMatrixList, threshold));
		
		return mappingLists;
	}
	
	public void performMatches(List<AlignmentParams> list) {
		for (AlignmentParams p : list) {
			performMatch(p);
		}
	}
	
//	public void performMatches() {
//		for (String alignmentClassName : splitAlignmentClassNames()) {
//			performMatch(alignmentClassName);
//		}
//	}
	
	public void performMatch(AlignmentParams p) {
		performMatch(p.getClass_(), p.getProps());
	}
	
	public void performMatch(String alignmentClassName, Map<String, String> props) {
		alignmentClassName = alignmentClassName.trim();
    	if (!alignmentClassName.equals("")) {
    		initOntologies(alignmentClassName);
		    try {
				long time = System.currentTimeMillis();
			    //result.align(  init, parameters ); // add opts
			    Properties properties = new Properties();
			    if (props != null) {
			    	for (String key : props.keySet()) {
			    		properties.setProperty(key, props.get(key));
			    	}
			    }
			    
			    // Compute alignment
			    result.align(null, properties);
			    
			    long newTime = System.currentTimeMillis();
			    result.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );
		
			    // Thresholding
			    //if (threshold != 0) result.cut( cutMethod, threshold );
			    
			    MSSimilarityMatrix matrix = new MSSimilarityMatrix();
			    matrix.setMatcherName(alignmentClassName);
			    //Get match score matrix
			    if (result instanceof DistanceAlignment) {
			    	try {
				    	DistanceAlignment res1 = (DistanceAlignment)result;
				    	double[][] clMatrix = ((MatrixMeasure)res1.getSimilarity()).clmatrix;
				    	double[][] classMatrix = new double[clMatrix.length][clMatrix[0].length];
				    	double[][] prMatrix = ((MatrixMeasure)res1.getSimilarity()).prmatrix;
				    	double[][] propMatrix = new double[prMatrix.length][prMatrix[0].length];
				    	double[][] indMatrix = ((MatrixMeasure)res1.getSimilarity()).indmatrix;
				    	
				    	for (Object cl1 : res1.ontology1().getClasses()) {
				    		//URI uri1 = res1.ontology1().getEntityURI(cl1);
				    		matrix.addToSrcClasses(cl1);
				    	}
				    	for (Object cl2 : res1.ontology2().getClasses()) {
				    		//URI uri2 = res1.ontology2().getEntityURI(cl2);
				    		matrix.addToTgtClasses(cl2);
				    	}
				    	//System.out.println("Size of matrix:"+matrix.getSrcClasses().size()+", "+matrix.getTgtClasses().size());
				    	for (int i=0;i<matrix.getSrcClasses().size();i++) {
				    		for (int j=0;j<matrix.getTgtClasses().size();j++) {
				    			//URI cl1 =  matrix.getSrcClasses().get(i);
				    			//URI cl2 =  matrix.getTgtClasses().get(j);
				    			Object cl1 =  matrix.getSrcClasses().get(i);
				    			Object cl2 =  matrix.getTgtClasses().get(j);
				    			
				    			//int i1 = ((MatrixMeasure)res1.getSimilarity()).classlist1.get(res1.ontology1().getEntity(cl1)).intValue();
				    			//int j1 = ((MatrixMeasure)res1.getSimilarity()).classlist2.get(res1.ontology2().getEntity(cl2)).intValue();
				    			int i1 = ((MatrixMeasure)res1.getSimilarity()).classlist1.get(cl1).intValue();
				    			int j1 = ((MatrixMeasure)res1.getSimilarity()).classlist2.get(cl2).intValue();
				    			
				    			if (res1.getSimilarity().getSimilarity())
				    				classMatrix[i][j] = clMatrix[i1][j1];
				    			else
				    				classMatrix[i][j] = 1.0 - clMatrix[i1][j1];
				    		}
				    	}
				    	matrix.assignClassMatrix(classMatrix);
				    	
				    	for (Object pr1 : res1.ontology1().getProperties()) {
				    		//URI uri1 = res1.ontology1().getEntityURI(pr1);
				    		matrix.addToSrcProperties(pr1);
				    	}
				    	for (Object pr2 : res1.ontology2().getProperties()) {
				    			//URI uri2 = res1.ontology2().getEntityURI(pr2);
				    			matrix.addToTgtProperties(pr2);
				    	}
				    	for (int i=0;i<matrix.getSrcProperties().size();i++) {
				    		for (int j=0;j<matrix.getTgtProperties().size();j++) {
				    			Object pr1 =  matrix.getSrcProperties().get(i);
				    			Object pr2 =  matrix.getTgtProperties().get(j);
				    			
				    			//int i1 = ((MatrixMeasure)res1.getSimilarity()).proplist1.get(res1.ontology1().getEntity(pr1)).intValue();
				    			//int j1 = ((MatrixMeasure)res1.getSimilarity()).proplist2.get(res1.ontology2().getEntity(pr2)).intValue();
				    			int i1 = ((MatrixMeasure)res1.getSimilarity()).proplist1.get(pr1).intValue();
				    			int j1 = ((MatrixMeasure)res1.getSimilarity()).proplist2.get(pr2).intValue();
				    			
				    			if (res1.getSimilarity().getSimilarity())
				    				propMatrix[i][j] = prMatrix[i1][j1];
				    			else
				    				propMatrix[i][j] = 1.0 - prMatrix[i1][j1];
				    		}
				    	}
				    	matrix.assignPropertyMatrix(propMatrix);
			    	} catch (OntowrapException e) {
			    		e.printStackTrace();
			    	}
			    }
			    else if (result instanceof ObjectAlignment) {
			    	UtilClass.msg(getClass(), "\nresult is ObjectAlignment\n");
			    }
			    else if (result instanceof URIAlignment) {
			    	UtilClass.msg(getClass(), "\nresult is URIAlignment\n");
			    }
			    
			    similarityMatrixList.add(matrix);
		    } catch (AlignmentException e) {
		    	e.printStackTrace();
		    }
    	}
	}
	
	public List<MSMappingList> msOverallMappings(List<MSSimilarityMatrix> matList, double threshold) {
		List<MSMappingList> mappingLists = new ArrayList<MSMappingList>();
		//if (this.threshold != -1)
		threshold = this.threshold;
		
		if (matList.size() > 0) {
			double[][] classMatrix = new double[matList.get(0).getClassMatrix().length][matList.get(0).getClassMatrix()[0].length]; 
			double[][] propertyMatrix = new double[matList.get(0).getPropertyMatrix().length][matList.get(0).getPropertyMatrix()[0].length];
			
			for (MSSimilarityMatrix mat : matList) {
				double[][] classMatrixTemp = mat.getClassMatrix();
				double[][] propertyMatrixTemp = mat.getPropertyMatrix();
				
				for (int i=0;i<classMatrix.length;i++)
					for (int j=0;j<classMatrix[0].length;j++)
						classMatrix[i][j] += classMatrixTemp[i][j];
				
				for (int i=0;i<propertyMatrix.length;i++)
					for (int j=0;j<propertyMatrix[0].length;j++)
						propertyMatrix[i][j] += propertyMatrixTemp[i][j];
			}
			
			double[][] classMatrixCopy = new double[classMatrix.length][classMatrix[0].length];
			for (int i=0;i<classMatrix.length;i++)
				for (int j=0;j<classMatrix[0].length;j++)
					classMatrixCopy[i][j] = classMatrix[i][j];
			
			double[][] propertyMatrixCopy = new double[propertyMatrix.length][propertyMatrix[0].length];
			for (int i=0;i<propertyMatrix.length;i++)
				for (int j=0;j<propertyMatrix[0].length;j++)
					propertyMatrixCopy[i][j] = propertyMatrix[i][j];
			
			System.out.println("calling hungarian method for classes");			
			int[][] result = HungarianMethod.callHungarianMethod(classMatrix, matList.get(0).getSrcClasses().size(), matList.get(0).getTgtClasses().size() );
			MSMappingList mappingList = new MSMappingList("Overall-classes-mappings");
			for( int i=0; i < result.length ; i++ ){
			    // The matrix has been destroyed
				double val = classMatrixCopy[result[i][0]][result[i][1]];
			    if( val > threshold ){
			    	double scores[] = new double[matList.size()];
			    	for (int j=0;j<scores.length;j++) {
			    		double[][] m1 = matList.get(j).getClassMatrix();
			    		scores[j] = m1[result[i][0]][result[i][1]];
			    		//scores[j] = classMatrixCopy[result[i][0]][result[i][1]];
			    		//System.out.println("---Matcher name "+j+": "+matList.get(j).getMatcherName());
			    	}
			    	//mappingList.addMappingPair(new MyMappingPair(i, matList.get(0).getSrcClasses().get(result[i][0]), matList.get(0).getTgtClasses().get(result[i][1]), "=", val));
			    	mappingList.addMappingPair(new MSMappingPair(i, matList.get(0).getSrcClasses().get(result[i][0]), matList.get(0).getTgtClasses().get(result[i][1]), "=", val/matList.size() ,scores));
			    }
			}
			// code for mismatches used for training samples - random forest
			//List<MyMappingPair> mismatches = getNonMatchingPairs(matList, result, result.length, Constants.CLASSES);
			//mappingList.addMappingPairs(mismatches);
			
			mappingLists.add(mappingList);
			
			System.out.println("calling hungarian method for properties");
			if (matList.get(0).getSrcProperties().size() > 0 && matList.get(0).getTgtProperties().size() > 0) {
				result = HungarianMethod.callHungarianMethod(propertyMatrix, matList.get(0).getSrcProperties().size(), matList.get(0).getTgtProperties().size() );
				MSMappingList mappingList2 = new MSMappingList("Overall-properties-mappings");
				for( int i=0; i < result.length ; i++ ){
				    // The matrix has been destroyed
					double val = propertyMatrixCopy[result[i][0]][result[i][1]];
				    if( val > threshold ){
				    	double scores[] = new double[matList.size()];
				    	for (int j=0;j<scores.length;j++) {
				    		double[][] m1 = matList.get(j).getPropertyMatrix();
				    		scores[j] = m1[result[i][0]][result[i][1]];
				    		//scores[j] = propertyMatrixCopy[result[i][0]][result[i][1]];
				    	}
				    	//mappingList2.addMappingPair(new MyMappingPair(i, matList.get(0).getSrcProperties().get(result[i][0]), matList.get(0).getTgtProperties().get(result[i][1]), "=", val));
				    	mappingList2.addMappingPair(new MSMappingPair(i, matList.get(0).getSrcProperties().get(result[i][0]), matList.get(0).getTgtProperties().get(result[i][1]), "=", val/matList.size(), scores));
				    }
				}
				// code for mismatches used for training samples - random forest
				//List<MyMappingPair> mismatches2 = getNonMatchingPairs(matList, result, result.length, Constants.PROPERTIES);
				//mappingList2.addMappingPairs(mismatches2);
				
				mappingLists.add(mappingList2);
			}
		}
		
		return mappingLists;
	}
	
	//this code is used for training of machine learning methods
	/*public List<MyMappingPair> getNonMatchingPairs(List<MySimilarityMatrix> matList, int[][] result, int numOfPairs, int resourceType) {
		double scores[];
		List<MyMappingPair> pairList = new ArrayList<MyMappingPair>();
		List<URI> srcResourceList = null;
		List<URI> tgtResourceList = null;
		
		if (resourceType == Constants.PROPERTIES) {
			srcResourceList = matList.get(0).getSrcProperties();
			tgtResourceList = matList.get(0).getTgtProperties();
		}
		else {
			srcResourceList = matList.get(0).getSrcClasses();
			tgtResourceList = matList.get(0).getTgtClasses();
		}
		
		for (int i=0;i<numOfPairs;i++) {
	    	int i1 = (int)Math.round(Math.random()*(srcResourceList.size()-1));
	    	int j1 = (int)Math.round(Math.random()*(tgtResourceList.size()-1));
	    	boolean exists = false;
	    	for (int k=0;k<result.length;k++) {
	    		if (result[k][0] == i1 && result[k][1] == j1) {
	    			exists = true;
	    			break;
	    		}
	    	}

	    	if (!exists) {
	    		scores = new double[matList.size()];
	    		double[][] m1 = null;
	    		for (int j=0;j<scores.length;j++) {
		    		if (resourceType == Constants.PROPERTIES)
		    			m1 = matList.get(j).getPropertyMatrix();
		    		else
		    			m1 = matList.get(j).getClassMatrix();
		    		
		    		scores[j] = m1[i1][j1];
		    	}
	    		pairList.add(new MyMappingPair(result.length+i, srcResourceList.get(i1), tgtResourceList.get(j1), "!=", -1 ,scores));
	    	}
	    	else {
	    		j1++;
	    		if (j1 > (matList.get(0).getTgtClasses().size()-1))
	    			j1 -= 2;
	    		scores = new double[matList.size()];
	    		double[][] m1 = null;
	    		for (int j=0;j<scores.length;j++) {
	    			if (resourceType == Constants.PROPERTIES)
		    			m1 = matList.get(j).getPropertyMatrix();
		    		else
		    			m1 = matList.get(j).getClassMatrix();
	    			
		    		scores[j] = m1[i1][j1];
		    	}
	    		pairList.add(new MyMappingPair(result.length+i, srcResourceList.get(i1), tgtResourceList.get(j1), "!=", -1 ,scores));
	    	}
		}
		return pairList;
	}*/
	
	
}
