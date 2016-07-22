package gr.iti.multisensor.ui.matches;

public class AlignmentModel {
	
	String source;
	String target;
	String rel;
	double conf;
	
	public AlignmentModel(String s, String t, String rel, double conf) {
		this.source = s;
		this.target = t;
		this.rel = rel;
		this.conf = conf;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getTarget() {
		return target;
	}
	
	public String getRelation() {
		return rel;
	}
	
	public double getConf() {
		return conf;
	}

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setConf(double conf) {
		this.conf = conf;
	}
	
	
}
