package gr.iti.multisensor.matrix;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSSimilarityMatrix {
	final static Logger logger = LoggerFactory.getLogger( MSSimilarityMatrix.class );
	private String matcherName;
	//private double[][] matrix;
	
	private double[][] classMatrix;
	private double[][] propertyMatrix;
	private List<Object> srcClasses;
	private List<Object> tgtClasses;
	private List<Object> srcProperties;
	private List<Object> tgtProperties;
	
	
	public MSSimilarityMatrix(Iterator<Object> classSrc, Iterator<Object> classTgt, Iterator<Object> propSrc, Iterator<Object> propTgt, String name) {
		init();
		if (classSrc != null) {
			addToSrcClasses(classSrc);
		}
		if (classTgt != null) {
			addToTgtClasses(classTgt);
		}
		if (propSrc != null) {
			addToSrcProperties(propSrc);
		}
		if (propTgt != null) {
			addToTgtProperties(propTgt);
		}
		
		classMatrix = new double[srcClasses.size()][tgtClasses.size()];
		propertyMatrix = new double[srcProperties.size()][tgtProperties.size()];
		
		matcherName = name;
	}
	
	public MSSimilarityMatrix() {
		init();
	}
	
	public void init() {
		//srcResources = new ArrayList<OntResourceStr>();
		//tgtResources = new ArrayList<OntResourceStr>();
		
		srcClasses = new ArrayList<Object>();
		srcProperties = new ArrayList<Object>();
		
		tgtClasses = new ArrayList<Object>();
		tgtProperties = new ArrayList<Object>();
		
		matcherName = "";
	}
	
	public void setMatcherName(String name) {
		this.matcherName = name;
	}
	
	/*public void addToSrcResources(Iterator<OntResourceStr> iter) {
		for (;iter.hasNext();) {
			srcResources.add(iter.next());
		}
	}*/
	
	public boolean containsNS(Object str) {
		if (str.toString().contains("http://xmlns.com") || 
			//str.toString().contains("http://purl.org") ||
			str.toString().contains("http://www.w3.org"))
			
			return true;
		
		return false;
	}
	
	public void addToSrcClasses(Object res) {
		if (!containsNS(res))
			srcClasses.add(res);
	}
	public void addToSrcClasses(Iterator<Object> iter) {
		for (;iter.hasNext();) {
			Object res = iter.next();
			if (!containsNS(res))
				srcClasses.add(res);
		}
	}
	public void addToSrcProperties(Object res) {
		if (!containsNS(res))
			srcProperties.add(res);
	}
	public void addToSrcProperties(Iterator<Object> iter) {
		for (;iter.hasNext();) {
			Object res = iter.next();
			if (!containsNS(res))
				srcProperties.add(res);
		}
	}
		
	/*public void addToTgtResources(Iterator<OntResourceStr> iter) {
		for (;iter.hasNext();) {
			tgtResources.add(iter.next());
		}
	}*/
	public void addToTgtClasses(Object res) {
		if (!containsNS(res))
			tgtClasses.add(res);
	}
	public void addToTgtClasses(Iterator<Object> iter) {
		for (;iter.hasNext();) {
			Object res = iter.next();
			if (!containsNS(res))
				tgtClasses.add(res);
		}
	}
	public void addToTgtProperties(Object res) {
		if (!containsNS(res))
			tgtProperties.add(res);
	}
	public void addToTgtProperties(Iterator<Object> iter) {
		for (;iter.hasNext();) {
			Object res = iter.next();
			if (!containsNS(res))
				tgtProperties.add(res);
		}
	}
	
	public String getMatcherName() {
		return matcherName;
	}
	
	/*public double[][] getMatrix() {
		return matrix;
	}*/
	public double[][] getClassMatrix() {
		return classMatrix;
	}
	public double[][] getPropertyMatrix() {
		return propertyMatrix;
	}
		
	/*public void assignMatrix(double[][] matrix) {
		if (matrix.length == this.matrix.length && matrix[0].length == this.matrix[0].length) {
			for (int i=0;i<matrix.length;i++)
				for (int j=0;j<matrix[0].length;j++)
					this.matrix[i][j] = matrix[i][j]; 
		}
	}*/
	public void assignClassMatrix(double[][] matrix) {
		if (matrix.length > 0 && matrix[0].length > 0) {
			this.classMatrix = new double[matrix.length][matrix[0].length];
			for (int i=0;i<matrix.length;i++)
				for (int j=0;j<matrix[0].length;j++)
					this.classMatrix[i][j] = matrix[i][j];
		}
		else
			logger.info("Class matrix to assign has zero dimension(s)");
	}
	public void assignPropertyMatrix(double[][] matrix) {
		if (matrix.length > 0 && matrix[0].length > 0) {
			this.propertyMatrix = new double[matrix.length][matrix[0].length];
			for (int i=0;i<matrix.length;i++)
				for (int j=0;j<matrix[0].length;j++)
					this.propertyMatrix[i][j] = matrix[i][j]; 
		}
		else
			logger.info("Property matrix to assign has zero dimension(s)");
	}
	
	/*public List<OntResourceStr> getSrcResources() {
		return srcResources;
	}*/
	public List<Object> getSrcClasses() {
		return srcClasses;
	}
	public List<Object> getSrcProperties() {
		return srcProperties;
	}
	
	/*public List<OntResourceStr> getTgtResources() {
		return tgtResources;
	}*/
	public List<Object> getTgtClasses() {
		return tgtClasses;
	}
	public List<Object> getTgtProperties() {
		return tgtProperties;
	}
	
	/*public void setValue(OntResourceStr src, OntResourceStr tgt, double val) {
		int i = srcResources.indexOf(src);
		int j = tgtResources.indexOf(tgt);
		
		if (i>-1 && j>-1)
			matrix[i][j] = val;
	}*/
	public void setClassMatrixValue(Object srcClass, Object tgtClass, double val) {
		int i = srcClasses.indexOf(srcClass);
		int j = tgtClasses.indexOf(tgtClass);
		
		if (i>-1 && j>-1)
			classMatrix[i][j] = val;
		else {
			//second try. Used for external matchers where only class ID is registered in MUs
			i = getIndexFromResourceID(srcClass.toString(), srcClasses);
			j = getIndexFromResourceID(tgtClass.toString(), tgtClasses);
			if (i>-1 && j>-1)
				classMatrix[i][j] = val;
		}
	}
	public void setPropertyMatrixValue(Object srcProperty, Object tgtProperty, double val) {
		int i = srcProperties.indexOf(srcProperty);
		int j = tgtProperties.indexOf(tgtProperty);
		
		if (i>-1 && j>-1)
			propertyMatrix[i][j] = val;
		else {
			//second try. Used for external matchers
			i = getIndexFromResourceID(srcProperty.toString(), srcProperties);
			j = getIndexFromResourceID(tgtProperty.toString(), tgtProperties);
			if (i>-1 && j>-1)
				propertyMatrix[i][j] = val;
		}
	}
	
	public int getIndexFromResourceID(String id, List<Object> list) {
		for (int i=0;i<list.size();i++) {
			if (list.get(i).toString().equals(id))
				return i;
		}
		
		return -1;
	}
	
	/*public double getValue(OntResourceStr src, OntResourceStr tgt) {
		int i = srcResources.indexOf(src);
		int j = tgtResources.indexOf(tgt);
		
		if (i>-1 && j>-1)
			return matrix[i][j];
		
		return -1;
	}*/
	public double getClassMatrixValue(Object srcClass, Object tgtClass) {
		int i = srcClasses.indexOf(srcClass);
		int j = tgtClasses.indexOf(tgtClass);
		
		if (i>-1 && j>-1)
			return classMatrix[i][j];
		
		return -1;
	}
	public double getPropertyMatrixValue(Object srcProperty, Object tgtProperty) {
		int i = srcProperties.indexOf(srcProperty);
		int j = tgtProperties.indexOf(tgtProperty);
		
		if (i>-1 && j>-1)
			return propertyMatrix[i][j];
		
		return -1;
	}
	
	protected String removeNamespace(Object str) {
		String s = str.toString();
		int pos = s.length()-1;
		while (pos > 0) {
			if (s.charAt(pos)!='/' && s.charAt(pos)!= '#')
				pos--;
			else 
				break;
		}
		
		return s.substring(pos+1, s.length());
	}
	
	protected String getNamespace(Object str) {
		String s = str.toString();
		int pos = s.length()-1;
		while (pos > 0) {
			if (s.charAt(pos)!='/' && s.charAt(pos)!= '#') {
				pos--;
			}
			else 
				break;
		}
		
		return s.substring(0, pos+1);
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer(500);
		NumberFormat formatter = new DecimalFormat("#0.00000");     
		
		buff.append("Matrix for matcher: ").append(matcherName).append("\n");
		if (srcClasses.size() == 0 || tgtClasses.size() == 0)
			return buff.append("Size is zero\n").toString();
		buff.append("Source namespace: ").append(getNamespace(srcClasses.get(0))).append("\n");
		buff.append("Target namespace: ").append(getNamespace(tgtClasses.get(0))).append("\n");
		buff.append("Classes similarity scores\n");
		buff.append(";");
		
		for (int i=0;i<tgtClasses.size();i++)
			buff.append(removeNamespace(tgtClasses.get(i))).append(";");
		buff.append("\n");
		for (int i=0;i<srcClasses.size();i++) {
			buff.append(removeNamespace(srcClasses.get(i))).append(";");
			for (int j=0;j<tgtClasses.size();j++) {
				buff.append(formatter.format(classMatrix[i][j])).append(";");
			}
			buff.append("\n");
		}
		buff.append("\n\nProperties similarity scores\n");
		buff.append(";");
		
		for (int i=0;i<tgtProperties.size();i++)
			buff.append(removeNamespace(tgtProperties.get(i))).append(";");
		buff.append("\n");
		for (int i=0;i<srcProperties.size();i++) {
			buff.append(removeNamespace(srcProperties.get(i))).append(";");
			for (int j=0;j<tgtProperties.size();j++) {
				buff.append(formatter.format(propertyMatrix[i][j])).append(";");
			}
			buff.append("\n");
		}
		
		/*for (int i=0;i<tgtResources.size();i++)
			buff.append(removeNamespace(tgtResources.get(i))).append(";");
		buff.append("\n");
		for (int i=0;i<srcResources.size();i++) {
			buff.append(removeNamespace(srcResources.get(i))).append(";");
			for (int j=0;j<tgtResources.size();j++) {
				buff.append(formatter.format(matrix[i][j])).append(";");
			}
			buff.append("\n");
		}*/
		
		return buff.toString();
	}
}
