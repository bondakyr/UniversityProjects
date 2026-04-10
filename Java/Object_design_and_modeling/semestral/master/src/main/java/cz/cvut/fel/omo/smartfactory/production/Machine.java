package cz.cvut.fel.omo.smartfactory.production;

import cz.cvut.fel.omo.smartfactory.api.DeviceDataAPI;
import cz.cvut.fel.omo.smartfactory.models.MachineType;
import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor;
import cz.cvut.fel.omo.smartfactory.patterns.builder.StringBuilderPattern;

public class Machine extends FactoryEntity implements DeviceDataAPI {
    private final MachineType machineType;
    private double electricityConsumption;
    private double oilConsumption;
    private double materialUsage;
    private double wearAndTear;
    private static final double BREAK_CHANCE = 0.1;

    public Machine(int id, MachineType machineType, double capacity) {
        super(id);
        this.machineType = machineType;
        this.electricityConsumption = 0;
        this.oilConsumption = 0;
        this.materialUsage = 0;
        this.wearAndTear = 0;
    }

    @Override
    public void performTask() {
        this.electricityConsumption += 8;
        this.oilConsumption += 2;
        this.materialUsage += 5;
        this.wearAndTear += 0.05;

        if (Math.random() < BREAK_CHANCE) {
            this.status = Status.BROKEN;
            System.out.println("Machine ID " + this.id + " se rozbil!");
        }

    }

    @Override
    public boolean repair() {
        System.out.println(this.getClass().getSimpleName() + " ID " + getId() + " se opravuje...");
        boolean repaired = super.repair();
        if (repaired) {
            resetConsumption();
        }
        return repaired;
    }

    private void resetConsumption() {
        electricityConsumption = 0;
        oilConsumption = 0;
        materialUsage = 0;
        wearAndTear = 0;
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visitMachine(this);
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
                .appendHeader("Machine Details")
                .append("ID", this.getId())
                .append("Type", this.machineType)
                .append("Electricity Consumption", this.getElectricityConsumption() + " kWh")
                .append("Oil Consumption", this.getOilConsumption() + " liters")
                .append("Material Usage", this.getMaterialUsage() + " units")
                .append("Wear and Tear", this.getWearAndTear() + " %")
                .addSeparator()
                .build();
    }
}
