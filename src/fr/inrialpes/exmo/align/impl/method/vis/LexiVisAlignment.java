package fr.inrialpes.exmo.align.impl.method.vis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.yahooimg.ImgService;
import com.example.yahooimg.StHttpRequest;
import com.example.yahooimg.similarity.Comparator;
import com.example.yahooimg.similarity.Reader;
//import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.align.impl.method.vis.util.ImageDownloaderThread;
import fr.inrialpes.exmo.align.impl.method.vis.util.Lock;
import fr.inrialpes.exmo.align.impl.method.vis.util.Node;
import fr.inrialpes.exmo.align.impl.method.vis.util.Tree;
import fr.inrialpes.exmo.align.impl.method.vis.util.UtilClass;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import gr.iti.mklab.visual.examples.FolderIndexingMT;

public class LexiVisAlignment extends DistanceAlignment  implements AlignmentProcess {
	//private String baseImageFolder = "D:/Projects/MULTISENSOR/Development/EclipseWorkspace/YahooBossImgService/images";
	private String baseImageFolder = "D:/Projects/MULTISENSOR/Development/EclipseWorkspace/align-4.6/images";
	private String indexPathAppend = "-index";
	final static Logger logger = LoggerFactory.getLogger( LexiVisAlignment.class );
	final static String JWNLconfig = "config/JWNL_properties.xml";
	final static String POSTaggerModelFile = "models/english-left3words-distsim.tagger";
	MaxentTagger tagger = null;
	final static int SYNSET_STRING_LENGTH = 8;
	final static String IMAGENET_URL = "http://www.image-net.org/api/text/imagenet.synset.geturls?wnid=";
	final static String IMAGENET_TRAIL = "n";
	final static int minimumImages = 25;
	POS pos = POS.NOUN;
	
	public LexiVisAlignment() {
		super();
		try	{
            if (!JWNL.isInitialized())
            	JWNL.initialize(new FileInputStream(JWNLconfig));
        } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Cannot initialise WordNet");
        }
		
		tagger = new MaxentTagger(POSTaggerModelFile);
	}
	
	protected class LexiVisMatrixMeasure extends MatrixMeasure {
		public LexiVisMatrixMeasure() {
		    similarity = true; // This is a similarity matrix
		}
		
		public double classMeasure( Object cl1, Object cl2 ) throws Exception {
			return measure(cl1,cl2);
		}
		
		public double propertyMeasure( Object pr1, Object pr2 ) throws Exception {
			return measure(pr1,pr2);
		}
		
		public double individualMeasure( Object id1, Object id2 ) throws Exception {
			return 0.0;
		}
		
		public double measure(Object o1, Object o2) throws Exception {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			
			String ts1 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s1))));
			String ts2 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s2))));
			
			List<TaggedWord> taggedS1 = POSTagging(ts1, true);
			List<TaggedWord> taggedS2 = POSTagging(ts2, true);
			
			double similarity1 = 0;
			double similarity2 = 0;
			int count = 0;
			for (TaggedWord tw1 : taggedS1) {
				for (TaggedWord tw2 : taggedS2) {
					
					//double sim1 = WuPalmerWordnetVisualSimilarity(tw1.word(), tw2.word(), PennToWordNetPOS(tw1.tag()));
					//double sim1 = WuPalmerWordnetSimilarity(tw1.word(), tw2.word(), PennToWordNetPOS(tw1.tag()));
					double sim1 = WuPalmerWordnetVisualSimilarity(tw1.word(), tw2.word(), PennToWordNetPOS(tw1.tag()));
					
					//double sim2 = VisualMeasure( tw1.word(), tw2.word(),  PennToWordNetPOS(tw1.tag()));
					double sim2 = sim1;
					
					if (sim1 > 0 && sim2 > 0) {
						similarity1 += sim1;
						similarity2 += sim2;
						count++;
					}
				}
			}
			if (count > 0) {
				similarity1 /= (double)count;
				similarity2 /= (double)count;
			}
			//double similarity1 = WuPalmerWordnetSimilarity(ts1, ts2);
			//double similarity2 = VisualMeasure( ts1, ts2 );
			
			double aL = 0.5; double aV = 1.0-aL;
			
			return aL*similarity1 + aV*similarity2;
		}
	}
	
	public List<TaggedWord> POSTagging(String sentence, boolean whole) {
		if (tagger == null)
			return null;
		
		List<TaggedWord> taggedSentence = new ArrayList<TaggedWord>();
		
		if (!whole) {
			String[] sentenceParts = sentence.split(" ");
		
			List<HasWord> sentenceList = Sentence.toWordList(sentenceParts);
			taggedSentence = tagger.tagSentence(sentenceList);
		}
	    
		if (taggedSentence.size() > 1 || whole) {
			List<HasWord> sentenceListWhole = Sentence.toWordList(sentence);
			List<TaggedWord> taggedSentenceWhole = tagger.tagSentence(sentenceListWhole);
			taggedSentence.addAll(taggedSentenceWhole);
		}
		
	    return taggedSentence;
	}
	
	public POS PennToWordNetPOS(String tag) {
		if (tag.startsWith("N")) return POS.NOUN;
		if (tag.startsWith("J")) return POS.ADJECTIVE;
		if (tag.startsWith("V")) return POS.VERB;
		if (tag.startsWith("RB")) return POS.ADVERB;
		
		return POS.NOUN;
	}
	
	@Override
	public void align( Alignment alignment, Properties params ) throws AlignmentException {
		setSimilarity( new LexiVisMatrixMeasure() );
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
	
	public void makeHierarchyTree(Synset s, PointerType direction, Tree<Synset> tree) throws Exception {
		try {
			Pointer[] pointerArr = s.getPointers(direction); 
			if (pointerArr != null) {
				if (pointerArr.length > 0) {
					for (int i=0;i<pointerArr.length;i++) {
						Pointer x = pointerArr[i];
						Synset target = x.getTargetSynset();
						if (target != null && tree != null && s != null && tree.getDepth() < 100) {
							tree.addNode(target, s);
							//recursive call
							makeHierarchyTree(target, direction, tree);
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in synset "+s.getWords());
		}
	}
	
	public double WuPalmerWordnetSimilarity(String s1, String s2, POS pos) {
		try {
			IndexWord iw1 = Dictionary.getInstance().getIndexWord(pos, s1);
			IndexWord iw2 = Dictionary.getInstance().getIndexWord(pos, s2);
			if (iw1 != null && iw2 != null) {
				if (iw1.getLemma().equals(iw2.getLemma()))
					return 1;
				
				Synset[] set1 = iw1.getSenses();
				List<Tree<Synset>> set1Trees = new ArrayList<Tree<Synset>>(set1.length);
				Synset[] set2 = iw2.getSenses();
				List<Tree<Synset>> set2Trees = new ArrayList<Tree<Synset>>(set2.length);
				
				for (int i=0;i<set1.length;i++) {
					try {
						Tree<Synset> tree = new Tree<Synset>();
						tree.addNode(set1[i]);
						makeHierarchyTree(set1[i], PointerType.HYPERNYM, tree);
						set1Trees.add(tree);
					}catch (Exception e) {
						System.out.println("Error in set1: "+s1+" - "+s2);
						e.printStackTrace();
					}
				}
				for (int i=0;i<set2.length;i++) {
					try {
						Tree<Synset> tree = new Tree<Synset>();
						tree.addNode(set2[i]);
						makeHierarchyTree(set2[i], PointerType.HYPERNYM, tree);
						set2Trees.add(tree);
					}catch (Exception e) {
						System.out.println("Error in set1: "+s1+" - "+s2);
						e.printStackTrace();
					}
				}
				
				double similarity = 0;
				for (int i=0;i<set1Trees.size();i++) {
					Iterator<Node> tree1Iterator = set1Trees.get(i).iterator(set1[i]);
					while (tree1Iterator.hasNext()) {
						Node node1 = tree1Iterator.next();
						Synset synset1 = (Synset)node1.getIdentifier();
						for (int j=0;j<set2Trees.size();j++) {
							Iterator<Node> tree2Iterator = set2Trees.get(j).iterator(set2[j]);
							while (tree2Iterator.hasNext()) {
								Node node2 = tree2Iterator.next();
								Synset synset2 = (Synset)node2.getIdentifier();
								if (synset2.getOffset() == synset1.getOffset()) {
									double N1 = (double)node1.getLevel();
									double N2 = (double)node2.getLevel();
									double N3 = (double)set2Trees.get(j).getDepth() - N2;
									if (2.0*N3/(N1+N2+2.0*N3) > similarity) {
										similarity = 2.0*N3/(N1+N2+2.0*N3);
									}
									break;
								}
							}
						}
					}
				}
				return similarity;
			}
			else {
				return 0;
			}
		}catch (JWNLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public double WuPalmerWordnetVisualSimilarity(String s1, String s2, POS pos) {
		try {
			IndexWord iw1 = Dictionary.getInstance().getIndexWord(pos, s1);
			IndexWord iw2 = Dictionary.getInstance().getIndexWord(pos, s2);
			if (iw1 != null && iw2 != null) {
				if (iw1.getLemma().equals(iw2.getLemma()))
					return 1;
				Synset[] set1 = iw1.getSenses();
				List<Tree<Synset>> set1Trees = new ArrayList<Tree<Synset>>(set1.length);
				Synset[] set2 = iw2.getSenses();
				List<Tree<Synset>> set2Trees = new ArrayList<Tree<Synset>>(set2.length);
				
				for (int i=0;i<set1.length;i++) {
					try {
						Tree<Synset> tree = new Tree<Synset>();
						tree.addNode(set1[i]);
						makeHierarchyTree(set1[i], PointerType.HYPERNYM, tree);
						set1Trees.add(tree);
					}catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error in set1: "+s1+" - "+s2);
					}
				}
				for (int i=0;i<set2.length;i++) {
					try {
						Tree<Synset> tree = new Tree<Synset>();
						tree.addNode(set2[i]);
						makeHierarchyTree(set2[i], PointerType.HYPERNYM, tree);
						set2Trees.add(tree);
					}catch (Exception e) {
						e.printStackTrace();
						System.out.println("Error in set2: "+s1+" - "+s2);
					}
				}
				
				double similarity = 0;
				for (int i=0;i<set1Trees.size();i++) {
					Iterator<Node> tree1Iterator = set1Trees.get(i).iterator(set1[i]);
					while (tree1Iterator.hasNext()) {
						Node node1 = tree1Iterator.next();
						Synset synset1 = (Synset)node1.getIdentifier();
						for (int j=0;j<set2Trees.size();j++) {
							Iterator<Node> tree2Iterator = set2Trees.get(j).iterator(set2[j]);
							while (tree2Iterator.hasNext()) {
								Node node2 = tree2Iterator.next();
								Synset synset2 = (Synset)node2.getIdentifier();
								if (synset2.getOffset() == synset1.getOffset()) {
									try {
										Synset S1 = set1Trees.get(i).getNodes().get(set1[i]).getIdentifier();
										Synset S2 = set2Trees.get(j).getNodes().get(set2[j]).getIdentifier();
										
										double N1 = VisualMeasure(S1, synset2);
										double N2 = VisualMeasure(S2, synset2);
										double N3 = VisualMeasure(S1,S2);
										
										double temp = N3/(3.0-(N1+N2));
										
										if (temp > similarity)
											similarity = temp;
										//if (2.0*N3/(N1+N2+2.0*N3) > similarity) {
										//	similarity = 2.0*N3/(N1+N2+2.0*N3);
										//}
										break;
									}catch (AlignmentException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
				return similarity;
			}
			else {
				return 0;
			}
		}catch (JWNLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public double VisualMeasure(String s1, String s2, POS pos) throws AlignmentException {
		double ret = 0;
		try {
			//String s1 = ontology1().getEntityName(o1);
			//String s2 = ontology2().getEntityName(o2);
			
			//s1 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s1))));
			//s2 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s2))));
			
			if (s1 == null || s2 == null) return 0;
			
			//String qs1 = tokensToWord(tokenize(s1));
			//String qs2 = tokensToWord(tokenize(s2));
			
			//createIndex(s1, qs1);
			//createIndex(s2, qs2);
			String[] senses1 = createIndex(s1, pos);
			String[] senses2 = createIndex(s2, pos);
			
			Reader reader = new Reader();
			for (int i=0;i<senses1.length;i++) {
				for (int j=0;j<senses2.length;j++) {
					String csvFile1 =  baseImageFolder+"/"+IMAGENET_TRAIL+senses1[i]+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
					double[][] index1 = reader.CSV2Array(csvFile1, 100, 1);
					
					String csvFile2 =  baseImageFolder+"/"+IMAGENET_TRAIL+senses2[j]+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
					double[][] index2 = reader.CSV2Array(csvFile2, 100, 1);
					
					//double sim = Comparator.cosineSimilarity(index1, index2);
					double sim = Comparator.jaccardSetSimilarity(index1, index2);
					
					if (sim > ret)
						ret = sim;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
    }
	
	public double VisualMeasure(Synset s1, Synset s2) throws AlignmentException {
		double ret = 0;
		try {
			if (s1 == null || s2 == null) 
				return 0;
			
			String[] senses1 = createIndex(s1);
			String[] senses2 = createIndex(s2);
			
			Reader reader = new Reader();
			for (int i=0;i<senses1.length;i++) {
				for (int j=0;j<senses2.length;j++) {
					String csvFile1 =  baseImageFolder+"/"+IMAGENET_TRAIL+senses1[i]+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
					double[][] index1 = reader.CSV2Array(csvFile1, 100, 1);
					
					String csvFile2 =  baseImageFolder+"/"+IMAGENET_TRAIL+senses2[j]+indexPathAppend+"/BDB_1048576_surf_32768to100_index.csv";
					double[][] index2 = reader.CSV2Array(csvFile2, 100, 1);
					
					//double sim = Comparator.cosineSimilarity(index1, index2);
					double sim = Comparator.jaccardSetSimilarity(index1, index2);
					
					if (sim > ret)
						ret = sim;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret;
    }

	public Synset[] getSensesOfWord(String s1, POS pos) {
		//String c1 = tokensToSentence(tokenize(s1));
		try {
			IndexWord iw1 = Dictionary.getInstance().getIndexWord(pos, s1);
			if (iw1 != null) {
				return iw1.getSenses();
			}
		}catch (JWNLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String[] createIndex(Synset synset) {
		Synset[] synsets = {synset};
		
		return createIndex(synsets);
	}
	
	public String[] createIndex(String origConcept, POS pos) {
		Synset[] senses = getSensesOfWord(origConcept, pos);
		
		return createIndex(senses);
	}
	
	public String[] createIndex(Synset[] senses) {
		List<String> ret = new ArrayList<String>();
		if (senses != null) {
			for (int i=0;i<senses.length;i++) {
				String synsetID = padSynsetID(senses[i]);
				ret.add(synsetID);
				if (!checkDirExists(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID)) {
					createDir(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID);
					getImagesFromImagenet(senses[i]);
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
				else if (!checkDirForImages(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID)) {
					getImagesFromImagenet(senses[i]);
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
				else if (!checkDirExists(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID+indexPathAppend)) {
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
			}
		}
		return ret.toArray(new String[0]);
	}
	
/*	public String[] createIndex(String origConcept, POS pos) {
		Synset[] senses = getSensesOfWord(origConcept, pos);
		List<String> ret = new ArrayList<String>();
		if (senses != null) {
			for (int i=0;i<senses.length;i++) {
				String synsetID = padSynsetID(senses[i]);
				ret.add(synsetID);
				if (!checkDirExists(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID)) {
					createDir(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID);
					getImagesFromImagenet(senses[i]);
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
				else if (!checkDirForImages(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID)) {
					getImagesFromImagenet(senses[i]);
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
				else if (!checkDirExists(baseImageFolder+"/"+IMAGENET_TRAIL+synsetID+indexPathAppend)) {
					extractVisualIndexesForConcepts(IMAGENET_TRAIL+synsetID);
				}
			}
		}
		return ret.toArray(new String[0]);
	}
*/	
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
	
	public String padSynsetID(Synset s) {
		String synsetID = s.getOffset()+"";
		for (int i=0;i<(SYNSET_STRING_LENGTH-synsetID.length());i++)
			synsetID = "0"+synsetID;
		
		return synsetID;
	}
	
	public void getImagesFromImagenet(Synset s) {
		try {
			String synsetID = padSynsetID(s);
			
			String urlString = IMAGENET_URL+IMAGENET_TRAIL+synsetID;
			
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
	 
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
	 					
			List<String> imageURLs = new ArrayList<String>();
			for (int k=0;k<50;k++) {
				String string = br.readLine();
				if (string !=null) {
					if (string.trim().length() == 0)
						continue;
					//if (!string.startsWith("The synset is not ready yet.") && !string.startsWith("Invalid url")) {
						try {
							URL test = new URL(string);
							imageURLs.add(string);
						} catch (MalformedURLException e) {}
					//}
					//else
					//	break;
				}
				else
					break;
			}
			br.close();
			conn.disconnect();
			
			//if imagenet has no images for synset or too few images, get images from Yahoo image search
			if (imageURLs.size() < minimumImages) {
				ImgService service = new ImgService();
				StHttpRequest httpsRequest = service.authorize();
				Word[] words = s.getWords();
				List<String> temp = new ArrayList<String>();
				for (Word word : words)
					temp.add(word.getLemma());
				System.out.println("Concat words are: "+tokensToSentence(temp));
				imageURLs = service.getImagesListForQuery(httpsRequest, tokensToSentence(temp));
			}
			
			String dir = this.baseImageFolder+"/"+IMAGENET_TRAIL+synsetID;
			UtilClass u = new UtilClass();
			u.downloadImagesFromList(imageURLs, dir);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void getImagesFromYahoo(String concept, String paddedSynsetID) {
		ImgService s = new ImgService();
		StHttpRequest httpsRequest = s.authorize();
		
		try {
			List<String> list = s.getImagesListForQuery(httpsRequest, tokensToSentence(tokenize(concept)));
			String dir = "";
			if (!paddedSynsetID.equals("")) {
				dir = this.baseImageFolder+"/"+IMAGENET_TRAIL+paddedSynsetID;
			}
			else {
				dir = this.baseImageFolder+"/"+concept;
			}
			UtilClass u = new UtilClass();
			u.downloadImagesFromList(list, dir);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<String> tokenize(String concept) {
		List<String> list = new ArrayList<String>();
		int pos = 0;
		for (int i=0;i<concept.length();i++) {
			if (Character.isUpperCase(concept.charAt(i)) && pos!=i) {
				if (i>0) {
					if (!Character.isUpperCase(concept.charAt(i-1))) {
						list.add(concept.substring(pos, i));
						pos = i;
					}
				}
			}
			else if ((concept.charAt(i) == '_') || (concept.charAt(i) == '-') || (concept.charAt(i) == '.')) {
				list.add(concept.substring(pos, i));
				pos = i+1;
			}
		}
		
		list.add(concept.substring(pos, concept.length()));
		
		return list;
	}
	
	public String tokensToSentence(List<String> list) {
		String ret  = "";
		for (String s : list)
			ret += s+" ";
		return ret.trim();
	}
}
