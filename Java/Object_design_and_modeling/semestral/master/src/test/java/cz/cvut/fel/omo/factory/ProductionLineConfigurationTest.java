package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProductionLine class with specific configurations.
 */
class ProductionLineConfigurationTest {

    private ProductionLine line;

    @BeforeEach
    void setUp() {
        line = new ProductionLine(10, "ConfigurableLine");
    }

    @Test
    void testAddMultipleEntities() {
        line.addEntity(new Machine(201, cz.cvut.fel.omo.smartfactory.models.MachineType.PRESS, 200));
        line.addEntity(new Machine(202, cz.cvut.fel.omo.smartfactory.models.MachineType.CUTTER, 150));

        assertEquals(2, line.getEntities().size(), "Two entities should be added to the line.");
    }
}