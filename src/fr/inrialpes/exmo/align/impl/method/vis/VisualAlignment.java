package fr.inrialpes.exmo.align.impl.method.vis;

import java.util.List;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.TaggedWord;
import fr.inrialpes.exmo.align.impl.method.vis.util.UtilClass;

public class VisualAlignment extends LexiVisAlignment {
	final static Logger logger = LoggerFactory.getLogger( VisualAlignment.class );
	
	protected class VisualMatrixMeasure extends LexiVisMatrixMeasure {
		
		@Override
		public double measure(Object o1, Object o2) throws Exception {
			double similarity2 = 0;
			try {
				String s1 = ontology1().getEntityName(o1);
				String s2 = ontology2().getEntityName(o2);
				
				String ts1 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s1))));
				String ts2 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s2))));
				
				List<TaggedWord> taggedS1 = POSTagging(ts1, true);
				List<TaggedWord> taggedS2 = POSTagging(ts2, true);
				
				int count = 0;
				for (TaggedWord tw1 : taggedS1) {
					for (TaggedWord tw2 : taggedS2) {
						double sim2 = VisualMeasure( tw1.word(), tw2.word(),  PennToWordNetPOS(tw1.tag()));
						
						if (sim2 > similarity2) {
							similarity2 = sim2;
						}
						//if (sim2 != 0) {
						//	similarity2 += sim2;
						//	count++;
						//}
					}
				}
				//if (count > 0) {
				//	similarity2 /= (double)count;
				//}
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			System.out.println(o1+" - "+o2+" = "+similarity2);
			
			return similarity2;
		}
	}
	
	@Override
	public void align( Alignment alignment, Properties params ) throws AlignmentException {
		setSimilarity( new VisualMatrixMeasure() );
		setType("??");
		
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

}
