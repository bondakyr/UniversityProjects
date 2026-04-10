package cz.cvut.fel.omo.factory;
import cz.cvut.fel.omo.smartfactory.patterns.builder.StringBuilderPattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StringBuilderPattern class.
 */
class StringBuilderPatternTest {

    @Test
    void testAppend() {
        StringBuilderPattern builder = new StringBuilderPattern();
        builder.append("Key", "Value");
        String result = builder.build();

        assertTrue(result.contains("Key: Value"), "Appended key-value pair should be in the result.");
    }

    @Test
    void testAddSeparator() {
        StringBuilderPattern builder = new StringBuilderPattern();
        builder.addSeparator();
        String result = builder.build();

        assertTrue(result.contains("------------"), "Separator should be present in the result.");
    }

    @Test
    void testAppendHeader() {
        StringBuilderPattern builder = new StringBuilderPattern();
        builder.appendHeader("Header");
        String result = builder.build();

        assertTrue(result.contains("Header\n============"), "Header should be formatted correctly in the result.");
    }

    @Test
    void testBuild() {
        StringBuilderPattern builder = new StringBuilderPattern();
        builder.append("TestKey", "TestValue");
        builder.addSeparator();

        String result = builder.build();
        assertNotNull(result, "Built string should not be null.");
        assertFalse(result.isEmpty(), "Built string should not be empty.");
    }
}
