package com.example.yahooimg.similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Reader {
	public double[][] CSV2Array(String csvFile, int dims, int skip) throws FileNotFoundException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(csvFile));
		String line = "";
		
		ArrayList<double[]> dd = new ArrayList<double[]>();
		
		while ((line = in.readLine()) != null) {
			String[] parts = line.split(",");
			double[] dimVector = new double[dims];
			for (int i=skip;i<dims+skip;i++) {
				dimVector[i-skip] = Double.valueOf(parts[i]).doubleValue();
			}
			dd.add(dimVector);
		}
		in.close();
		
		double[][] matrixIndex = new double[dd.size()][dims];
		for (int i=0;i<dd.size();i++) {
			matrixIndex[i] = dd.get(i);
		}
				
		return matrixIndex;
	}
	
	public void printArray(double[][] array) {
		for (int i=0;i<array.length;i++) {
			System.out.println();
			for (int j=0;j<array[0].length;j++)
				System.out.print(array[i][j]+", ");
		}
	}
}
