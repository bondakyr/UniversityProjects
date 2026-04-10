package cz.cvut.fel.omo.smartfactory.patterns.factory;

import cz.cvut.fel.omo.smartfactory.models.MachineType;
import cz.cvut.fel.omo.smartfactory.models.TaskType;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.Operator;
import cz.cvut.fel.omo.smartfactory.production.Robot;
import cz.cvut.fel.omo.smartfactory.patterns.pool.TaskStrategyPool;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;

/**
 * Factory for creating factory entities.
 */
public class FactoryEntityFactory {
    private static int entityCounter = 1;

    public static FactoryEntity createFactoryEntity(String entityType) {
        int newId = entityCounter++;
        switch (entityType.toLowerCase()) {
            case "robot":
                return new Robot(newId, TaskType.WELDING, 0.95, 1.0, new TaskStrategyPool());
            case "machine":
                return new Machine(newId, MachineType.CUTTER, 200);
            case "human":
                return new Operator(newId, "Operator_" + newId);
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }
}