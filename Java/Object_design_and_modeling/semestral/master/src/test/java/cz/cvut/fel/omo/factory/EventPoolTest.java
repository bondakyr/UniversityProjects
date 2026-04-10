package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.patterns.observer.Event;
import cz.cvut.fel.omo.smartfactory.patterns.singleton.EventPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.Instant;

/**
 * Tests for EventPool singleton class.
 */
class EventPoolTest {

    @BeforeEach
    void setUp() throws Exception {
        Field instanceField = EventPool.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        EventPool pool = new EventPool(2);
        instanceField.set(null, pool);
    }

    @Test
    void testSingletonInstance() {
        EventPool firstInstance = EventPool.getInstance();
        EventPool secondInstance = EventPool.getInstance();
        assertSame(firstInstance, secondInstance, "Both instances should be the same (singleton).");
    }

    @Test
    void testBorrowAndReturnEvent() {
        EventPool pool = EventPool.getInstance();

        Event event1 = pool.borrowEvent();
        assertNotNull(event1, "Borrowed event should not be null.");

        Event event2 = pool.borrowEvent();
        assertNotNull(event2, "Borrowed event should not be null.");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            pool.borrowEvent();
        });
        String expectedMessage = "Maximum Event pool size reached";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
        pool.returnEvent(event1);

        Event event3 = pool.borrowEvent();
        assertNotNull(event3, "Borrowed event should not be null.");
        assertEquals(event1, event3, "Returned event should be the same as borrowed again.");

        pool.returnEvent(event2);
        pool.returnEvent(event3);
    }


}
