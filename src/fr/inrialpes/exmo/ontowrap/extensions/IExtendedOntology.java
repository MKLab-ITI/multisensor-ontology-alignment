package fr.inrialpes.exmo.ontowrap.extensions;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;

public interface IExtendedOntology<O> extends HeavyLoadedOntology<O> {
	public Object toModel(String model);
}
