package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.Node;
import cz.cvut.fel.pjv.Tree;

public class TreeImpl implements Tree {

    private NodeImpl root;

    public TreeImpl() {
        this.root = null;
    }

    @Override
    public void setTree(int[] values) {
        if (values == null || values.length == 0) {
            root = null;
            return;
        }
        root = createTree(values, 0, values.length - 1);
    }

    private NodeImpl createTree(int[] values, int start, int end) {
        if (start > end) {
            return null;
        }

        int mid = start + (end - start + 1) / 2;

        NodeImpl node = new NodeImpl(values[mid]);
        node.setLeft(createTree(values, start, mid - 1));
        node.setRight(createTree(values, mid + 1, end));

        return node;
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public String toString() {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        toStringHelper(root, sb, 0);
        return sb.toString();
    }

    private void toStringHelper(Node node, StringBuilder sb, int depth) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < depth; i++) {
            sb.append(" ");
        }

        sb.append("- ").append(node.getValue()).append("\n");
        toStringHelper(node.getLeft(), sb, depth + 1);
        toStringHelper(node.getRight(), sb, depth + 1);
    }
}