package cz.cvut.fel.omo.smartfactory.patterns.state;

/**
 * Interface for entity states in the State pattern.
 */
public interface EntityState {
    /**
     * Handle actions when entering this state.
     */
    void handle();

    /**
     * Perform task based on the current state.
     */
    void performTask();
}
