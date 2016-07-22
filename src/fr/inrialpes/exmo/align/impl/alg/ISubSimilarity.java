package fr.inrialpes.exmo.align.impl.alg;

public class ISubSimilarity {
	private static boolean normaliseStrings = true;
	
	//*******************************
   // Compute String Similarity
   //*******************************
   /**
     * Computes the similarity of two strings by using I-Sub matching algorithm by Stoilos
     * @return double, similarity
     */
	public static double run(String stringOne, String stringTwo) {
		if( (stringOne == null) || (stringTwo == null) )
			return -1;
	
		String inputStr1 = stringOne.toString();
		String inputStr2 = stringTwo.toString();
	
		if( normaliseStrings ){
			inputStr1 = inputStr1.toLowerCase();
			inputStr2 = inputStr2.toLowerCase();
			
			inputStr1 = normalizeString( inputStr1 , '.' );
			inputStr2 = normalizeString( inputStr2 , '.' );
			inputStr1 = normalizeString( inputStr1 , '_' );
			inputStr2 = normalizeString( inputStr2 , '_' );
			inputStr1 = normalizeString( inputStr1 , ' ' );
			inputStr2 = normalizeString( inputStr2 , ' ' );
		}
		
		int l1 = inputStr1.length(); // length of s
		int l2 = inputStr2.length(); // length of t
	
		int L1 = l1;
		int L2 = l2;
	
		if ((L1 == 0) && (L2 == 0))
			return 1;
		if ((L1 == 0) || (L2 == 0))
			return -1;
	
		double common = 0;
		int best = 2;
	
		while( inputStr1.length() > 0 && inputStr2.length() > 0 && best != 0 ) {
			best = 0; // the best subs length so far
	
			l1 = inputStr1.length(); // length of s
			l2 = inputStr2.length(); // length of t
	
			int i = 0; // iterates through s1
			int j = 0; // iterates through s2
	
			int startS2 = 0;
			int endS2 = 0;
			int startS1 = 0;
			int endS1 = 0;
			int p=0;
	
			for( i = 0; (i < l1) && (l1 - i > best); i++) {
				j = 0;
				while (l2 - j > best) {
					int k = i;
					for(;(j < l2) && (inputStr1.charAt(k) != inputStr2.charAt(j)); j++);
			
					if (j != l2) { // we have found a starting point
						p = j;
						for (j++, k++;
							(j < l2) && (k < l1) && (inputStr1.charAt(k) == inputStr2.charAt(j));
							j++, k++);
						if( k-i > best){
							best = k-i;
							startS1 = i;
							endS1 = k;
							startS2 = p;
							endS2 = j;	
						}
					}
				}
			}
			char[] newString = new char[ inputStr1.length() - (endS1 - startS1) ];
		
			j=0;
			for( i=0 ;i<inputStr1.length() ; i++ ) {
				if( i>=startS1 && i< endS1 )
					continue;
				newString[j++] = inputStr1.charAt( i );			
			}
			
			inputStr1 = new String( newString );
	
			newString = new char[ inputStr2.length() - ( endS2 - startS2 ) ];
			j=0;
			for( i=0 ;i<inputStr2.length() ; i++ ) {
				if( i>=startS2 && i< endS2 )
					continue;
				newString[j++] = inputStr2.charAt( i );
			}
			
			inputStr2 = new String( newString );
	
			if( best > 2 )
				common += best;
			else
				best = 0;
		}
	
		double commonality = 0;
		double scaledCommon = (double)(2*common)/(L1+L2);
		commonality = scaledCommon;
	
		double winklerImprovement = winklerImprovement(inputStr1, inputStr2, commonality);
		double dissimilarity = 0;
	
		double rest1 = L1 - common;
		double rest2 = L2 - common;
	
		double unmatchedS1 = Math.max( rest1 , 0 );
		double unmatchedS2 = Math.max( rest2 , 0 );
		unmatchedS1 = rest1/L1;
		unmatchedS2 = rest2/L2;
		
		/** Hamacher Product */
		double suma = unmatchedS1 + unmatchedS2;
		double product = unmatchedS1 * unmatchedS2;
		double p = 0.6;   //For 1 it coincides with the algebraic product
		if( (suma-product) == 0 )
			dissimilarity = 0;
		else
			dissimilarity = (product)/(p+(1-p)*(suma-product));
		
		return commonality - dissimilarity + winklerImprovement;

	}
	
	private static double winklerImprovement(String s1, String s2, double commonality) {

		int i;
		int n = Math.min( s1.length() , s2.length() );
		for( i=0 ; i<n ; i++ )
			if( s1.charAt( i ) != s2.charAt( i ) )
				break;

		return Math.min(4, i)*0.1*(1-commonality);
	}

	public static String normalizeString(String str, char remo) {
		
		StringBuffer strBuf = new StringBuffer(); 
		for( int i=0 ; i<str.length() ; i++ ){
			if( str.charAt( i ) != remo )
				strBuf.append( str.charAt( i ) );
		}
		return strBuf.toString();		
	}
	
}
