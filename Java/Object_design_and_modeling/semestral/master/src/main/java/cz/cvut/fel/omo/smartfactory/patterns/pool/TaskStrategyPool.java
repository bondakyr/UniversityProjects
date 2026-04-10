package cz.cvut.fel.omo.smartfactory.patterns.pool;

import cz.cvut.fel.omo.smartfactory.patterns.strategy.TaskStrategy;
import cz.cvut.fel.omo.smartfactory.patterns.strategy.WeldingStrategy;

import java.util.Stack;

/**
 * Pool pro správu instancí TaskStrategy.
 */
public class TaskStrategyPool {

    private final Stack<TaskStrategy> weldingStrategies;

    public TaskStrategyPool() {
        this.weldingStrategies = new Stack<>();
    }

    /**
     * Vrátí instanci WeldingStrategy ze zásobníku (pokud tam je),
     * nebo vytvoří novou, pokud je zásobník prázdný.
     */
    public TaskStrategy acquireWeldingStrategy() {
        if (!weldingStrategies.isEmpty()) {
            return weldingStrategies.pop();
        } else {
            return new WeldingStrategy();
        }
    }
}
