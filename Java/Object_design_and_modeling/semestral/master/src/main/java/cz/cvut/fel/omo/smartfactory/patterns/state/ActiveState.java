package cz.cvut.fel.omo.smartfactory.patterns.state;

import cz.cvut.fel.omo.smartfactory.models.TaskType;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.observer.Event;
import cz.cvut.fel.omo.smartfactory.patterns.singleton.EventPool;

/**
 * State representing an active entity.
 */
public class ActiveState implements EntityState {
    private final FactoryEntity entity;

    public ActiveState(FactoryEntity entity) {
        this.entity = entity;
    }

    @Override
    public void performTask() {
        TaskType task = entity.getCurrentTaskType();
        if (task != null) {
            entity.executeTask(task);
        }

        if (entity.isTaskFailed()) {
            entity.setState(new BrokenState(entity));
            reportFailure("Task failed for entity.");
        }
    }

    @Override
    public void handle() {
        System.out.println("Entity ID " + entity.getId() + " is now Active.");
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
