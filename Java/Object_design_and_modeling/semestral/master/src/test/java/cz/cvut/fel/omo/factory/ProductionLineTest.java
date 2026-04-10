package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ProductionLine class.
 */
class ProductionLineTest {

    private ProductionLine line;
    private FactoryEntity dummyEntity;

    @BeforeEach
    void setUp() {
        line = new ProductionLine(1, "TestLine");
        dummyEntity = new FactoryEntity(999) {
            @Override
            public void accept(cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor visitor) {
                // dummy
            }
        };
    }

    @Test
    void testAddEntity() {
        assertTrue(line.getEntities().isEmpty(),
                "Initially, no entities should be in the line.");

        line.addEntity(dummyEntity);
        assertEquals(1, line.getEntities().size(),
                "After adding, we expect 1 entity in the line.");
        assertEquals(dummyEntity, line.getEntities().get(0),
                "The entity added should match our dummyEntity.");
    }

    @Test
    void testDoProductionTickNoBroken() {
        line.setItemsToProduce(2);
        line.setTimePerItem(2);

        line.addEntity(dummyEntity);

        line.doProductionTick();
        assertEquals(1,
                getPrivateField(line, "progress"),
                "Progress should be 1 after first tick");
        assertEquals(0, line.getItemsProduced());

        line.doProductionTick();
        assertEquals(0,
                getPrivateField(line, "progress"),
                "Progress should reset to 0 after finishing item");
        assertEquals(1, line.getItemsProduced());

        assertEquals(1, line.getItemsToProduce());
    }

    @Test
    void testDoProductionTickBrokenEntity() {
        line.setItemsToProduce(1);
        line.setTimePerItem(1);

        dummyEntity.setStatus(Status.BROKEN);
        line.addEntity(dummyEntity);

        line.doProductionTick();
        assertEquals(0,
                getPrivateField(line, "progress"),
                "Progress must remain 0 if any entity is BROKEN.");
        assertEquals(0, line.getItemsProduced(),
                "No items should be produced if an entity is broken.");
    }

    /**
     * Helper method to access private fields via reflection if needed
     */
    private int getPrivateField(ProductionLine pl, String fieldName) {
        try {
            var field = ProductionLine.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(pl);
        } catch (Exception e) {
            fail("Reflection access failed: " + e.getMessage());
            return -1;
        }
    }
}
