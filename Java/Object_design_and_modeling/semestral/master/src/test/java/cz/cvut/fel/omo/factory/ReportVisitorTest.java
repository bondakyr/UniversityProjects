package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.patterns.visitor.ReportVisitor;
import cz.cvut.fel.omo.smartfactory.production.Machine;
import cz.cvut.fel.omo.smartfactory.models.MachineType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReportVisitor class.
 */
class ReportVisitorTest {

    private ReportVisitor visitor;
    private Machine machine;

    @BeforeEach
    void setUp() {
        visitor = new ReportVisitor();
        machine = new Machine(101, MachineType.CUTTER, 100);
    }

    @Test
    void testVisitMachineAddsToReport() {
        visitor.visitMachine(machine);
        assertEquals(1, visitor.getReportItems().size(), "Visitor should collect one item.");
        assertEquals("Machine", visitor.getReportItems().get(0).getEntityType(), "Entity type should be Machine.");
    }
}