package cz.cvut.fel.omo.smartfactory.patterns.decorator;

import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor;

/**
 * Abstract decorator class for FactoryEntity.
 */
public abstract class FactoryEntityDecorator extends FactoryEntity {

    /**
     * The FactoryEntity being decorated.
     */
    protected FactoryEntity decoratedEntity;

    /**
     * Constructor for FactoryEntityDecorator.
     *
     * @param decoratedEntity The FactoryEntity to decorate.
     */
    public FactoryEntityDecorator(FactoryEntity decoratedEntity) {
        super(decoratedEntity.getId());
        this.decoratedEntity = decoratedEntity;
        this.status = decoratedEntity.getStatus();
        this.currentState = decoratedEntity.getCurrentState();
    }

    @Override
    public void attach(cz.cvut.fel.omo.smartfactory.patterns.observer.Observer observer) {
        decoratedEntity.attach(observer);
    }

    @Override
    public void detach(cz.cvut.fel.omo.smartfactory.patterns.observer.Observer observer) {
        decoratedEntity.detach(observer);
    }

    @Override
    public void notifyObservers(cz.cvut.fel.omo.smartfactory.patterns.observer.Event event) {
        decoratedEntity.notifyObservers(event);
    }

    @Override
    public void performTask() {
        decoratedEntity.performTask();
    }

    @Override
    public void accept(Visitor visitor) {
        decoratedEntity.accept(visitor);
    }

    @Override
    public void reportIssue(cz.cvut.fel.omo.smartfactory.models.Issue issue) {
        decoratedEntity.reportIssue(issue);
    }

    @Override
    public void setState(cz.cvut.fel.omo.smartfactory.patterns.state.EntityState newState) {
        decoratedEntity.setState(newState);
    }
}
