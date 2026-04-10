
package cz.cvut.fel.omo.smartfactory.patterns.observer;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an event in the factory system.
 */
public class Event implements Comparable<Event> {
    @Getter
    @Setter
    private int sourceId;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private Instant timestamp;
    private final int priority;

    public Event(int sourceId, String description, int priority) {
        this.sourceId = sourceId;
        this.description = description;
        this.timestamp = Instant.now();
        this.priority = priority;
    }

    public Event(int sourceId, String description) {
        this(sourceId, description, 5);
    }

    public Event() {
        this(-1, "", 5);
    }

    public void reset() {
    }

    @Override
    public int compareTo(Event other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return "Event{" +
                "sourceId=" + sourceId +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", priority=" + priority +
                '}';
    }
}
