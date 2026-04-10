package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.Node;

public class NodeImpl implements Node {

    private int value;
    private NodeImpl left;
    private NodeImpl right;

    public NodeImpl(int value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }

    @Override
    public Node getLeft() {
        return left;
    }

    public void setLeft(NodeImpl left) {
        this.left = left;
    }

    @Override
    public Node getRight() {
        return right;
    }

    public void setRight(NodeImpl right) {
        this.right = right;
    }

    @Override
    public int getValue() {
        return value;
    }
}