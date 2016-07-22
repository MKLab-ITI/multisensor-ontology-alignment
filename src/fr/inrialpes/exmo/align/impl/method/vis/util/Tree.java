package fr.inrialpes.exmo.align.impl.method.vis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Tree<T> {

    private final static int ROOT = 0;

    private HashMap<T, Node<T>> nodes;
    private TraversalStrategy traversalStrategy;
    private int treeDepth = 0;

    // Constructors
    public Tree() {
        //this(TraversalStrategy.DEPTH_FIRST);
    	this(TraversalStrategy.BREADTH_FIRST);
    }

    public Tree(TraversalStrategy traversalStrategy) {
        this.nodes = new HashMap<T, Node<T>>();
        this.traversalStrategy = traversalStrategy;
    }

    // Properties
    public HashMap<T, Node<T>> getNodes() {
        return nodes;
    }

    public TraversalStrategy getTraversalStrategy() {
        return traversalStrategy;
    }

    public void setTraversalStrategy(TraversalStrategy traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
    }
    
    public int getDepth() {
    	return treeDepth;
    }

    // Public interface
    public Node<T> addNode(T identifier) {
    	Node<T> node = this.addNode(identifier, null);
    	node.setLevel(0);
        
    	return node;
    }

    public Node<T> addNode(T identifier, T parent) {
        Node<T> node = new Node<T>(identifier);
        nodes.put(identifier, node);

        //if (parent != null && nodes.get(parent) != null) {
        if (parent != null) {
            nodes.get(parent).addChild(identifier);
            node.setLevel(nodes.get(parent).getLevel()+1);
            if (node.getLevel() > this.treeDepth)
            	this.treeDepth = node.getLevel();
        }

        return node;
    }

    public void display(T identifier) {
        this.display(identifier, ROOT);
    }

    public void display(T identifier, int depth) {
        ArrayList<T> children = nodes.get(identifier).getChildren();

        if (depth == ROOT) {
            System.out.println(nodes.get(identifier).getIdentifier() + " - "+nodes.get(identifier).getLevel());
        } else {
            String tabs = String.format("%0" + depth + "d", 0).replace("0", "    "); // 4 spaces
            System.out.println(tabs + nodes.get(identifier).getIdentifier() + " - "+nodes.get(identifier).getLevel());
        }
        depth++;
        for (T child : children) {

            // Recursive call
            this.display(child, depth);
        }
    }

    public Iterator<Node> iterator(T identifier) {
        return this.iterator(identifier, traversalStrategy);
    }

    public Iterator<Node> iterator(T identifier, TraversalStrategy traversalStrategy) {
        return traversalStrategy == TraversalStrategy.BREADTH_FIRST ?
                new BreadthFirstTreeIterator(nodes, identifier) :
                new DepthFirstTreeIterator(nodes, identifier);
    }
}