package gr.iti.multisensor.matrix;

import fr.inrialpes.exmo.ontosim.util.HungarianAlgorithm;

public class HungarianMethod {
	public static int[][] callHungarianMethod( double[][] matrix, int i, int j ) {
		boolean transposed = false;
		if ( i > j ) { // transposed array (because rows>columns).
		    matrix = HungarianAlgorithm.transpose(matrix);
		    transposed = true;
		}
		int[][] result = HungarianAlgorithm.hgAlgorithm( matrix, "max" );
		if ( transposed ) {
		    for( int k=0; k < result.length ; k++ ) { 
		    	int val = result[k][0]; 
		    	result[k][0] = result[k][1]; 
		    	result[k][1] = val; 
		    }
		    
		}
		return result;
    }
}
