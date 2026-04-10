package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.patterns.iterator.Iterator;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProductionLineIterator class.
 */
class ProductionLineIteratorTest {

    private ProductionLine productionLine;

    @BeforeEach
    void setUp() {
        productionLine = new ProductionLine(1, "TestLine");
        productionLine.addEntity(new Machine(101, cz.cvut.fel.omo.smartfactory.models.MachineType.CUTTER, 100));
        productionLine.addEntity(new Machine(102, cz.cvut.fel.omo.smartfactory.models.MachineType.PRESS, 100));
    }

    @Test
    void testHasNext() {
        Iterator iterator = productionLine.createIterator();
        assertTrue(iterator.hasNext(), "Iterator should have elements initially.");

        iterator.next();
        iterator.next();
        assertFalse(iterator.hasNext(), "Iterator should not have elements after iterating through all.");
    }

    @Test
    void testNext() {
        Iterator iterator = productionLine.createIterator();
        assertNotNull(iterator.next(), "Next element should not be null.");
        assertNotNull(iterator.next(), "Next element should not be null.");
    }

    @Test
    void testRemove() {
        Iterator iterator = productionLine.createIterator();
        iterator.next();
        iterator.remove();

        assertEquals(1, productionLine.getEntities().size(), "One entity should remain after removal.");
    }
}
