package fr.inrialpes.exmo.align.impl.alg;

import java.lang.reflect.Method;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.ontosim.string.StringDistances;

public class StringSimAlignment extends DistanceAlignment implements AlignmentProcess {

final static Logger logger = LoggerFactory.getLogger( StringSimAlignment.class );
    
    Method similarityMethod = null;
    String methodName = "ISub";

    protected class StringSimMatrixMeasure extends MatrixMeasure {
	public StringSimMatrixMeasure() {
	    similarity = true; // This is a similarity matrix
	}
	public double measure( Object o1, Object o2 ) throws Exception {
	    String s1 = null;
	    String s2 = null;
	    try {
		s1 = ontology1().getEntityName( o1 );
		s2 = ontology2().getEntityName( o2 );
	    } catch ( Exception owex ) {
		logger.debug( "IGNORED (returned 1. instead)", owex );
	    };
	    // Unnamed entity = min similarity
	    if ( s1 == null || s2 == null ) return 0.;
	    Object[] params = { s1.toLowerCase(), s2.toLowerCase() };
	    //logger.trace( "OB:{} ++ {} ==> {}", s1, s2, dissimilarity.invoke( null, params ) );
	    return ((Double)similarityMethod.invoke( null, params )).doubleValue();
	}
	public double classMeasure( Object cl1, Object cl2 ) throws Exception {
	    return measure( cl1, cl2 );
	}
	public double propertyMeasure( Object pr1, Object pr2 ) throws Exception{
	    return measure( pr1, pr2 );
	}
	public double individualMeasure( Object id1, Object id2 ) throws Exception{
	    return measure( id1, id2 );
	}
	
	public double[][] getClassMatrix() {
		return clmatrix;
	}
    }

    /**
     * Creation
     * (4.3) For compatibility reason with previous versions, the type is set to
     * "?*" so that the behaviour is the same.
     * In future version (5.0), this should be reverted to "**",
     * so the extractors will behave differently
     **/
    public StringSimAlignment() {
	setSimilarity( new StringSimMatrixMeasure() );
	setType("?*");
	//setType("11");
    }

    /* Processing */
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
	// Get function from params
	String f = params.getProperty("stringFunction");
	try {
	    if ( f != null ) methodName = f.trim();
	    Class[] mParams = { String.class, String.class };
	    similarityMethod = StringSimilarity.class.getMethod( methodName, mParams );
	} catch ( NoSuchMethodException e ) {
	    throw new AlignmentException( "Unknown method for StringSimAlignment : "+params.getProperty("stringFunction"), e );
	}

	// JE2010: Strange: why is it not equivalent to call
	// super.align( alignment, params )
	// Load initial alignment
	loadInit( alignment );

	// Initialize matrix
	getSimilarity().initialize( ontology1(), ontology2(), alignment );

	// Compute similarity/dissimilarity
	getSimilarity().compute( params );

	// Print matrix if asked
	params.setProperty( "algName", getClass()+"/"+methodName );
	if ( params.getProperty("printMatrix") != null ) printDistanceMatrix( params );

	// Extract alignment
	extract( type, params );
    }

}
