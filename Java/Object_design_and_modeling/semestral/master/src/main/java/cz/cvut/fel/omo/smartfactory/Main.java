package cz.cvut.fel.omo.smartfactory;

import cz.cvut.fel.omo.smartfactory.config.JsonConfiguration;
import java.io.IOException;

/**
 * Main class for running the simulation.
 */
public class Main {
    public static void main(String[] args) {

//        String filePath = "factory_config.json";
        String filePath = "factory_config_2.json";
        final int DEFAULT_REPAIR_TIME = 5;
        final int DEFAULT_MAX_TICKS = 20000;

        JsonConfiguration jsonConfig;
        try {
            jsonConfig = JsonUtils.loadJsonConfiguration(filePath);
        } catch (IOException e) {
            System.err.println("Failed to load JSON configuration: " + e.getMessage());
            return;
        }

        SimulationRunner runner = new SimulationRunner();
        runner.runSimulation(jsonConfig, DEFAULT_REPAIR_TIME, DEFAULT_MAX_TICKS);
    }
}