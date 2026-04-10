package cz.cvut.fel.omo.factory;
import cz.cvut.fel.omo.smartfactory.patterns.observer.Event;
import cz.cvut.fel.omo.smartfactory.patterns.singleton.EventPool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for EventPool class.
 */
class EventPoolAdditionalTest {

    @Test
    void testMaxSizeExceeded() {
        EventPool pool = new EventPool(1);

        Event event1 = pool.borrowEvent();
        assertNotNull(event1, "Borrowed event should not be null.");

        Exception exception = assertThrows(RuntimeException.class, pool::borrowEvent);
        assertEquals("Maximum Event pool size reached, no available Events.", exception.getMessage(), "Exception message should match.");
    }

    @Test
    void testReturnEventAfterMaxSize() {
        EventPool pool = new EventPool(1);

        Event event1 = pool.borrowEvent();
        pool.returnEvent(event1);

        Event event2 = pool.borrowEvent();
        assertSame(event1, event2, "Returned event should be reused.");
    }
}