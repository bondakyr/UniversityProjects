package cz.cvut.fel.omo.smartfactory.patterns.visitor;

import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.Operator;
import cz.cvut.fel.omo.smartfactory.production.Robot;

/**
 * Visitor interface for Visitor pattern.
 */
public interface Visitor {
    void visitRobot(Robot robot);
    void visitMachine(Machine machine);
    void visitOperator(Operator operator);
}