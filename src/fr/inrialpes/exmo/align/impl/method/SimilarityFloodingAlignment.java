package fr.inrialpes.exmo.align.impl.method;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.rdf.model.Literal;
import org.w3c.rdf.model.ModelException;
import org.w3c.rdf.model.NodeFactory;
import org.w3c.rdf.model.RDFNode;
import org.w3c.rdf.model.Model;
import org.w3c.rdf.model.Resource;
import org.w3c.rdf.model.Statement;
import org.w3c.rdf.util.RDFFactory;
import org.w3c.rdf.util.RDFFactoryImpl;
import org.w3c.rdf.util.RDFUtil;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.interdataworking.mm.alg.MapPair;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import fr.inrialpes.exmo.align.impl.Similarity;
import fr.inrialpes.exmo.align.impl.alg.StringSimilarity;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.extensions.ExtendedOWLAPI3;
import fr.inrialpes.exmo.ontowrap.extensions.IExtendedOntology;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;

public class SimilarityFloodingAlignment  extends DistanceAlignment implements AlignmentProcess {

	final static Logger logger = LoggerFactory.getLogger( SimilarityFloodingAlignment.class );

    private HeavyLoadedOntology<Object> honto1 = null;
    private HeavyLoadedOntology<Object> honto2 = null;
    
    public boolean DEBUG = false;
	  public static final int DEBUG_MAX_ITERATIONS = 0;

	  public static double RESOURCE_LITERAL_MATCH_PENALTY = 0.1;
	  
	  // default formula to be used: {ADD_SIGMA0_BEFORE=t, ADD_SIGMA0_AFTER=t, ADD_SIGMAN_AFTER=t}
	  public boolean[] formula = FORMULA_TTT;

	  // default way of computing the propagation coefficients to be used
	  public int FLOW_GRAPH_TYPE = FG_AVG;

	  // various iteration formulas

	  // MAY BE BETTER! but much worse convergence!
	  //   sigma^{n+1} = normalize(f(sigma^0 + sigma^n));
	  public static final boolean[] FORMULA_TFF = {true, false, false};

	  // SLIGHTLY WORSE, BUT BETTER CONVERGENCE!
	  //   sigma^{n+1} = normalize(sigma^0 + sigma^n + f(sigma^0 + sigma^n));
	  public static final boolean[] FORMULA_TTT = {true, true, true};

	  // USE THIS ONE FOR TESTING/DEBUGGING - PURE VERSION
	  //   sigma^{n+1} = normalize(sigma^n + f(sigma^n));
	  public static final boolean[] FORMULA_FFT = {false, false, true};

	  //   sigma^{n+1} = normalize(sigma^0 + f(sigma^n));
	  public static final boolean[] FORMULA_FTF = {false, true, false};

	  //   sigma^{n+1} = normalize(sigma^0 + f(sigma^0 + sigma^n));
	  public static final boolean[] FORMULA_TTF = {true, true, false};

	  //   sigma^{n+1} = normalize(sigma^n + f(sigma^0 + sigma^n));
	  public static final boolean[] FORMULA_TFT = {true, false, true};

	  //   sigma^{n+1} = normalize(sigma^0 + sigma^n + f(sigma^n));
	  public static final boolean[] FORMULA_FTT = {false, true, true};



	  //  static final boolean ADD_SIGMAN_BEFORE = true; // ALWAYS TRUE, OTHERWISE DOES NOT MAKE SENSE
	  //static final double MIN_NODE_SIM2 = StringMatcher.MIN_NODE_SIM;
	  public static double MIN_NODE_SIM2 = 0.001;
	  
	  // ways of computing propagation coefficients

	  public static final int FG_PRODUCT = 1;
	  public static final int FG_AVG = 2;
	  public static final int FG_EQUAL = 3;
	  public static final int FG_TOTALP = 4;
	  public static final int FG_TOTALS = 5;
	  public static final int FG_AVG_TOTALS = 6;
	  public static final int FG_STOCHASTIC = 7; // weight of OUTGOING normalized to 1
	  public static final int FG_INCOMING = 8; // weight of INCOMING normalized to 1

	  // other variables and constants

	  public static final double UPDATE_GUESS_WEIGHT = 1.0; // between 0 and 1
	  public double RESIDUAL_VECTOR_LENGTH = 0.05; // make this number smaller to increase precision
	  public int MAX_ITERATION_NUM = 10000;
	  public int MIN_ITERATION_NUM = 7;
	  public int TIMEOUT = 30 * 1000; // 30 sec

	  public boolean TRY_ALL_ARCS = true; // consider all arcs, not only those that are equal
	  public boolean DIRECTED_GRAPH = true;
	  public boolean TEST = true;

	  static final int MIN_CHOICE_ITEMS = 50;
	  public double EQUAL_PRED_COEFF = 1.0;
	  public double OTHER_PRED_COEFF = 0.001;

	  Model m1, m2;
	  private PGNode[] result = null;

	  Map pgnodes = new HashMap();
	  List pgarcs = new ArrayList();

	  // cache for reusable pairs
	  MapPair[] cachePairs = { new MapPair(), new MapPair() };
	  static final int PASS_PAIR = 0;
	  static final int GET_PAIR = 1;

    
    /** Creation **/
    public SimilarityFloodingAlignment(){
    	setSimilarity(new FloodMatrixMeasure());
    	setType("?*");
    };
    
    public void assignFloodMatrixMeasure(PGNode pg, MatrixMeasure sim) {
    	try {
	    	RDFNode srcNode = pg.getLeftNode();
			URI srcNodeURI = null;
			if (srcNode instanceof Resource) {
				Resource resource = (Resource)srcNode;
				srcNodeURI = URI.create(resource.getURI());
			}
			
			URI tgtNodeURI = null;
			RDFNode tgtNode = pg.getRightNode();
			if (tgtNode instanceof Resource) {
				Resource resource = (Resource)tgtNode;
				tgtNodeURI = URI.create(resource.getURI());
			}
			
			pg.sim = 1.0 - 1.0/Math.exp(pg.sim/0.1);
			
			if (srcNodeURI!=null && tgtNodeURI!=null) {
				Object ho1 = honto1.getEntity(srcNodeURI);
				Object ho2 = honto2.getEntity(tgtNodeURI);
				if (sim.classlist1.get(ho1)!=null && sim.classlist2.get(ho2)!=null) {
					sim.clmatrix[sim.classlist1.get(ho1).intValue()][sim.classlist2.get(ho2).intValue()] = pg.sim;
				}
				else if (sim.proplist1.get(ho1)!=null && sim.proplist2.get(ho2)!=null) {
					sim.prmatrix[sim.proplist1.get(ho1).intValue()][sim.proplist2.get(ho2).intValue()] = pg.sim;
				}
				else if (sim.indlist1.get(ho1)!=null && sim.indlist2.get(ho2)!=null) {
					sim.indmatrix[sim.indlist1.get(ho1).intValue()][sim.indlist2.get(ho2).intValue()] = pg.sim;
				}
			}
    	}catch (Exception e) {
    		//e.printStackTrace();
    		//System.out.println(e.getMessage());
    	}
    }

    /**
     * Initialization
     * The class requires HeavyLoadedOntologies
     */
    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
    	super.init( o1, o2, ontologies );
    	if ( !( getOntologyObject1() instanceof HeavyLoadedOntology
    			&& getOntologyObject1() instanceof HeavyLoadedOntology ))
    		throw new AlignmentException( "SimilarityFloodingAlignment requires HeavyLoadedOntology ontology loader" );
    }
    
    protected class FloodMatrixMeasure extends MatrixMeasure {

    	public FloodMatrixMeasure() {
    	    similarity = true; // This is a similarity matrix
    	}
    	
    	public double measure( Object o1, Object o2 ) {
    	    return 1.0;
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
    }
    
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
		loadInit( alignment );
    
		honto1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
    	honto2 = (HeavyLoadedOntology<Object>)getOntologyObject2();
    	
    	RDFFactory rf = new RDFFactoryImpl();
		m1 = rf.createModel();
		m2 = rf.createModel();
		
		heavyLoadedOntologyToModel(honto1, m1);
		heavyLoadedOntologyToModel(honto2, m2);
		
		try {
			System.out.println("Model m1 has statements: "+m1.size());
			System.out.println("Model m2 has statements: "+m2.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// JE2010: Strange: why is it not equivalent to call
		// super.align( alignment, params )
		// Load initial alignment
		//loadInit( alignment );

		// Initialize matrix
		getSimilarity().initialize( ontology1(), ontology2(), alignment );
		
		if (this.result == null)
			doMatching();
		
		for (int i=0;i<result.length;i++) {
			try {
				PGNode pg = result[i];
				RDFNode srcNode = pg.getLeftNode();
				RDFNode tgtNode = pg.getRightNode();
				
				Object o1 = honto1.getEntity(new URI(srcNode.toString()));
				Object o2 = honto2.getEntity(new URI(tgtNode.toString()));
				if (o1 != null && o2 != null) {
					//addAlignCell( o1, o2, "<", pg.sim );
				}
			} catch(URISyntaxException e) {
				//e.printStackTrace();
			} catch (OntowrapException e) {
				e.printStackTrace();
			}
		}
		
		// Compute similarity/dissimilarity
		//getSimilarity().compute( params );

		// Print matrix if asked
		//params.setProperty( "algName", getClass()+"/"+methodName );
		//if ( params.getProperty("printMatrix") != null ) printDistanceMatrix( params );

		// Extract alignment
		extract( type, params );

    }
    
    private void doMatching() {
		try {
			this.result = getMatch(m1, m2, null);
			//this.result = getMatch(m1, m2, initSimilarities());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void heavyLoadedOntologyToModel(HeavyLoadedOntology<Object> honto1, Model m) {
		OntModel model = null;
		if (honto1.getOntology() instanceof OWLOntology) {
			OWLOntology onto = (OWLOntology)honto1.getOntology();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

		    OWLOntologyManager owlmanager = onto.getOWLOntologyManager();

		    String format = "TURTLE";
		    //String format = "RDF/XML";

		    if(format.equals("TURTLE")||format.equals("RDF/XML")){

		        if(format.equals("TURTLE"))
		          owlmanager.setOntologyFormat(onto, new TurtleOntologyFormat());
		        else if(format.equals("RDF/XML"))
		          owlmanager.setOntologyFormat(onto,new RDFXMLOntologyFormat());

		        OWLOntologyFormat owlformat = owlmanager.getOntologyFormat(onto);

		        try {
					owlmanager.saveOntology(onto, owlformat, out);
				} catch (OWLOntologyStorageException e) {
					e.printStackTrace();
				}

		        model = ModelFactory.createOntologyModel();
		        OWLOntologyID id = onto.getOntologyID();
		        model.read(new ByteArrayInputStream(out.toByteArray()),id.toString().replace("<","").replace(">",""),format);
		    }       
		}
		
		//IExtendedOntology<Object> onto = (IExtendedOntology<Object>)honto1;	
		//OntModel model = (OntModel)onto.toModel(ExtendedOWLAPI3.JENAMODEL);
		
		RDFFactory rf = new RDFFactoryImpl();
		NodeFactory nf = rf.getNodeFactory();
		Iterator<com.hp.hpl.jena.rdf.model.Statement> iter1 = model.listStatements();
		while (iter1.hasNext()) {
			try {
				com.hp.hpl.jena.rdf.model.Statement st = iter1.next();
				Resource subj = nf.createResource(st.getSubject().toString());
				Resource pred = nf.createResource(st.getPredicate().toString());
				com.hp.hpl.jena.rdf.model.RDFNode objN = st.getObject();
				if (objN.canAs(com.hp.hpl.jena.rdf.model.Literal.class)) {
					m.add(nf.createStatement(subj, pred, nf.createLiteral(objN.toString())));
				}
				else {
					Resource obj = nf.createResource(st.getObject().toString());
					m.add(nf.createStatement(subj, pred, obj));
				}
				//Resource obj = nf.createResource(st.getObject().toString());
				//m.add(nf.createStatement(subj, pred, obj));
			}catch (ModelException e) {
				e.printStackTrace();
			}
		}
	}
    
	/*	  public List execute(List input) throws ModelException {

    Model m1 = (Model)input.get(0);
    Model m2 = (Model)input.get(1);
    List sigma0 = null;
    
    if(input.size() > 2) {
      Model initialMap = (Model)input.get(2);
      sigma0 = MapPair.toMapPairs(initialMap);
      if(sigma0.size() == 0)
	sigma0 = null;
    }

    PGNode[] finalList = getMatch(m1, m2, sigma0);

    Model map = MapPair.asModel(m1.create(), finalList);
    ArrayList l = new ArrayList();
    l.add(map);
    return l;
  }
*/
  public PGNode[] getMatch(Model m1, Model m2, List sigma0) throws ModelException {

    this.m1 = m1;
    this.m2 = m2;
    //    this.sigma0 = rm;

    // computes sigma0 if null; must go before constructPropagationGraph
    if(sigma0 == null)
      initSigma0();
    else {
      // set initial values from sigma0
      for(Iterator it = sigma0.iterator(); it.hasNext();) {

    	  MapPair p = (MapPair)it.next();
    	  PGNode n = (PGNode)pgnodes.get(p);
    	  if(n == null) {
    		  n = new PGNode(p.getLeft(), p.getRight());
    		  pgnodes.put(n, n);
    	  }
    	  n.sim0 = (p.sim == p.NULL_SIM ? 1 : p.sim);
    	  n.inverse = p.inverse; // make sure chosen direction remains unchanged
      }
    }

    // must go AFTER cardMaps
    boolean ignorePredicates = TRY_ALL_ARCS; // || (FLOW_GRAPH_TYPE == FG_STOCHASTIC);
    //boolean ignorePredicates = DIRECTED_GRAPH; // || (FLOW_GRAPH_TYPE == FG_STOCHASTIC);

    long pgstart = System.currentTimeMillis();
    System.err.print("Creating propagation graph: ");
    constructPropagationGraph(ignorePredicates);
    long pgend = System.currentTimeMillis();
    System.err.println("" + (double)(pgend - pgstart) / 1000 + " sec");

    long startTime = System.currentTimeMillis();

    if(TEST) {
      //      System.err.println("Pairwise connectivity graph contains " + stmtPairs.size() + " arcs");
      System.err.println("Propagation graph contains " + pgarcs.size() + " bidirectional arcs and " + pgnodes.size() + " nodes");
      if(DEBUG) {
// 	System.err.println("============ Arcs: ==============");
// 	dump(pgarcs);
// 	System.err.println("============ Nodes: =============");
// 	dump(pgnodes.values());
	System.err.println("EQUAL_PRED_COEFF = " + EQUAL_PRED_COEFF + "\n" +
			   "OTHER_PRED_COEFF = " + OTHER_PRED_COEFF + "\n" +
			   "TRY_ALL_ARCS = " + TRY_ALL_ARCS);
      }
    }

    // create arrays for efficiency

    PGNode[] nodes;
    PGArc[] arcs;

    arcs = new PGArc[pgarcs.size()];
    pgarcs.toArray(arcs);
    pgarcs = null; // free memory

    nodes = new PGNode[pgnodes.size()];
    pgnodes.values().toArray(nodes);
    pgnodes = null; // free memory

    if(TEST)
      System.err.print("Iterating over (" + 
		       m1.size() + " arcs, " + RDFUtil.getNodes(m1).size() + " nodes) x (" +
		       m2.size() + " arcs, " + RDFUtil.getNodes(m2).size() + " nodes): ");

    int iterationNum = MAX_ITERATION_NUM;


    // initialize sigmaN1 := sigma0;
    for(int i=0; i < nodes.length; i++) {
      //      nodes[i].simN1 = rnd.nextDouble();
      //      nodes[i].sim0 /= 1000;
      nodes[i].simN1 = nodes[i].sim0;
    }
    normalizeN1(nodes);


    for(int iteration=0; iteration < iterationNum; iteration++) {

      if(DEBUG && iteration < DEBUG_MAX_ITERATIONS) {
	System.err.println("\nIteration: " + iteration);
	//	debugMap(sigmaN);
      }

      applyFormula(arcs, nodes, iteration);
      
      System.err.print(".");

      normalizeN1(nodes);

      if(DEBUG && iteration < DEBUG_MAX_ITERATIONS) {
	System.err.println("\nAfter norm: " + iteration);
	dump(Arrays.asList(nodes));
      }

      double diff = distance(nodes);

//       double maxN1 = maxN1(nodes);
//       double diff = distanceF(nodes, maxN, maxN1);
//       maxN = maxN1;

      if(TEST) {
	if(DEBUG && iteration < DEBUG_MAX_ITERATIONS)
	  System.err.println("------------------");
	System.err.print("(" + iteration + ":" + diff + ")");
      }

      if(iteration >= MIN_ITERATION_NUM && diff <= RESIDUAL_VECTOR_LENGTH)
	break; // we are done!
      if(System.currentTimeMillis() - startTime > TIMEOUT)
  	break;

      // copy sigmaN+1 into sigmaN and repeat: done at top of loop
    }
    // RETURN

    if(TEST)
      System.err.println(". Time: " +
			 ((double)(System.currentTimeMillis() - startTime) / 1000) + " sec");

    // copy result into sim
    for(int i=0; i < nodes.length; i++) {
      nodes[i].sim = nodes[i].simN1; // / maxN;
      assignFloodMatrixMeasure(nodes[i], (MatrixMeasure)getSimilarity());
    }

    return nodes;
  }

  public void applyFormula(PGArc[] arcs, PGNode[] nodes, int iteration) {

    // special case for default formula

    if(formula == FORMULA_TFT) {
      
      for(int i = nodes.length; --i >= 0;) {

	PGNode n = nodes[i];
	n.sim = (n.simN = n.simN1) + n.sim0;
      }
      propagateValues(arcs);
      return;
    }

    // generic, for all formulas

    boolean add_sigma0_before = formula[0];
    boolean add_sigma0_after = formula[1];
    boolean add_sigmaN_after = formula[2];

    for(int i = nodes.length; --i >= 0;) {
      
      PGNode n = nodes[i];
      // move simN1 values in simN and take current value from previous iteration
      n.sim = n.simN = n.simN1;
      
      if(add_sigma0_before)
	n.sim += n.sim0;
      
      // initialize simN1 for next iteration
      if(!add_sigmaN_after)
	n.simN1 = 0; // otherwise, n.simN1 = n.sim from above

      if(add_sigma0_after)
	n.simN1 += n.sim0;
    }

//     if(DEBUG && iteration < DEBUG_MAX_ITERATIONS) {
//       System.err.println("\nBefore propagation: " + iteration);
//       dump(nodes);
//     }

    propagateValues(arcs);

    if(DEBUG && iteration < DEBUG_MAX_ITERATIONS) {
      System.err.println("\nAfter propagation: " + iteration);
      dump(nodes);
    }

    /*
    if(add_sigma0_after || add_sigmaN_after) {

      for(int i = nodes.length; --i >= 0;) {
      
	PGNode n = nodes[i];

	if(add_sigma0_after)
	  n.simN1 += n.sim0;

	if(add_sigmaN_after)
	  n.simN1 += n.sim;
      }
    }
    */
  }

  public static void propagateValues(PGArc[] arcs) {

    // propagate values from previous iteration over propagation graph

    for(int i = arcs.length; --i >= 0;) {

      PGArc arc = arcs[i];
      // forward
      arc.dest.simN1 += arc.src.sim * arc.fw;
      // backward
      arc.src.simN1 += arc.dest.sim * arc.bw;
    }
  }

  public double distance(PGNode[] nodes) {

    double diff = 0.0;

    for(int i = nodes.length; --i >= 0;) {
      double d = nodes[i].simN1 - nodes[i].simN;
      diff += d*d;
    }

    return Math.sqrt(diff);
  }

  public double distanceF(PGNode[] nodes, double maxN, double maxN1) {

    if(DEBUG)
      System.err.println("Calc distance with maxN=" + maxN + ", maxN1=" + maxN1);

    double diff = 0.0;

    for(int i=0; i < nodes.length; i++) {
      double d = nodes[i].simN1 / maxN1 - nodes[i].simN / maxN;
      diff += d*d;
    }

    return Math.sqrt(diff);
  }

  static double minN(PGNode[] nodes) {
    
    double min = 0;
    for(int i=0; i < nodes.length; i++)
      if(nodes[i].simN > 0)
	min = Math.min(min, nodes[i].simN);
    return min;
  }

  static double maxN1(PGNode[] nodes) {
    
    double max = 0;
    for(int i=0; i < nodes.length; i++)
      max = Math.max(max, nodes[i].simN1);
    return max;
  }

  static double sumN1(PGNode[] nodes) {
    
    double sum = 0;
    for(int i=0; i < nodes.length; i++)
      sum += nodes[i].simN1;
    return sum;
  }

  static double sumN(PGNode[] nodes) {
    
    double sum = 0;
    for(int i=0; i < nodes.length; i++)
      sum += nodes[i].simN;
    return sum;
  }

  static void normalizeN1(PGNode[] nodes) {
    
    double max = maxN1(nodes);
    if(max == 0)
      return;
    for(int i=0; i < nodes.length; i++)
      nodes[i].simN1 /= max;
  }

  public static void dump(Collection c) {

    for(Iterator it = c.iterator(); it.hasNext();)
      System.err.println(String.valueOf(it.next()));
  }

  public static void dump(Object[] arr) {

    dump(Arrays.asList(arr));
  }

  PGNode getNormalNode(RDFNode n1, RDFNode n2) {

    double sim = n1.equals(n2) ? 1.0 : MIN_NODE_SIM2;

    boolean isL1 = n1 instanceof Literal;
    boolean isL2 = n2 instanceof Literal;

    if(isL1 != isL2)
    	sim *= RESOURCE_LITERAL_MATCH_PENALTY;
      //sim *= StringMatcher.RESOURCE_LITERAL_MATCH_PENALTY;

    PGNode node = new PGNode(n1, n2);
    node.sim0 = sim;
    return node;
  }

  // precompute pairs of statements to consider
  // also computes sigma0 for the cross-product of all nodes and literals if needed
  // computes a mapping, in principle

  void initSigma0() throws ModelException {

    System.err.println("All nodes are considered equally similar");
    //    sigma0 = new HashSet();
    Collection c2 = RDFUtil.getNodes(m2).values();
    Iterator it1 = RDFUtil.getNodes(m1).values().iterator();
    while(it1.hasNext()) {
      RDFNode n1 = (RDFNode)it1.next();
      Iterator it2 = c2.iterator();
      while(it2.hasNext()) {
	RDFNode n2 = (RDFNode)it2.next();
	//	MapPair p = getNormalPair(n1, n2);
	//	System.err.println("Reinforce pair: " + p);
	//	sigma0.add(p);
	PGNode pn = getNormalNode(n1, n2);
	//	System.err.println("Init node: " + pn);
	pgnodes.put(pn, pn);
      }
    }
  }
  
  public List<MapPair> initSimilarities() throws AlignmentException {
	  double threshold = 0.3;
	  List<MapPair> ret = new ArrayList<MapPair>();
	  List<Object> classList1 = new ArrayList<Object>();
	  List<Object> classList2 = new ArrayList<Object>();
	  List<Object> propertyList1 = new ArrayList<Object>();
	  List<Object> propertyList2 = new ArrayList<Object>();
	  
	  
	  // Create class lists
	  try {
		  for ( Object cl : honto2.getClasses() ){
			  classList2.add( cl );
		  }
		  for ( Object cl : honto1.getClasses() ){
			  classList1.add( cl );
		  }
		  for ( Object pr : honto2.getProperties() ){
			  propertyList2.add( pr );
		  }
		  for ( Object pr : honto1.getProperties() ){
			  propertyList1.add( pr );
		  }
	  } catch ( OntowrapException owex ) {
		  throw new AlignmentException( "Cannot access class hierarchy", owex );
	  }
	  double[][] classMatrix = new double[classList1.size()+1][classList2.size()+1];
	  double[][] propertyMatrix = new double[propertyList1.size()+1][propertyList2.size()+1];
	  
	  try {
		  RDFFactory rf = new RDFFactoryImpl();
		  NodeFactory nf = rf.getNodeFactory();
		  
		  for (int i=0; i<classList1.size(); i++ ){
			  Object cl1 = classList1.get(i);
			  for (int j=0; j<classList2.size(); j++ ){
				  Object cl2 = classList2.get(j);
				  //classMatrix[i][j] = StringDistances.levenshteinDistance(honto1.getEntityName( cl1 ).toLowerCase(),
					//						 honto2.getEntityName( cl2 ).toLowerCase());
				  classMatrix[i][j] = 1.0 - StringSimilarity.ISub(honto1.getEntityName( cl1 ).toLowerCase(),
											 honto2.getEntityName( cl2 ).toLowerCase());
				  
				  RDFNode n1 = nf.createResource(honto1.getEntityURI(cl1).toString());
				  RDFNode n2 = nf.createResource(honto2.getEntityURI(cl2).toString());
				  MapPair node = new MapPair(n1, n2);
				  node.sim = 1.0-classMatrix[i][j];
				  ret.add(node);
			  }
		  }
		  for (int i=0; i<propertyList1.size(); i++ ){
			  Object pr1 = propertyList1.get(i);
			  for (int j=0; j<propertyList2.size(); j++ ){
				  Object pr2 = propertyList2.get(j);
				  //propertyMatrix[i][j] = StringDistances.levenshteinDistance(honto1.getEntityName( pr1 ).toLowerCase(),
					//						 honto2.getEntityName( pr2 ).toLowerCase());
				  propertyMatrix[i][j] = 1.0 - StringSimilarity.ISub(honto1.getEntityName( pr1 ).toLowerCase(),
							 honto2.getEntityName( pr2 ).toLowerCase());
				  
				  RDFNode n1 = nf.createResource(honto1.getEntityURI(pr1).toString());
				  RDFNode n2 = nf.createResource(honto2.getEntityURI(pr2).toString());
				  MapPair node = new MapPair(n1, n2);
				  node.sim = 1.0-propertyMatrix[i][j];
				  ret.add(node);
			  }
		  }
		} catch ( OntowrapException owex ) {
		    throw new AlignmentException( "Cannot find entity URI", owex );
		} catch (ModelException e) {
			e.printStackTrace();
		}
	  
	  return ret;
  }


  void constructPropagationGraph(boolean ignorePredicates) throws ModelException {

    // SP -> count
    Map cardMapSPLeft, cardMapOPLeft, cardMapPLeft, cardMapSPRight, cardMapOPRight, cardMapPRight;

    cardMapSPLeft = new HashMap();
    cardMapOPLeft = new HashMap();
    cardMapPLeft = new HashMap();

    cardMapSPRight = new HashMap();
    cardMapOPRight = new HashMap();
    cardMapPRight = new HashMap();

    computeCardMaps(m1, cardMapSPLeft, cardMapOPLeft, cardMapPLeft, ignorePredicates);
    computeCardMaps(m2, cardMapSPRight, cardMapOPRight, cardMapPRight, ignorePredicates);


    Map outgoing = new HashMap(); // same as incoming
    //    Map incoming = new HashMap();

    List stmtPairs = new ArrayList();

    for(Enumeration en1 = m1.elements(); en1.hasMoreElements();) {

      Statement st1 = (Statement)en1.nextElement();
      
      if(st1.subject() instanceof Statement ||
	 st1.object() instanceof Statement)
	continue;

      for(Enumeration en2 = m2.elements(); en2.hasMoreElements();) {

	Statement st2 = (Statement)en2.nextElement();

	if(st2.subject() instanceof Statement ||
	   st2.object() instanceof Statement)
	  continue;

// 	if(tryAll) {
// 	  sigma0.add(getNormalPair(st1.subject(), st2.subject()));
// 	  sigma0.add(getNormalPair(st1.object(), st2.object()));
// 	  sigma0.add(getNormalPair(st1.subject(), st2.object()));
// 	  sigma0.add(getNormalPair(st1.object(), st2.subject()));
// 	}

	double ps = 0.0; //predicateSim(st1.predicate(), st2.predicate());
	//	System.err.println("-- " + st1 + " -- " + st2);
	if(st1.predicate().equals(st2.predicate()))
	  ps = EQUAL_PRED_COEFF;
	else if(TRY_ALL_ARCS)
	  ps = OTHER_PRED_COEFF;


	if(ps > 0) {
	  //	  System.err.println("--- ps=" + ps + " from " + EQUAL_PRED_COEFF + ", " + TRY_ALL_ARCS + ", " + OTHER_PRED_COEFF);
	  StmtPair p = new StmtPair(st1, st2, ps,
				    getCard(cardMapSPLeft, st1.subject(), ignorePredicates ? null : st1.predicate()),
				    getCard(cardMapOPLeft, st1.object(), ignorePredicates ? null : st1.predicate()),
				    getCard(cardMapPLeft, null, ignorePredicates ? null : st1.predicate()),
				    getCard(cardMapSPRight, st2.subject(), ignorePredicates ? null : st2.predicate()),
				    getCard(cardMapOPRight, st2.object(), ignorePredicates ? null : st2.predicate()),
				    getCard(cardMapPRight, null, ignorePredicates ? null : st2.predicate())
				    );

// 	  MapPair p = new MapPair(st1, st2, ps);

	  if(FLOW_GRAPH_TYPE == FG_STOCHASTIC || FLOW_GRAPH_TYPE == FG_INCOMING) {

	    // collect the numbers

	    MapPair sourcePair = get(outgoing, st1.subject(), st2.subject());
	    sourcePair.sim += 1.0;
	    
  	    sourcePair = get(outgoing, st1.object(), st2.object());
  	    sourcePair.sim += 1.0;

	    if(TRY_ALL_ARCS) {
	      sourcePair = get(outgoing, st1.subject(), st2.object());
	      sourcePair.sim += 1.0;

	      sourcePair = get(outgoing, st1.object(), st2.subject());
	      sourcePair.sim += 1.0;
	    }

//  	    MapPair targetPair = get(incoming, st1.object(), st2.object());
//  	    targetPair.sim += 1.0;
	  }

	  if(DEBUG)
	    System.err.println("" + p);
	  stmtPairs.add(p);
	}
      }
    }

    if(FLOW_GRAPH_TYPE == FG_STOCHASTIC) {

      Iterator it = stmtPairs.iterator();
      while(it.hasNext()) {

	StmtPair p = (StmtPair)it.next();
	p.soso = 1.0 / get(outgoing, p.stLeft.subject(), p.stRight.subject()).sim;
	p.osos = 1.0 / get(outgoing, p.stLeft.object(), p.stRight.object()).sim;
	if(TRY_ALL_ARCS) {
	  p.soos = 1.0 / get(outgoing, p.stLeft.subject(), p.stRight.object()).sim;
	  p.osso = 1.0 / get(outgoing, p.stLeft.object(), p.stRight.subject()).sim;
	}
	if(DEBUG)
	  System.err.println("Adjusted: " + p);
      }
    } else if(FLOW_GRAPH_TYPE == FG_INCOMING) {

      Iterator it = stmtPairs.iterator();
      while(it.hasNext()) {
	
	StmtPair p = (StmtPair)it.next();
	p.osos = 1.0 / get(outgoing, p.stLeft.subject(), p.stRight.subject()).sim;
	p.soso = 1.0 / get(outgoing, p.stLeft.object(), p.stRight.object()).sim;
	if(TRY_ALL_ARCS) {
	  p.osso = 1.0 / get(outgoing, p.stLeft.subject(), p.stRight.object()).sim;
	  p.soos = 1.0 / get(outgoing, p.stLeft.object(), p.stRight.subject()).sim;
	}
	if(DEBUG)
	  System.err.println("Adjusted: " + p);
      }
    }

    // we don't need cardMaps any more, free memory
    cardMapSPLeft = cardMapOPLeft = cardMapSPRight = cardMapOPRight = cardMapPLeft = cardMapPRight = null;


//     pgnodes = new HashMap();
//     pgarcs = new ArrayList();

    for(Iterator it = stmtPairs.iterator(); it.hasNext();) {

      StmtPair p = (StmtPair)it.next();
      Statement st1 = (Statement)p.stLeft;
      Statement st2 = (Statement)p.stRight;

      PGNode ss = getNode(pgnodes, st1.subject(), st2.subject());
      PGNode oo = getNode(pgnodes, st1.object(), st2.object());

      pgarcs.add(new PGArc(ss, oo, p.soso * UPDATE_GUESS_WEIGHT, p.osos * UPDATE_GUESS_WEIGHT));

      if(!DIRECTED_GRAPH) {

	PGNode so = getNode(pgnodes, st1.subject(), st2.object());
	PGNode os = getNode(pgnodes, st1.object(), st2.subject());

	pgarcs.add(new PGArc(so, os, p.soos * UPDATE_GUESS_WEIGHT, p.osso * UPDATE_GUESS_WEIGHT));
      }
    }
  }


  PGNode getNode(Map table, RDFNode r1, RDFNode r2) {

    PGNode p = new PGNode(r1, r2);
    PGNode res = (PGNode)table.get(p);
    if(res == null) {
      table.put(p, p);
      return p;
    }
    return res;
  }
  
  int getCard(Map cardMap, RDFNode r, Resource pred) {

    MapPair p = get(cardMap, r, pred);
    // there MUST be a pair after computeCardMaps!!!
    return (int)p.sim;
  }

  // collects the number of nodes going out of a node

  void computeCardMaps(Model m, Map cardMapSP, Map cardMapOP, Map cardMapP, boolean ignorePredicates) throws ModelException {

    for(Enumeration en = m.elements(); en.hasMoreElements();) {

      Statement st = (Statement)en.nextElement();
      
      if(st.subject() instanceof Statement ||
	 st.object() instanceof Statement)
	continue;

      Resource pred = ignorePredicates ? null : st.predicate();
      MapPair p = get(cardMapSP, st.subject(), pred);
      p.sim += 1.0;
      
      p = get(cardMapOP, st.object(), pred);
      p.sim += 1.0;

      p = get(cardMapP, null, pred);
      p.sim += 1.0;
    }
  }

  MapPair get(Map table, RDFNode r1, RDFNode r2) {

    MapPair p = setPair(GET_PAIR, r1, r2); // new MapPair(r1, r2); //
    // MapPair p = new MapPair(r1, r2);
    MapPair res = (MapPair)table.get(p);
    if(res == null) {
      res = p.duplicate();
      table.put(res, res);
    }
    return res;
  }

  // this method is used to avoid creating of new objects
  MapPair setPair(int id, RDFNode r1, RDFNode r2) {

    MapPair p = cachePairs[id];
    p.setLeft(r1);
    p.setRight(r2);
    //    p.hash = 0;
    return p;
  }

	
	class PGNode extends MapPair {

	    double sim0;
	    // double sim; corresponds to simN, defined in MapPair
	    double simN1; // N+1
	    double simN; // for comparing vectors, storage only

	    public PGNode(Object r1, Object r2) {

	      super(r1, r2);
	    }

	    public String toString() {

	      return "[" + getLeft() + "," + getRight() + ": sim=" + sim + ", init=" + sim0 + ", N=" + simN + ", N1=" + simN1 + (inverse ? ", inverse" : "") + "]";
	    }
	  }

	  /**
	   * An arc of the propagation graph
	   */
	  class PGArc {

	    double fw, bw; // coefficients on arcs
	    PGNode src, dest;

	    public PGArc(PGNode n1, PGNode n2, double fw, double bw) {

	      this.src = n1;
	      this.dest = n2;
	      this.fw = fw;
	      this.bw = bw;
	    }

	    public String toString() {

	      return src + " <--" + bw + " " + fw + "--> " + dest;
	    }
	  }

	  /**
	   * Instances of this class are used temporarily for creating the propagation graph
	   */
	  class StmtPair {

	    Statement stLeft, stRight;
	    double predSim;
	    //    int spLeft,opLeft,spRight,opRight;
	    double soso,osos,soos,osso;

	    public StmtPair(Statement stLeft, Statement stRight, double predSim,
			    int spLeft, int opLeft, int pLeft, int spRight, int opRight, int pRight) {

	      //      System.err.println("--- predSim=" + predSim);

	      this.stLeft = stLeft;
	      this.stRight = stRight;
	      this.predSim = predSim;

	      switch(FLOW_GRAPH_TYPE) {

	      case SimilarityFloodingAlignment.FG_AVG: {

		double c = 2.0;
		this.soso = c * predSim / (spLeft + spRight);
		this.osos = c * predSim / (opLeft + opRight);
		this.soos = c * predSim / (spLeft + opRight);
		this.osso = c * predSim / (opLeft + spRight);
		//	System.err.println("--- soso=" + soso + " from predSim=" + predSim + ", spLeft="+ spLeft + ", spRight=" + spRight);
		break;
	      }
	      case SimilarityFloodingAlignment.FG_PRODUCT: {

		double c = 1.0;
		this.soso = c * predSim / (spLeft * spRight);
		this.osos = c * predSim / (opLeft * opRight);
		this.soos = c * predSim / (spLeft * opRight);
		this.osso = c * predSim / (opLeft * spRight);
		break;
	      }

		/*
	      case Match.FG_CONSTANT_WEIGHT: { // ignore directionality

		double c = 1.0 / ((spLeft * spRight) + (opLeft * opRight));
		this.soso = c * predSim;
		this.osos = c * predSim;
		this.soos = c * predSim;
		this.osso = c * predSim;
		break;
	      }
		*/
	      case SimilarityFloodingAlignment.FG_EQUAL:
	      case SimilarityFloodingAlignment.FG_INCOMING:
	      case SimilarityFloodingAlignment.FG_STOCHASTIC: { // for constant weight, weight computed here does not matter...

		double c = 1.0;
		this.soso = c * predSim;
		this.osos = c * predSim;
		this.soos = c * predSim;
		this.osso = c * predSim;
		break;
	      }
	      case SimilarityFloodingAlignment.FG_TOTALP: {

		double c = pLeft * pRight;
		this.soso = predSim / c;
		this.osos = predSim / c;
		this.soos = predSim / c;
		this.osso = predSim / c;
		break;
	      }
	      case SimilarityFloodingAlignment.FG_TOTALS: {

		double c = 2.0 / (pLeft + pRight);
		this.soso = predSim * c;
		this.osos = predSim * c;
		this.soos = predSim * c;
		this.osso = predSim * c;
		break;
	      }

	      case SimilarityFloodingAlignment.FG_AVG_TOTALS: {

		double c = 4.0 / (pLeft + pRight);
		this.soso = c * predSim / (spLeft + spRight);
		this.osos = c * predSim / (opLeft + opRight);
		this.soos = c * predSim / (spLeft + opRight);
		this.osso = c * predSim / (opLeft + spRight);
		break;
	      }

	      }
	    }

	    public String toString() {


	      try {
		return "StmtPair[(" + stLeft.subject() + "," + stRight.subject() + ") -> (" +
		  stLeft.object() + "," + stRight.object() + "), " + soso + "->, <-" + osos +
		  (TRY_ALL_ARCS ? " *** (" + stLeft.subject() + "," + stRight.object() + ") -> (" +
		   stLeft.object() + "," + stRight.subject() + "), " + soos + "->, <-" + osso + ")" : "");
	      } catch (ModelException any) {
		return "StmtPair[" + stLeft + "," + stRight + "," + predSim + "," +
		  soso + "," + osos + "," + soos + "," + osso;
	      }
	    }
	  }
}
