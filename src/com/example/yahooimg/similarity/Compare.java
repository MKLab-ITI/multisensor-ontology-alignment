package com.example.yahooimg.similarity;

import gr.iti.mklab.visual.examples.FolderIndexingMT;
import gr.iti.mklab.visual.examples.FolderIndexingMT_CommonVectorizer;
import gr.iti.mklab.visual.vectorization.ImageVectorizer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import com.example.yahooimg.ImgService;
import com.example.yahooimg.StHttpRequest;

public class Compare {
	
	public void getImagesForConcepts() {
		ImgService s = new ImgService();
		StHttpRequest httpsRequest = s.authorize();
		
		try {
			s.getImagesForQueries(httpsRequest, Constants.concepts, Constants.baseImageDir);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ImageVectorizer createVectorizer(String[] args) {
		String[] codebookFiles = args[2].split(",");
		String[] numCentroidsString = args[3].split(",");
		int[] numCentroids = new int[numCentroidsString.length];
		for (int i = 0; i < numCentroidsString.length; i++) {
			numCentroids[i] = Integer.parseInt(numCentroidsString[i]);
		}
		String pcaFile = args[4];
		int projectionLength = Integer.parseInt(args[5]);
		int maxImageSizeInPixels = Integer.parseInt(args[6]);
		int numVectorizationThreads = Integer.parseInt(args[7]);
		String featureType = args[8];
		boolean whitening = Boolean.parseBoolean(args[9]);
		
		try {
			ImageVectorizer vectorizer = new ImageVectorizer(featureType, codebookFiles, numCentroids,
					projectionLength, pcaFile, whitening, numVectorizationThreads);
			vectorizer.setMaxImageSizeInPixels(maxImageSizeInPixels);
			return vectorizer;
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void extractVisualIndexesForConcepts(boolean useCommonVectorizer) {
		try {
			String[] paths = new String[Constants.concepts.length];
			int i=0;
			for (String concept : Constants.concepts) {
				paths[i++] = Constants.baseImageDir+"/"+concept;
			}
			
			String[] args1 = {"",
					"",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_0.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_1.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_2.csv,D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/surf_l2_128c_3.csv",
					"128,128,128,128",
					"D:/Projects/MULTISENSOR/Development/EclipseWorkspace/Multimedia-Indexing/models/pca_surf_4x128_32768to1024.txt",
					"100",
					"1048576",
					"10",
					"surf",
					"false"};
			
			ImageVectorizer vectorizer = null;
			if (useCommonVectorizer)
				vectorizer = createVectorizer(args1);
			
			for (String path : paths) {
				args1[0] = path+"/";
				args1[1] = path+Constants.indexPathAppend+"/";
				
				File dirFile = new File(args1[1]);
				if (!dirFile.exists())
					dirFile.mkdirs();
				
				if (!useCommonVectorizer)
					FolderIndexingMT.main(args1);
				else
					FolderIndexingMT_CommonVectorizer.main2(args1, vectorizer);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		Compare c = new Compare();
		
		//c.getImagesForConcepts();
		//c.extractVisualIndexesForConcepts(false);
		
		Map<String, double[][]> sourceConcepts = new HashMap<String, double[][]>();
		Map<String, double[][]> targetConcepts = new HashMap<String, double[][]>();
		
		Reader reader = new Reader();
		//bicycle at index 6
		String csvFile1 =  Constants.baseImageDir+"/"+Constants.concepts[0]+Constants.indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
		sourceConcepts.put(Constants.concepts[0], reader.CSV2Array(csvFile1, 100, 1));
		
		//others at indices 0-5
		for (int i=1;i<8;i++) {
			String csvFile2 =  Constants.baseImageDir+"/"+Constants.concepts[i]+Constants.indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
			targetConcepts.put(Constants.concepts[i], reader.CSV2Array(csvFile2, 100, 1));
		}
		
		//compare!
		double[][] sourceIndex = sourceConcepts.get(Constants.concepts[0]);
		double distance = 0;
		String match = "";
		for (String key : targetConcepts.keySet()) {
			//double temp = Comparator.cosineDistance(sourceIndex, targetConcepts.get(key))/(sourceIndex.length*targetConcepts.get(key).length); 
			double temp = Comparator.cosineSimilarity(sourceIndex, targetConcepts.get(key))/(sourceIndex.length*targetConcepts.get(key).length);
			if (temp > distance) {
				distance = temp;
				match = key;
			}
			System.out.println("Distance of '"+Constants.concepts[0]+"' to '"+key+"' is: "+temp);
		}
		System.out.println("Concept '"+Constants.concepts[0]+"' matches '"+match+"' with distance "+distance);
	}
}
