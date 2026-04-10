package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.models.TaskType;
import cz.cvut.fel.omo.smartfactory.patterns.pool.TaskStrategyPool;
import cz.cvut.fel.omo.smartfactory.production.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Robot class.
 */
class RobotTest {

    private Robot robot;

    @BeforeEach
    void setUp() {
        robot = new Robot(10, TaskType.WELDING, 0.99, 1.0, new TaskStrategyPool());
    }

    @Test
    void testPerformTaskIncreasesConsumption() {
        assertEquals(0, robot.getElectricityConsumption());
        assertEquals(0, robot.getOilConsumption());
        assertEquals(0, robot.getMaterialUsage());
        assertEquals(0, robot.getWearAndTear());

        robot.performTask();

        assertEquals(5, robot.getElectricityConsumption(), 1e-9);
        assertEquals(1, robot.getOilConsumption(), 1e-9);
        assertEquals(2, robot.getMaterialUsage(), 1e-9);
        assertEquals(0.1, robot.getWearAndTear(), 1e-9);
    }

    @Test
    void testBreakAndRepair() {
        robot.setStatus(Status.BROKEN);
        assertEquals(Status.BROKEN, robot.getStatus());

        boolean repaired = robot.repair();
        assertTrue(repaired, "Robot should be repaired successfully");
        assertEquals(Status.ACTIVE, robot.getStatus(),
                "After repair, status should be ACTIVE.");
    }
}
