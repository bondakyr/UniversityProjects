package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.factory.FactoryEntityFactory;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.production.Operator;
import cz.cvut.fel.omo.smartfactory.production.Robot;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FactoryEntityFactory class.
 */
class FactoryEntityFactoryTest {

    @Test
    void testCreateRobot() {
        FactoryEntity robot = FactoryEntityFactory.createFactoryEntity("robot");
        assertNotNull(robot);
        assertTrue(robot instanceof Robot, "Created entity should be an instance of Robot.");
        assertEquals(Status.ACTIVE, robot.getStatus(), "Robot should initially be ACTIVE.");
    }

    @Test
    void testCreateMachine() {
        FactoryEntity machine = FactoryEntityFactory.createFactoryEntity("machine");
        assertNotNull(machine);
        assertTrue(machine instanceof Machine, "Created entity should be an instance of Machine.");
        assertEquals(Status.ACTIVE, machine.getStatus(), "Machine should initially be ACTIVE.");
    }

    @Test
    void testCreateOperator() {
        FactoryEntity operator = FactoryEntityFactory.createFactoryEntity("human");
        assertNotNull(operator);
        assertTrue(operator instanceof Operator, "Created entity should be an instance of Operator.");
        assertEquals(Status.ACTIVE, operator.getStatus(), "Operator should initially be ACTIVE.");
    }

    @Test
    void testCreateUnknownEntityThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            FactoryEntityFactory.createFactoryEntity("unknown");
        });

        String expectedMessage = "Unknown entity type";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}
