package cz.cvut.fel.omo.smartfactory.patterns.observer;


/**
 * Observer interface for the Observer pattern.
 */
public interface Observer {
    /**
     * Update method called by Subject.
     *
     * @param event The event that occurred.
     */
    void update(Event event);
}
