package cz.cvut.fel.omo.smartfactory.patterns.visitor;

import cz.cvut.fel.omo.smartfactory.models.ReportItem;
import cz.cvut.fel.omo.smartfactory.production.Robot;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.Operator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Visitor for generating reports.
 */
@Getter
public class ReportVisitor implements Visitor {

    private final List<ReportItem> reportItems;

    public ReportVisitor() {
        this.reportItems = new ArrayList<>();
    }

    @Override
    public void visitRobot(Robot robot) {
        ReportItem item = new ReportItem(robot.getId(), "Robot", robot.getStatus().toString());
        reportItems.add(item);
        System.out.println("Added Robot ID: " + robot.getId() + " to report.");
    }

    @Override
    public void visitMachine(Machine machine) {
        ReportItem item = new ReportItem(machine.getId(), "Machine", machine.getStatus().toString());
        reportItems.add(item);
        System.out.println("Added Machine ID: " + machine.getId() + " to report.");
    }

    @Override
    public void visitOperator(Operator operator) {
        ReportItem item = new ReportItem(operator.getId(), "Operator", operator.getStatus().toString());
        reportItems.add(item);
        System.out.println("Added Operator ID: " + operator.getId() + " to report.");
    }

}
