package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.patterns.factory.Factory;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import cz.cvut.fel.omo.smartfactory.production.Repairer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Factory class.
 */
class FactoryTest {

    private Factory factory;

    @BeforeEach
    void setUp() {
        factory = new Factory();
    }

    @Test
    void testAddProductionLine() {
        ProductionLine line = new ProductionLine(1, "Line1");
        factory.addProductionLine(line);

        assertEquals(1, factory.getProductionLines().size());
        assertEquals(line, factory.getProductionLines().get(0));
    }

    @Test
    void testAddRepairer() {
        Repairer r = new Repairer(1, 5);
        factory.addRepairer(r);
        assertEquals(1, factory.getRepairers().size());
        assertEquals(r, factory.getRepairers().get(0));
    }
}
