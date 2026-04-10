package cz.cvut.fel.omo.smartfactory.patterns.state;

import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.observer.Event;
import cz.cvut.fel.omo.smartfactory.patterns.singleton.EventPool;

/**
 * State representing a broken entity.
 */
public class BrokenState implements EntityState {
    private final FactoryEntity entity;

    public BrokenState(FactoryEntity entity) {
        this.entity = entity;
    }

    @Override
    public void performTask() {
        System.out.println("Attempting to repair entity ID: " + entity.getId());
        boolean repaired = entity.repair();

        if (repaired) {
            entity.setState(new ActiveState(entity));
            reportSuccess("Entity repaired successfully.");
        } else {
            reportFailure("Repair failed for entity.");
        }
    }

    @Override
    public void handle() {
        System.out.println("Entity ID " + entity.getId() + " is now Broken.");
    }

    private void reportSuccess(String description) {
        EventPool eventPool = EventPool.getInstance();
        Event event = eventPool.borrowEvent();
        event.setSourceId(entity.getId());
        event.setDescription(description);
        event.setTimestamp(java.time.Instant.now());

        entity.notifyObservers(event);

        eventPool.returnEvent(event);
    }

    private void reportFailure(String description) {
        EventPool eventPool = EventPool.getInstance();
        Event event = eventPool.borrowEvent();
        event.setSourceId(entity.getId());
        event.setDescription(description);
        event.setTimestamp(java.time.Instant.now());

        entity.notifyObservers(event);

        eventPool.returnEvent(event);
    }
}

