package fr.inrialpes.exmo.align.impl.method.vis.util;

import java.util.*;

import net.didion.jwnl.data.Synset;

public class BreadthFirstTreeIterator<T> implements Iterator<Node> {

    private static final int ROOT = 0;

    private LinkedList<Node> list;
    private HashMap<Integer, ArrayList<T>> levels;

    public BreadthFirstTreeIterator(HashMap<T, Node> tree, T identifier) {
        list = new LinkedList<Node>();
        levels = new HashMap<Integer, ArrayList<T>>();

        if (tree.containsKey(identifier)) {
            this.buildList(tree, identifier, ROOT);

            for (Map.Entry<Integer, ArrayList<T>> entry : levels.entrySet()) {
                for (T child : entry.getValue()) {
                    list.add(tree.get(child));
                }
            }
        }
    }

    private void buildList(HashMap<T, Node> tree, T identifier, int level) {
        if (level == ROOT) {
            list.add(tree.get(identifier));
        }

        ArrayList<T> children = tree.get(identifier).getChildren();

        if (!levels.containsKey(level)) {
            levels.put(level, new ArrayList<T>());
        }
        for (T child : children) {
            levels.get(level).add(child);

            // Recursive call
            this.buildList(tree, child, level + 1);
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

