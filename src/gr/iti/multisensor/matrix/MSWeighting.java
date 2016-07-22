package gr.iti.multisensor.matrix;

import java.util.ArrayList;
import java.util.List;

public class MSWeighting {
	List<double[][][]> weightsList; //3D weight matrix. [0][][] if for classes similarity and [1][][] for properties
	List<MSSimilarityMatrix> matList;
	
	public void init(List<MSSimilarityMatrix> matList) {
		this.matList = new ArrayList<MSSimilarityMatrix>(matList.size());
		for (int i=0;i<matList.size();i++)
			this.matList.add(matList.get(i));
		
		weightsList = new ArrayList<double[][][]>(matList.size());
	}
	
	private MSSimilarityMatrix getSimilarityMatrix(int index) {
		if (index > 0 && index < matList.size())
			return matList.get(index);
		
		return null;
	}
	
	public void clearMatList() {
		this.matList = new ArrayList<MSSimilarityMatrix>();
	}
	
	private MSSimilarityMatrix getSimilarityMatrix(String matcher) {
		for (MSSimilarityMatrix matrix : matList) {
			if (matrix.getMatcherName().equals(matcher))
				return matrix;
		}
		
		return null;
	}
	
	public List<MSSimilarityMatrix> getSimilarityMatrixList() {
		return matList;
	}
	
	public double[][][] setMatcherWeights(String matcher) {
		try {
			return setMatcherWeights(getSimilarityMatrix(matcher));
		}
		catch (Exception e) {
			System.err.println("Matcher '"+matcher+"': "+e.getMessage());
		}
		
		return null;
	}
	
	public double[][][] setMatcherWeights(int index) {
		try {
			return setMatcherWeights(getSimilarityMatrix(index));
		} catch (Exception e) {
			System.err.println("Matcher "+index+": "+e.getMessage());
		}
		
		return null;
	}
	
	public double[][][] setMatcherWeights(MSSimilarityMatrix matrix) throws Exception {
		if (matrix != null) {
			//double[][] mat = matrix.getMatrix();
			double[][] classMatrix = matrix.getClassMatrix();
			double[][] propertyMatrix = matrix.getPropertyMatrix();
			
			double[][][] weights = new double[2][][];
			weights[0] = new double[matrix.getSrcClasses().size()][matrix.getTgtClasses().size()];
			weights[1] = new double[matrix.getSrcProperties().size()][matrix.getTgtProperties().size()];

			//This iz for classezzzz
			//find max values per line
			double[] maxPerLine = findMax(classMatrix);
			//find mean per line excl. max value
			double[] meanPerLine = calcMean(classMatrix, maxPerLine);
			//find total mean incl. all values
			double[] totalMeanPerLine = calcTotalMean(classMatrix);
			//find variance per line excl. max value
			double[] varPerLine = calcVariance(classMatrix, maxPerLine, meanPerLine);
			
			for (int i=0;i<weights[0].length;i++) {
				for (int j=0;j<weights[0][0].length;j++) {
					weights[0][i][j] = 1/(1+Math.exp(-5.0*(classMatrix[i][j]-(meanPerLine[i]+2.0*varPerLine[i])))); //ws4
				}
			}
			
			//This iz for propertiezzzz
			//find max values per line
			maxPerLine = findMax(propertyMatrix);
			//find mean per line excl. max value
			meanPerLine = calcMean(propertyMatrix, maxPerLine);
			//find total mean incl. all values
			totalMeanPerLine = calcTotalMean(propertyMatrix);
			//find variance per line excl. max value
			varPerLine = calcVariance(propertyMatrix, maxPerLine, meanPerLine);
			
			for (int i=0;i<weights[1].length;i++) {
				for (int j=0;j<weights[1][0].length;j++) {
					weights[1][i][j] = 1/(1+Math.exp(-5.0*(propertyMatrix[i][j]-(meanPerLine[i]+2.0*varPerLine[i])))); //ws4
				}
			}
			
			return weights;
		}
		else
			throw new Exception("Similarity matrix is null!");
	}
	
	protected double[] findMax(double[][] matrix) {
		double[] maxPerLine = new double[matrix.length];
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix[0].length;j++) {
				if (maxPerLine[i] < matrix[i][j])
					maxPerLine[i] = matrix[i][j];
			}
		}
		
		return maxPerLine;
	}
	
	//calc mean from all line values
	protected double[] calcTotalMean(double[][] matrix) {
		double[] meanPerLine = new double[matrix.length];
		
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix[0].length;j++) {
				meanPerLine[i] += matrix[i][j];
			}
			if (matrix[0].length > 0)
				meanPerLine[i] /= (matrix[0].length);
			else
				meanPerLine[i] = 0;
		}
		
		return meanPerLine;
	}
		
	//calc mean excluding the maximum value
	protected double[] calcMean(double[][] matrix, double[] maxPerLine) {
		boolean selected = false;
		double[] meanPerLine = new double[matrix.length];
		
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix[0].length;j++) {
				if (matrix[i][j] == maxPerLine[i] && !selected)
					selected = true;
				else {
					meanPerLine[i] += matrix[i][j];
				}
			}
			if (matrix[0].length > 1)
				meanPerLine[i] /= (matrix[0].length-1);
			else if (matrix[0].length == 0)
				meanPerLine[i] = 0;
		}
		
		return meanPerLine;
	}
	
	//calc variance excluding the maximum value
	protected double[] calcVariance(double[][] matrix, double[] maxPerLine, double[] meanPerLine) {
		boolean selected = false;
		double[] varPerLine = new double[matrix.length];
		
		for (int i=0;i<matrix.length;i++) {
			for (int j=0;j<matrix[0].length;j++) {
				if (matrix[i][j] == maxPerLine[i] && !selected)
					selected = true;
				else {
					varPerLine[i] += Math.abs(matrix[i][j] - meanPerLine[i]);
				}
			}
			if (matrix[0].length > 2)
				varPerLine[i] /= (matrix[0].length-2);
			else 
				varPerLine[i] = 0;
		}
		
		return varPerLine;
	}
	
	public void calcAndApplyWeightsToMatrix(MSSimilarityMatrix matrix) {
		try {
			double[][][] weights = setMatcherWeights(matrix);
			
			//classes
			double[][] classSimilarityMatrix = matrix.getClassMatrix();
			if (weights != null) {
				for (int i=0;i<weights[0].length;i++)
					for (int j=0;j<weights[0][0].length;j++)
						classSimilarityMatrix[i][j] = weights[0][i][j]*classSimilarityMatrix[i][j];
				matrix.assignClassMatrix(classSimilarityMatrix);
			}
			
			//properties
			double[][] propertySimilarityMatrix = matrix.getPropertyMatrix();
			if (weights != null) {
				for (int i=0;i<weights[1].length;i++)
					for (int j=0;j<weights[1][0].length;j++)
						propertySimilarityMatrix[i][j] = weights[1][i][j]*propertySimilarityMatrix[i][j];
				matrix.assignPropertyMatrix(propertySimilarityMatrix);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calcAndApplyWeightsToMatrixList() {
		for (int k=0;k<matList.size();k++) {
			try {
				MSSimilarityMatrix matrix = matList.get(k);
				double[][][] weights = setMatcherWeights(matrix);
				weightsList.add(weights);
				
				//classes
				double[][] classSimilarityMatrix = matrix.getClassMatrix();
				if (weights != null) {
					for (int i=0;i<weights[0].length;i++)
						for (int j=0;j<weights[0][0].length;j++) {
							classSimilarityMatrix[i][j] = weights[0][i][j]*classSimilarityMatrix[i][j];
							if (classSimilarityMatrix[i][j] > 1.0)
								classSimilarityMatrix[i][j] = 1.0;
						}
					matrix.assignClassMatrix(classSimilarityMatrix);
				}
				
				//properties
				double[][] propertySimilarityMatrix = matrix.getPropertyMatrix();
				if (weights != null) {
					for (int i=0;i<weights[1].length;i++)
						for (int j=0;j<weights[1][0].length;j++) {
							propertySimilarityMatrix[i][j] = weights[1][i][j]*propertySimilarityMatrix[i][j];
							if (propertySimilarityMatrix[i][j] > 1.0)
								propertySimilarityMatrix[i][j] = 1.0;
						}
					matrix.assignPropertyMatrix(propertySimilarityMatrix);
				}
				matList.set(k, matrix);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
