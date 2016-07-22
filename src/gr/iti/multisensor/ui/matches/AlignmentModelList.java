package gr.iti.multisensor.ui.matches;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import gr.iti.multisensor.matrix.MSMappingList;
import gr.iti.multisensor.matrix.MSMappingPair;

public class AlignmentModelList {
	List<AlignmentModel> alignments;
	URIAlignment basicAlignment;
	
	LoadedOntology<Object> onto1;
	LoadedOntology<Object> onto2;
	
	public AlignmentModelList() {
		alignments = new ArrayList<AlignmentModel>();
		basicAlignment = new URIAlignment();
	}
	
	public void setOntology1(LoadedOntology<Object> onto1) {
		this.onto1 = onto1;
	}
	public void setOntology2(LoadedOntology<Object> onto2) {
		this.onto2 = onto2;
	}
	public LoadedOntology<Object> getOntology1() {
		return onto1;
	}
	public LoadedOntology<Object> getOntology2() {
		return onto2;
	}
	
	public List<AlignmentModel> getAlignments() {
		return alignments;
	}
	public URIAlignment getBasicAlignment() {
		return basicAlignment;
	}
	public void initBasicAlignment() {
		try {
			basicAlignment.init(onto1, onto2);
		} catch (AlignmentException e) {
			e.printStackTrace();
		}
	}
	
	public void populateList(List<MSMappingList> lists) {
		if (lists.size() > 0) {
			if (basicAlignment.getArrayElements().size() > 0)
				basicAlignment.deleteAllCells();
			if (alignments.size() > 0)
				alignments.clear();
			//classes are in index 0
			MSMappingList listC = lists.get(0);
			for (MSMappingPair map : listC.getMappingList()) {
				String src = map.getSrcResource().toString();
				String tgt = map.getTgtResource().toString();
				String rel = map.getRelation();
				double conf = map.getConf();
				AlignmentModel m = new AlignmentModel(src, tgt, rel, conf);
				alignments.add(m);
				try {
					//basicAlignment.addAlignCell(map.getSrcResource(), map.getTgtResource(), map.getRelation(), map.getConf());
					basicAlignment.addAlignCell(onto1.getEntityURI(map.getSrcResource()), onto2.getEntityURI(map.getTgtResource()), map.getRelation(), map.getConf());
				} catch (AlignmentException e) {
					e.printStackTrace();
				} catch (OntowrapException e) {
					e.printStackTrace();
				}
			}
		
			if (lists.size() > 1) {
				//properties are in index 1
				MSMappingList listP = lists.get(1);
				for (MSMappingPair map : listP.getMappingList()) {
					String src = map.getSrcResource().toString();
					String tgt = map.getTgtResource().toString();
					String rel = map.getRelation();
					double conf = map.getConf();
					AlignmentModel m = new AlignmentModel(src, tgt, rel, conf);
					alignments.add(m);
					try {
						//basicAlignment.addAlignCell(map.getSrcResource(), map.getTgtResource(), map.getRelation(), map.getConf());
						basicAlignment.addAlignCell(onto1.getEntityURI(map.getSrcResource()), onto2.getEntityURI(map.getTgtResource()), map.getRelation(), map.getConf());
					} catch (AlignmentException e) {
						e.printStackTrace();
					} catch (OntowrapException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public Object checkResource(LoadedOntology<Object> onto, URI resource) {
		Object test = null;
		try {
			test = onto.getEntity(resource);
		} catch (OntowrapException e) {
			e.printStackTrace();
		}
		
		return test;
	}
}
