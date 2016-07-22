package gr.iti.multisensor.ui.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Constants {
	public static Properties prop = new Properties();
	
	public static Map<String, AlignmentParams> stringMatchers = new HashMap<String, AlignmentParams>();
	public static Map<String, AlignmentParams> structMatchers = new HashMap<String, AlignmentParams>();
	public static Map<String, AlignmentParams> semanticMatchers = new HashMap<String, AlignmentParams>();
	
	//public static String WNDIR = "C:/Projects/MULTISENSOR/Related software/Wordnet-3.0/dict";
	public static String PARAMS_KEY = "params";
	public static String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";

	static {
		try {
			InputStream input = new FileInputStream("config/config.txt");
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		stringMatchers.put("ISub", new AlignmentParams("fr.inrialpes.exmo.align.impl.alg.StringSimAlignment"));
		stringMatchers.put("Equal distance", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.StringDistAlignment"));
		stringMatchers.put("Edit distance", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment"));
		stringMatchers.put("NameEq alignment", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.NameEqAlignment"));
		stringMatchers.put("Substring distance", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.SubsDistNameAlignment"));
		
		structMatchers.put("StructurePlus", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.StrucSubsDistAlignment"));
		structMatchers.put("Similarity flooding", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.SimilarityFloodingAlignment"));
		//structMatchers.put("Name and property","fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment");
		
		semanticMatchers.put("Euzenat similarity (Wordnet)", new AlignmentParams("fr.inrialpes.exmo.align.ling.JWNLAlignment", "wndict", prop.getProperty("wndir")));
		semanticMatchers.put("Visual similarity", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.vis.VisualAlignment"));
		semanticMatchers.put("Wu-Palmer similarity (Wordnet)", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.vis.LexicalAlignment"));
		semanticMatchers.put("LexiVis similarity", new AlignmentParams("fr.inrialpes.exmo.align.impl.method.vis.LexiVisAlignment"));
	}
	
	public static class AlignmentParams {
		String class_;
		Map<String, String> props = null;
		
		public AlignmentParams(String class_, String... props) {
			this.class_ = class_;
			
			if (props != null) {
				if (props.length > 0) {
					if (props.length%2 != 0)
						return;
					this.props = new HashMap<String, String>();
					for (int i=0;i<props.length;i+=2) {
						this.props.put(props[i], props[i+1]);
					}
				}
			}
		}
		
		public String getClass_() {
			return this.class_;
		}
		
		public Map<String, String> getProps() {
			return this.props;
		}
		
		public String toString() {
			String ret = "";
			
			ret += "class: "+class_+"\n";
			if (props != null)
				for (String key : props.keySet()) {
					ret += "\t"+key+" = "+props.get(key)+"\n"; 
				}
			
			return ret;
		}
	}
}
