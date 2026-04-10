package cz.cvut.fel.omo.smartfactory.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single item in a report.
 */
@Getter
@Setter
@AllArgsConstructor
public class ReportItem {
    private int itemId;
    private String entityType;
    private String status;
}
