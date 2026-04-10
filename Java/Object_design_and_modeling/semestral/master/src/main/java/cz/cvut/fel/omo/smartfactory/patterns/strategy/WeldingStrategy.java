package cz.cvut.fel.omo.smartfactory.patterns.strategy;

import cz.cvut.fel.omo.smartfactory.production.Robot;

public class WeldingStrategy implements TaskStrategy {
    @Override
    public void execute(Robot robot) {
        System.out.println("Strategy: Robot ID=" + robot.getId() + " is welding something...");
    }
}

