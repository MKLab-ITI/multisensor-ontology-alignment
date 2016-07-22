package fr.inrialpes.exmo.align.impl.method.vis;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.yahooimg.ImgService;
import com.example.yahooimg.StHttpRequest;
import com.example.yahooimg.similarity.Comparator;
import com.example.yahooimg.similarity.Reader;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import gr.iti.mklab.visual.examples.FolderIndexingMT;
import gr.iti.multisensor.ui.utils.Parameters;

public class VisAlignment extends DistanceAlignment  implements AlignmentProcess {

	private String baseImageFolder = Parameters.VISUAL_ALIGNMENT_IMAGE_FOLDER;
	private String indexPathAppend = "-index";
	final static Logger logger = LoggerFactory.getLogger( VisAlignment.class );
	
	protected class VisMatrixMeasure extends MatrixMeasure {
		public VisMatrixMeasure() {
		    similarity = false; // This is a distance matrix
		}
		
		public double classMeasure( Object cl1, Object cl2 ) throws Exception {
		    return measure( cl1, cl2 );
		}
		public double propertyMeasure( Object pr1, Object pr2 ) throws Exception{
		    //return measure( pr1, pr2 );
			return 1.0;
		}
		public double individualMeasure( Object id1, Object id2 ) throws Exception{
		    //return measure( id1, id2 );
			return 1.0;
		}
		
		public double measure(Object o1, Object o2) throws AlignmentException {
			double ret = 0;
			try {
				String s1 = ontology1().getEntityName(o1);
				String s2 = ontology2().getEntityName(o2);
				
				if (s1 == null || s2 == null) return 0.;
				
				//String qs1 = tokensToWord(tokenize(s1));
				//String qs2 = tokensToWord(tokenize(s2));
				
				//createIndex(s1, qs1);
				//createIndex(s2, qs2);
				createIndex(s1);
				createIndex(s2);
				
				Reader reader = new Reader();
				
				String csvFile1 =  baseImageFolder+"/"+s1+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
				double[][] index1 = reader.CSV2Array(csvFile1, 100, 1);
				
				String csvFile2 =  baseImageFolder+"/"+s2+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
				double[][] index2 = reader.CSV2Array(csvFile2, 100, 1);
				
				ret = 1.0 - Comparator.cosineSimilarity(index1, index2);
				
			} catch ( OntowrapException owex ) {
				throw new AlignmentException( "Error getting entity name", owex );
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return ret;
	    }
	}
	
	@Override
	public void align( Alignment alignment, Properties params ) throws AlignmentException {
		setSimilarity( new VisMatrixMeasure() );
		setType("??");
		
		// JE2010: Strange: why is it not equivalent to call
		// super.align( alignment, params )
		// Load initial alignment
		loadInit( alignment );

		// Initialize matrix
		getSimilarity().initialize( ontology1(), ontology2(), alignment );

		// Compute similarity/dissimilarity
		getSimilarity().compute( params );

		// Print matrix if asked
		params.setProperty( "algName", getClass().toString() );
		if ( params.getProperty("printMatrix") != null ) printDistanceMatrix( params );

		// Extract alignment
		extract( type, params );
	}

	public void createIndex(String origConcept) {
		if (!checkDirExists(baseImageFolder+"/"+origConcept)) {
			createDir(baseImageFolder+"/"+origConcept);
			//getImagesForConcepts(tokenizedConcept);
			System.out.println("Query = "+origConcept);
			getImagesForConcepts(origConcept);
			extractVisualIndexesForConcepts(origConcept);
		}
		else if (!checkDirForImages(baseImageFolder+"/"+origConcept)) {
			//getImagesForConcepts(tokenizedConcept);
			System.out.println("Query = "+origConcept);
			getImagesForConcepts(origConcept);
			extractVisualIndexesForConcepts(origConcept);
		}
		else if (!checkDirExists(baseImageFolder+"/"+origConcept+indexPathAppend)) {
			extractVisualIndexesForConcepts(origConcept);
		}
	}
	
	public boolean checkDirExists(String dir) {
		File dirFile = new File(dir);
		if (!dirFile.exists())
			return false;
		return true;
	}
	
	public boolean checkDirForImages(String dir) {
		File folder = new File(dir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".jpg")
						|| name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif"))
					return true;
				else
					return false;
			}
		};
		String[] files = folder.list(filter);
		if (files.length == 0)
			return false;
		return true;
	}
	
	public void createDir(String dir) {
		File dirFile = new File(dir);
		dirFile.mkdirs();
	}
	
	public void extractVisualIndexesForConcepts(String concept) {
		try {
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
			
			
				args1[0] = baseImageFolder+"/"+concept+"/";
				args1[1] = baseImageFolder+"/"+concept+indexPathAppend+"/";
				
				File dirFile = new File(args1[1]);
				if (!dirFile.exists())
					dirFile.mkdirs();
				
				FolderIndexingMT.main(args1);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getImagesForConcepts(String concept) {
		ImgService s = new ImgService();
		StHttpRequest httpsRequest = s.authorize();
		
		try {
			String[] c = {concept};
			s.getImagesForQueries(httpsRequest, c, baseImageFolder);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*public static void main(String[] args) {
		VisAlignment v = new VisAlignment();
		List<String> s = v.tokenize("Caneuro");
		for (int i=0;i<s.size();i++)
			System.out.print(s.get(i)+" ");
	}*/
}
