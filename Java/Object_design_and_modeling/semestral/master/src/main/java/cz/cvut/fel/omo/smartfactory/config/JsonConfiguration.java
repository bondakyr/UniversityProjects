package cz.cvut.fel.omo.smartfactory.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JsonConfiguration {
    private int numOfProductionLines;
    private int numOfRepairmen;
    private String randomGeneratorSeed;
    private List<ProductConfig> products;

}
