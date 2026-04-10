package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.state.ActiveState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ActiveState class.
 */
class ActiveStateTest {

    private FactoryEntity entity;
    private ActiveState activeState;

    @BeforeEach
    void setUp() {
        entity = new FactoryEntity(1) {
            @Override
            public void accept(cz.cvut.fel.omo.smartfactory.patterns.visitor.Visitor visitor) {
                // No-op for tests
            }
        };
        activeState = new ActiveState(entity);
    }

    @Test
    void testHandleSetsActiveStatus() {
        activeState.handle();
        assertEquals(Status.ACTIVE, entity.getStatus(), "Status should be ACTIVE after handling.");
    }

    @Test
    void testPerformTaskDoesNotChangeStatus() {
        activeState.performTask();
        assertEquals(Status.ACTIVE, entity.getStatus(), "Status should remain ACTIVE after performing a task.");
    }
}