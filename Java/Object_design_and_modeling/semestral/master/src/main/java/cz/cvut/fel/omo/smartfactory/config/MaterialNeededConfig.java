package cz.cvut.fel.omo.smartfactory.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Material needed for a product, with cost.
 */
@Setter
@Getter
public class MaterialNeededConfig {
    private String name;
    private double cost;

}
