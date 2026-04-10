package cz.cvut.fel.omo.smartfactory.models;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an issue in the factory system.
 */
public class Issue {
    private final int issueCode;
    @Setter
    @Getter
    private String description;
    @Setter
    @Getter
    private Instant timestamp;

    /**
     * Konstruktor pro Issue s popisem.
     *
     * @param description Popis chyby.
     */
    public Issue(String description) {
        this.issueCode = -1;
        this.description = description;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return "Issue{" +
                "issueCode=" + issueCode +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
