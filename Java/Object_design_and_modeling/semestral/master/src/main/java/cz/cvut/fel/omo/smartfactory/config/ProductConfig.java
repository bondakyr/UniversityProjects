package cz.cvut.fel.omo.smartfactory.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Product configuration
 */
@Setter
@Getter
public class ProductConfig {
    private String name;
    private int amountInSeries;
    private List<String> productionLineConfig;
    private List<MaterialNeededConfig> materialsNeeded;

}
