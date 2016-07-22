package fr.inrialpes.exmo.align.impl.method.vis.util;

import java.util.ArrayList;

import net.didion.jwnl.data.Synset;

public class Node<T> {

    private T identifier;
    private ArrayList<T> children;
    private int level;

    // Constructor
    public Node(T identifier) {
        this.identifier = identifier;
        children = new ArrayList<T>();
    }

    // Properties
    public T getIdentifier() {
        return identifier;
    }
    
    public int getLevel() {
    	return level;
    }
    public void setLevel(int level) {
    	this.level = level;
    }

    public ArrayList<T> getChildren() {
        return children;
    }

    // Public interface
    public void addChild(T identifier) {
        children.add(identifier);
    }
}
