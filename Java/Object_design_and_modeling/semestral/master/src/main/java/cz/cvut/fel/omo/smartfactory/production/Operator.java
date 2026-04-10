package cz.cvut.fel.omo.smartfactory.production;

import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor;
import cz.cvut.fel.omo.smartfactory.patterns.builder.StringBuilderPattern;

public class Operator extends FactoryEntity {
    private final String operatorName;

    public Operator(int id, String operatorName) {
        super(id);
        this.operatorName = operatorName;
    }

    @Override
    public void performTask() {
        System.out.println(operatorName + " is working.");
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitOperator(this);
    }

    @Override
    public String toString() {
        return getDetails();
    }

    public String getDetails() {
        return new StringBuilderPattern()
                .appendHeader("Operator Details")
                .append("ID", this.getId())
                .append("Name", this.operatorName)
                .addSeparator()
                .build();
    }
}
