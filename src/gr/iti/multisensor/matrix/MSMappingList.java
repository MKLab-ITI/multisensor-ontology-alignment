package gr.iti.multisensor.matrix;

import java.util.ArrayList;
import java.util.List;

public class MSMappingList {
	List<MSMappingPair> mappingList;
	String mapperName;
	
	public MSMappingList(String mapperName) {
		mappingList = new ArrayList<MSMappingPair>();
		this.mapperName = mapperName;
	}
	
	public String getMapperName() {
		return mapperName;
	}
	
	public void addMappingPair(MSMappingPair pair) {
		if (pair != null) {
			mappingList.add(pair);
		}
	}
	public void addMappingPairs(List<MSMappingPair> pairList) {
		if (pairList != null) {
			mappingList.addAll(pairList);
		}
	}
	
	public List<MSMappingPair> getMappingList() {
		return mappingList;
	}
	
/*	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer(1000);
		for (MyMappingPair pair : mappingList) {
			ret.append("<").append(pair.getSrcResource().getID()).append(", ").append(pair.getTgtResource().getID());
			ret.append(", =, ").append(pair.getConf()).append(">\n");
		}
		
		return ret.toString();
	}
*/
}
