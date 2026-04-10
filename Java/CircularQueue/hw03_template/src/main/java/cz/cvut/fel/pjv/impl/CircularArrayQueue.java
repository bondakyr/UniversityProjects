package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.Queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the {@link Queue} backed by fixed size array.
 */
public class CircularArrayQueue implements Queue {

    private final String[] elements;
    private final int capacity;
    private int size;
    private int front;
    private int rear;

    /**
     * Creates the queue with capacity set to the value of 5.
     */
    public CircularArrayQueue() {
        this(5);
    }

    /**
     * Creates the queue with given {@code capacity}.
     * The capacity represents maximal number of elements that the
     * queue is able to store.
     * @param capacity of the queue
     */
    public CircularArrayQueue(int capacity) {
        this.capacity = capacity;
        this.elements = new String[capacity];
        this.size = 0;
        this.front = 0;
        this.rear = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean isFull() {
        return size == capacity;
    }

    @Override
    public boolean enqueue(String obj) {
        if (obj == null || isFull()) {
            return false;
        }
        elements[rear] = obj;
        rear = (rear + 1) % capacity;
        size++;
        return true;
    }

    @Override
    public String dequeue() {
        if (!isEmpty()) {
            String removed = elements[front];
            elements[front] = null;
            front = (front + 1) % capacity;
            size--;
            return removed;
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getElements() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(elements[(front + i) % capacity]);
        }
        return list;
    }

    @Override
    public void printAllElements() {
        for (String element : getElements()) {
            System.out.println(element);
        }
    }
}