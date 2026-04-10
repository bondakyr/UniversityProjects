package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.state.ActiveState;
import cz.cvut.fel.omo.smartfactory.production.Repairer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Repairer class.
 */
class RepairerTest {

    private Repairer repairer;
    private FactoryEntity brokenEntity;

    @BeforeEach
    void setUp() {
        repairer = new Repairer(1, 3);
        brokenEntity = new FactoryEntity(1000) {
            @Override
            public void accept(cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor visitor) {
                // no op
            }
        };
        brokenEntity.setStatus(Status.BROKEN);
    }

    @Test
    void testRepairEntityAssignment() {
        assertTrue(repairer.isAvailable());
        repairer.repairEntity(brokenEntity);
        assertFalse(repairer.isAvailable());
        assertEquals(brokenEntity, repairer.getCurrentRepairingEntity());
    }

    @Test
    void testCannotRepairNonBrokenEntity() {
        FactoryEntity activeEntity = new FactoryEntity(2000) {
            @Override
            public void accept(cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor visitor) {
                // no op
            }
        };
        activeEntity.setStatus(Status.ACTIVE);
        repairer.repairEntity(activeEntity);
        assertNull(repairer.getCurrentRepairingEntity());
        assertTrue(repairer.isAvailable());
    }

    /**
     * Helper method to access private fields via reflection if needed
     */
    private int getPrivateField(Repairer r, String fieldName) {
        try {
            java.lang.reflect.Field field = Repairer.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(r);
        } catch (Exception e) {
            fail("Reflection access failed: " + e.getMessage());
            return -1;
        }
    }
}
