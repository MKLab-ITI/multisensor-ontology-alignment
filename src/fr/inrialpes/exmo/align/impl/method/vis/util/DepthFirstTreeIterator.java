package fr.inrialpes.exmo.align.impl.method.vis.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.didion.jwnl.data.Synset;

/*
 * See URL: http://en.wikipedia.org/wiki/Depth-first_search
 */

public class DepthFirstTreeIterator<T> implements Iterator<Node> {
    private LinkedList<Node> list;

    public DepthFirstTreeIterator(HashMap<T, Node> tree, T identifier) {
        list = new LinkedList<Node>();

        if (tree.containsKey(identifier)) {
            this.buildList(tree, identifier);
        }
    }

    private void buildList(HashMap<T, Node> tree, T identifier) {
        list.add(tree.get(identifier));
        ArrayList<T> children = tree.get(identifier).getChildren();
        for (T child : children) {

            // Recursive call
            this.buildList(tree, child);
        }
    }

    @Override
    public boolean hasNext() {
        return !list.isEmpty();
    }

    @Override
    public Node next() {
        return list.poll();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}