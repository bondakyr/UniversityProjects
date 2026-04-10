package cz.cvut.fel.omo.smartfactory.patterns.decorator;

import cz.cvut.fel.omo.smartfactory.models.Issue;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;

/**
 * Decorator that adds logging functionality to FactoryEntity.
 */
public class LoggingDecorator extends FactoryEntityDecorator {

    /**
     * Constructor for LoggingDecorator.
     *
     * @param decoratedEntity The FactoryEntity to decorate.
     */
    public LoggingDecorator(FactoryEntity decoratedEntity) {
        super(decoratedEntity);
    }

    @Override
    public void performTask() {
        System.out.println("Logging: Starting task for Entity ID " + id);
        decoratedEntity.performTask();
        System.out.println("Logging: Finished task for Entity ID " + id);
    }

    @Override
    public void reportIssue(Issue issue) {
        System.out.println("Logging: Reporting issue for Entity ID " + id + ": " + issue.getDescription());
        decoratedEntity.reportIssue(issue);
        System.out.println("Logging: Issue reported for Entity ID " + id);
    }
}
