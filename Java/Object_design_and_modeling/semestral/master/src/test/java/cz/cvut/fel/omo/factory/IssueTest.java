package cz.cvut.fel.omo.factory;

import cz.cvut.fel.omo.smartfactory.models.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Issue class.
 */
class IssueTest {

    @Test
    void testIssueInitialization() {
        String description = "Test issue";
        Issue issue = new Issue(description);

        assertNotNull(issue.getTimestamp(), "Timestamp should be initialized.");
        assertEquals(description, issue.getDescription(), "Description should match.");
    }

    @Test
    void testToString() {
        String description = "Another issue";
        Issue issue = new Issue(description);

        String issueString = issue.toString();
        assertTrue(issueString.contains("description='Another issue'"), "toString should include description.");
        assertTrue(issueString.contains("issueCode=-1"), "toString should include issue code.");
    }
}