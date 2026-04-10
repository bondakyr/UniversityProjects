package cz.cvut.fel.omo.smartfactory.patterns.singleton;

import cz.cvut.fel.omo.smartfactory.patterns.observer.Event;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Singleton Object Pool for Event objects.
 */
public class EventPool {

    private final Queue<Event> availableEvents;
    private final int maxSize;
    private int currentSize = 0;

    private static volatile EventPool instance;

    /**
     * Private constructor to prevent instantiation.
     *
     * @param maxSize Maximum number of Event objects in the pool.
     */
    public EventPool(int maxSize) {
        this.availableEvents = new LinkedList<>();
        this.maxSize = maxSize;
    }

    /**
     * Gets the singleton instance of EventPool without altering maxSize.
     *
     * @return Singleton instance of EventPool.
     * @throws IllegalStateException If instance has not been initialized yet.
     */
    public static EventPool getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EventPool not initialized. Call getInstance(int maxSize) first.");
        }
        return instance;
    }

    /**
     * Získání Event objektu z poolu.
     *
     * @return An instance of Event.
     */
    public synchronized Event borrowEvent() {
        if (!availableEvents.isEmpty()) {
            return availableEvents.poll();
        } else if (currentSize < maxSize) {
            Event newEvent = new Event();
            currentSize++;
            return newEvent;
        } else {
            throw new RuntimeException("Maximum Event pool size reached, no available Events.");
        }
    }

    /**
     * Vrácení Event objektu do poolu.
     *
     * @param event The Event to return.
     */
    public synchronized void returnEvent(Event event) {
        if (event != null) {
            event.reset();
            availableEvents.offer(event);
        }
    }
}
