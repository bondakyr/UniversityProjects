package cz.cvut.fel.omo.smartfactory.patterns.observer;

import cz.cvut.fel.omo.smartfactory.models.Issue;
import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.models.TaskType;
import cz.cvut.fel.omo.smartfactory.patterns.state.EntityState;
import cz.cvut.fel.omo.smartfactory.patterns.state.ActiveState;
import cz.cvut.fel.omo.smartfactory.patterns.state.BrokenState;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor;
import cz.cvut.fel.omo.smartfactory.patterns.singleton.EventPool;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all factory entities.
 * Implements the Subject interface for the Observer pattern.
 */
@Getter
@Setter
public abstract class FactoryEntity implements Subject {

    /**
     * Unique identifier for the entity.
     */
    protected int id;

    /**
     * Current status of the entity.
     */
    protected Status status;

    /**
     * List of observers attached to this entity.
     */
    private List<Observer> observers = new ArrayList<>();

    /**
     * Current state of the entity.
     */
    protected EntityState currentState;

    /**
     * Current task type assigned to the entity.
     */
    @Getter
    protected TaskType currentTaskType;

    /**
     * Counter for tasks performed, může být použito pro statistiky nebo logiku stavů.
     */
    protected int taskCounter = 0;

    /**
     * Constructor for FactoryEntity.
     *
     * @param id Unique identifier.
     */
    public FactoryEntity(int id) {
        this.id = id;
        this.status = Status.ACTIVE;
        this.currentState = new ActiveState(this);
    }

    /**
     * Attach an observer to this entity.
     *
     * @param observer The observer to attach.
     */
    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Detach an observer from this entity.
     *
     * @param observer The observer to detach.
     */
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notify all attached observers about an event.
     *
     * @param event The event to notify about.
     */
    @Override
    public void notifyObservers(Event event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }

    /**
     * Perform the current task based on the entity's state.
     */
    public void performTask() {
        currentState.performTask();
    }

    /**
     * Change the state of the entity.
     *
     * @param newState The new state to transition to.
     */
    public void setState(EntityState newState) {
        if (newState != null) {
            this.currentState = newState;
            this.currentState.handle();
        }
    }

    /**
     * Abstract method to accept a visitor.
     *
     * @param visitor The visitor instance.
     */
    public abstract void accept(Visitor visitor);

    /**
     * Execute the assigned task using a strategy or specific logic.
     *
     * @param taskType The task type to execute.
     */
    public void executeTask(TaskType taskType) {
        taskCounter++;
        if(Math.random() > getReliability()) {
            reportIssue(new Issue("Task execution failed."));
        }
    }

    /**
     * Simulace opravy entity.
     *
     * @return True, pokud byla oprava úspěšná, jinak False.
     */
    @Override
    public boolean repair() {
        if (status == Status.BROKEN) {
            System.out.println(getClass().getSimpleName() + " ID " + getId() + " byl opraven.");
            this.status = Status.ACTIVE;
            return true;
        }
        return false;
    }


    /**
     * Simulace získání reliability entity.
     *
     * @return Reliabilita entity (0.0 - 1.0).
     */
    public double getReliability() {
        return 0.95;
    }

    /**
     * Report an issue in the entity.
     *
     * @param issue The issue to report.
     */
    public void reportIssue(Issue issue) {
        EventPool eventPool = EventPool.getInstance(); // Bez parametru, protože pool již byl inicializován
        Event event = eventPool.borrowEvent();
        event.setSourceId(this.id);
        event.setDescription("Issue reported: " + issue.getDescription());
        event.setTimestamp(java.time.Instant.now());

        setState(new BrokenState(this));
        notifyObservers(event);
        eventPool.returnEvent(event);
    }

    /**
     * Zjistí, zda úkol selhal (může být implementováno na základě další logiky).
     *
     * @return True, pokud úkol selhal, jinak False.
     */
    public boolean isTaskFailed() {
        return this.status == Status.BROKEN;
    }
}
