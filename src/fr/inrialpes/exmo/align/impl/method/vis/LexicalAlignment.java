package fr.inrialpes.exmo.align.impl.method.vis;

import java.util.List;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.TaggedWord;
import fr.inrialpes.exmo.align.impl.method.vis.util.UtilClass;

public class LexicalAlignment extends LexiVisAlignment {
	final static Logger logger = LoggerFactory.getLogger( LexicalAlignment.class );
	
	protected class LexicalMatrixMeasure extends LexiVisMatrixMeasure {
		
		@Override
		public double measure(Object o1, Object o2) throws Exception {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			
			String ts1 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s1))));
			String ts2 = tokensToSentence(UtilClass.removeStopWords(UtilClass.removeAlphaNumeric(tokenize(s2))));
			
			List<TaggedWord> taggedS1 = POSTagging(ts1, true);
			List<TaggedWord> taggedS2 = POSTagging(ts2, true);
			
			double similarity1 = 0;
			int count = 0;
			for (TaggedWord tw1 : taggedS1) {
				for (TaggedWord tw2 : taggedS2) {
					double sim1 = WuPalmerWordnetSimilarity(tw1.word(), tw2.word(), PennToWordNetPOS(tw1.tag()));
					
					if (sim1 != 0) {
						similarity1 += sim1;
						count++;
					}
				}
			}
			if (count > 0) {
				similarity1 /= (double)count;
			}
			
			return similarity1;
		}
		
	}
	
	@Override
	public void align( Alignment alignment, Properties params ) throws AlignmentException {
		setSimilarity( new LexicalMatrixMeasure() );
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
