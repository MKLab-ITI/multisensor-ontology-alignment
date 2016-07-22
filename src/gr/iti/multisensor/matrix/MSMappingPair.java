package gr.iti.multisensor.matrix;

import java.net.URI;

public class MSMappingPair {
	private Object srcResource;
	private Object tgtResource;
	private int id;
	private String relation;
	private double conf;
	private double[] scores; //the scores from the matchers. Used for machine learning
	
	public MSMappingPair(int id, Object srcResource, Object tgtResource, String relation, double conf) {
		this.srcResource = srcResource;
		this.tgtResource = tgtResource;
		this.id = id;
		this.conf = conf;
		this.relation = relation;
	}
	
	public MSMappingPair(int id, Object srcResource, Object tgtResource, String relation, double conf, double[] scores) {
		this.srcResource = srcResource;
		this.tgtResource = tgtResource;
		this.id = id;
		this.conf = conf;
		this.relation = relation;
		this.scores = scores;
	}

	public Object getSrcResource() {
		return srcResource;
	}

	public void setSrcResource(Object srcResource) {
		this.srcResource = srcResource;
	}

	public Object getTgtResource() {
		return tgtResource;
	}

	public void setTgtResource(Object tgtResource) {
		this.tgtResource = tgtResource;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public double getConf() {
		return conf;
	}

	public void setConf(double conf) {
		this.conf = conf;
	}
	
	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}

/*	@Override
	public String toString() {
		return "<"+id+", "+srcResource.getID()+", "+tgtResource.getID()+", "+relation+", "+conf+">";
	}
*/
}
