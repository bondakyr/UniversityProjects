package cz.cvut.fel.omo.smartfactory.patterns.strategy;

import cz.cvut.fel.omo.smartfactory.production.Robot;

/**
 * Rozhraní pro strategie úkolů.
 */
public interface TaskStrategy {
    /**
     * Vykoná úkol.
     */
    void execute(Robot robot);
}
