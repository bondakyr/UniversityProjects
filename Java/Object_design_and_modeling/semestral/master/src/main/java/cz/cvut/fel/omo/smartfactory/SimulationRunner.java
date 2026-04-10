package cz.cvut.fel.omo.smartfactory;

import cz.cvut.fel.omo.smartfactory.config.JsonConfiguration;
import cz.cvut.fel.omo.smartfactory.config.ProductConfig;
import cz.cvut.fel.omo.smartfactory.models.ReportItem;
import cz.cvut.fel.omo.smartfactory.patterns.decorator.LoggingDecorator;
import cz.cvut.fel.omo.smartfactory.patterns.factory.Factory;
import cz.cvut.fel.omo.smartfactory.patterns.factory.FactoryEntityFactory;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.visitor.ReportVisitor;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import cz.cvut.fel.omo.smartfactory.production.Repairer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SimulationRunner {

    public void runSimulation(JsonConfiguration jsonConfig, int defaultRepairTime, int defaultMaxTicks) {

        Factory factory = new Factory();

        for (ProductConfig product : jsonConfig.getProducts()) {
            setupProductionLineForProduct(factory, product);
        }

        for (int i = 0; i < jsonConfig.getNumOfRepairmen(); i++) {
            Repairer repairer = new Repairer(i + 1, defaultRepairTime);
            factory.addRepairer(repairer);
        }

        int tickCount = 0;
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("simulation_results.txt")))) {

            while (!allProductsDone(factory)) {
                tickCount++;
                System.out.println("\n=== Simulation tick " + tickCount + " ===\n");
                factory.update();

                if (tickCount % 10 == 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Tick ").append(tickCount).append(" completed.\n");
                    for (ProductionLine pl : factory.getProductionLines()) {
                        sb.append(pl.getName())
                                .append(" => Produced=")
                                .append(pl.getItemsProduced())
                                .append("\n");
                    }
                    String periodicInfo = sb.toString();
                    System.out.println(periodicInfo);
                    writer.println(periodicInfo);
                }

                if (tickCount >= defaultMaxTicks) {
                    String maxTicksInfo = "Too many ticks => break.";
                    System.out.println(maxTicksInfo);
                    writer.println(maxTicksInfo);
                    break;
                }
            }

            if (tickCount <= defaultMaxTicks) {
                String finalTickInfo = "Final tick " + tickCount + " completed.";
                System.out.println(finalTickInfo);
                writer.println(finalTickInfo);
            }

            System.out.println("\n=== PRODUCTION RESULTS ===");
            writer.println("\n=== PRODUCTION RESULTS ===");
            System.out.println("Simulation completed after " + tickCount + " ticks.");
            writer.println("Simulation completed after " + tickCount + " ticks.");
            for (ProductionLine pl : factory.getProductionLines()) {
                String result = pl.getName() + " => Produced=" + pl.getItemsProduced();
                System.out.println(result);
                writer.println(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n=== REPORT ===\n");
        ReportVisitor reportVisitor = new ReportVisitor();

        for (ProductionLine pl : factory.getProductionLines()) {
            for (FactoryEntity entity : pl.getEntities()) {
                entity.accept(reportVisitor);
            }
        }

        List<ReportItem> results = reportVisitor.getReportItems();
        System.out.println("\nVisitor collected " + results.size() + " items:");
        for (ReportItem item : results) {
            System.out.println(" - " + item.getEntityType() + " ID=" + item.getItemId()
                    + " status=" + item.getStatus());
        }
        System.out.println("Process finished with exit code 0");
    }

    private void setupProductionLineForProduct(Factory factory, ProductConfig productConfig) {
        ProductionLine line = new ProductionLine(100 + productConfig.getName().hashCode(), productConfig.getName());
        line.setItemsToProduce(productConfig.getAmountInSeries());

        for (String entityType : productConfig.getProductionLineConfig()) {
            FactoryEntity entity = FactoryEntityFactory.createFactoryEntity(entityType);

//            if (entityType.equalsIgnoreCase("machine")) {
//                entity = new LoggingDecorator(entity);
//            }

            line.addEntity(entity);
        }

        factory.addProductionLine(line);
    }

    private static boolean allProductsDone(Factory factory) {
        return factory.getProductionLines().stream()
                .allMatch(pl -> pl.getItemsToProduce() <= 0);
    }
}