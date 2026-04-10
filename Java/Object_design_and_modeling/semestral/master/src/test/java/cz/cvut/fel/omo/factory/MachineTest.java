package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Issue;
import cz.cvut.fel.omo.smartfactory.models.MachineType;
import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.observer.Observer;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Machine class.
 */
class MachineTest {

    private Machine machine;

    @BeforeEach
    void setUp() {
        machine = new Machine(20, MachineType.CUTTER, 200);
    }

    @Test
    void testPerformTaskIncreasesConsumption() {
        assertEquals(0, machine.getElectricityConsumption());
        assertEquals(0, machine.getOilConsumption());
        assertEquals(0, machine.getMaterialUsage());
        assertEquals(0, machine.getWearAndTear());

        machine.performTask();

        assertEquals(8, machine.getElectricityConsumption(), 1e-9);
        assertEquals(2, machine.getOilConsumption(), 1e-9);
        assertEquals(5, machine.getMaterialUsage(), 1e-9);
        assertEquals(0.05, machine.getWearAndTear(), 1e-9);
    }

    @Test
    void testPerformTaskMayBreakMachine() {
        machine.performTask();
        assertTrue(machine.getStatus() == Status.ACTIVE || machine.getStatus() == Status.BROKEN,
                "Status should be either ACTIVE or BROKEN after performTask.");
    }

    @Test
    void testRepair() {
        machine.setStatus(Status.BROKEN);
        assertEquals(Status.BROKEN, machine.getStatus());

        boolean repaired = machine.repair();
        assertTrue(repaired, "Machine should be repaired successfully.");
        assertEquals(Status.ACTIVE, machine.getStatus(),
                "After repair, status should be ACTIVE.");

        assertEquals(0, machine.getElectricityConsumption());
        assertEquals(0, machine.getOilConsumption());
        assertEquals(0, machine.getMaterialUsage());
        assertEquals(0, machine.getWearAndTear());
    }
}
