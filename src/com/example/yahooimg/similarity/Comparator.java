package com.example.yahooimg.similarity;

import java.util.ArrayList;
import java.util.List;

public class Comparator {
	public static double eucledianDistance(double[] source, double[] target) {
		if (source.length != target.length)
			return 0;
		
		double distance = 0.0;
		for (int i=0;i<source.length;i++) {
			distance += (source[i]-target[i])*(source[i]-target[i]);
		}
		distance = Math.sqrt(distance);
		
		return distance;
	}
	
	public static double eucledianDistance(double[][] source, double[][] target) {
		if (source[0].length != target[0].length)
			return -1;
		
		double distance = 0.0;
		double cumDistance = 0.0;
		
		for (int i=0;i<source.length;i++) {
			for (int j=0;j<target.length;j++) {
				for (int k=0;k<target[j].length;k++) {
					distance += (source[i][k]-target[j][k])*(source[i][k]-target[j][k]);
				}
				distance = Math.sqrt(distance);
				cumDistance += distance;
				distance = 0.0;
			}
		}
		
		return cumDistance;
	}
	
	public static double cosineSimilarity(double[] source, double[] target) {
		if (source.length != target.length)
			return 0;
		
		double normSource = norm2(source);
		double normTarget = norm2(target);
		
		if (normSource==0 || normTarget==0)
			return 0;
		
		double similarity = 0;
		
		for (int i=0;i<source.length;i++) {
			similarity += source[i]*target[i];
		}

		similarity = similarity/(normSource*normTarget);
		
		similarity = similarity > 1.0 ? 1.0 : similarity;
		similarity = similarity < -1.0 ? -1.0 : similarity;
		
		//angular similarity
		similarity = 1.0 - (Math.acos(similarity)/Math.PI);
		
		return similarity;
	}
	
	public static double cosineSimilarity(double[][] source, double[][] target) {
		if (source.length==0 || target.length==0)
			return 0;
		
		if (source[0].length != target[0].length)
			return 0;
		
		double cumSimilarity = 0.0;
		
		for (int i=0;i<source.length;i++) {
			for (int j=0;j<target.length;j++) {
				cumSimilarity += cosineSimilarity(source[i], target[j]);
			}
		}
		
		return cumSimilarity/(double)(source.length*target.length);
	}
	
	/*public static double cosineDistance(double[][] source, double[][] target) {
		if (source[0].length != target[0].length)
			return -1;
		
		double similarity = 0.0;
		double cumSimilarity = 0.0;
		
		for (int i=0;i<source.length;i++) {
			double normSource = norm2(source[i]);
			for (int j=0;j<target.length;j++) {
				double normTarget = norm2(target[j]);
				for (int k=0;k<target[j].length;k++) {
					similarity += source[i][k]*target[j][k];
				}
				
				similarity = similarity/(normSource*normTarget);
				
				//angular similarity
				similarity = 1.0 - (Math.acos(similarity)/Math.PI);
				
				//convert it to distance measure
				cumSimilarity += 1.0 - similarity;
				similarity = 0.0;
			}
		}
		
		return cumSimilarity;
	}*/
	
	/*public static double jaccardSimilarity(double[][] source, double[][] target) {
		if (source[0].length != target[0].length)
			return -1;
		
		double cumSum = 0.0;
		
		for (int i=0;i<source.length;i++) {
			double sumMin = 0;
			double sumMax = 0;
			for (int j=0;j<target.length;j++) {
				for (int k=0;k<target[j].length;k++) {
					sumMin += Math.abs(Math.min(source[i][k], target[j][k]));
					sumMax += Math.abs(Math.max(source[i][k], target[j][k]));
				}
			}
			cumSum += sumMin/sumMax;
		}
		
		return cumSum;
	}*/
	
	public static double jaccardSetSimilarity(double[][] source, double[][] target) {
		if (source.length==0 || target.length==0)
			return 0;
		
		if (source[0].length != target[0].length)
			return 0;
		
		//make the intersection list
		List<String> iList = new ArrayList<String>(source.length+target.length);
		
		double similarity = 0;
		int nearDuplicates = 0;
		double threshold = 0.33;
				
		for (int i=0;i<source.length;i++) {
			for (int j=0;j<target.length;j++) {
				double sim = cosineSimilarity(source[i], target[j]);
				if ((1.0 - sim) <= threshold) {
					//we have near duplicates. Add them to iList
					String indexSource = "s"+i;
					String indexTarget = "t"+j;
					if (!iList.contains(indexSource))
						iList.add(indexSource);
					if (!iList.contains(indexTarget))
						iList.add(indexTarget);
					
					nearDuplicates++;
					
				}
			}
		}

		//similarity = (double)nearDuplicates/(double)(source.length*target.length);
		similarity = (double)iList.size()/(double)(source.length+target.length);
		
		return similarity;
	}
	
	public static double norm2(double[] vector) {
		double value = 0.0;
		for (int i=0;i<vector.length;i++)
			value += vector[i]*vector[i];
		
		return Math.sqrt(value);
	}
}
