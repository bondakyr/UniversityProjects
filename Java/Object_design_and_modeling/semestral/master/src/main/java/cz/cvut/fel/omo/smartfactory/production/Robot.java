package cz.cvut.fel.omo.smartfactory.production;

import cz.cvut.fel.omo.smartfactory.api.DeviceDataAPI;
import cz.cvut.fel.omo.smartfactory.models.TaskType;
import cz.cvut.fel.omo.smartfactory.patterns.pool.TaskStrategyPool;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.strategy.TaskStrategy;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor;
import cz.cvut.fel.omo.smartfactory.patterns.builder.StringBuilderPattern;

public class Robot extends FactoryEntity implements DeviceDataAPI {
    private double electricityConsumption;
    private double oilConsumption;
    private double materialUsage;
    private double wearAndTear;
    private final TaskStrategyPool strategyPool;

    public Robot(int id, TaskType taskType, double reliability, double efficiency, TaskStrategyPool strategyPool) {
        super(id);
        this.strategyPool = strategyPool;
        this.electricityConsumption = 0;
        this.oilConsumption = 0;
        this.materialUsage = 0;
        this.wearAndTear = 0;
    }

    @Override
    public void performTask() {
        TaskStrategy strategy = this.strategyPool.acquireWeldingStrategy();
        strategy.execute(this);
        this.electricityConsumption += 5;
        this.oilConsumption += 1;
        this.materialUsage += 2;
        this.wearAndTear += 0.1;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRobot(this);
    }

    @Override
    public double getElectricityConsumption() {
        return this.electricityConsumption;
    }

    @Override
    public double getOilConsumption() {
        return this.oilConsumption;
    }

    @Override
    public double getMaterialUsage() {
        return this.materialUsage;
    }

    @Override
    public double getWearAndTear() {
        return this.wearAndTear;
    }

    @Override
    public String toString() {
        return getDetails();
    }

    public String getDetails() {
        return new StringBuilderPattern()
                .appendHeader("Robot Details")
                .append("ID", this.getId())
                .append("Electricity Consumption", this.getElectricityConsumption() + " kWh")
                .append("Oil Consumption", this.getOilConsumption() + " liters")
                .append("Material Usage", this.getMaterialUsage() + " units")
                .append("Wear and Tear", this.getWearAndTear() + " %")
                .addSeparator()
                .build();
    }
}
