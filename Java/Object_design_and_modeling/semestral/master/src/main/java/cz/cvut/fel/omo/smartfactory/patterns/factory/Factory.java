package cz.cvut.fel.omo.smartfactory.patterns.factory;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.production.ProductionLine;
import cz.cvut.fel.omo.smartfactory.production.Repairer;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Factory {
    private List<ProductionLine> productionLines;
    private List<Repairer> repairers;
    private Set<FactoryEntity> beingRepairedEntities;

    public Factory() {
        this.productionLines = new ArrayList<>();
        this.repairers = new ArrayList<>();
        this.beingRepairedEntities = new HashSet<>();
    }

    public void addProductionLine(ProductionLine productionLine) {
        if (productionLine != null && !productionLines.contains(productionLine)) {
            productionLines.add(productionLine);
        }
    }

    public List<ProductionLine> getProductionLines() {
        return Collections.unmodifiableList(productionLines);
    }

    public void addRepairer(Repairer repairer) {
        if (repairer != null && !repairers.contains(repairer)) {
            repairers.add(repairer);
        }
    }

    /**
     * Hlavní metoda pro aktualizaci továrny (simulační tick).
     */
    public void update() {
        for (ProductionLine line : productionLines) {
            for (FactoryEntity entity : line.getEntities()) {
                entity.performTask();
                System.out.println(entity);
            }
        }

        updateRepairers();

        for (ProductionLine pl : productionLines) {
            pl.doProductionTick();
        }
    }

    /**
     * Aktualizuje opraváře a řeší rozbité entity.
     */
    private void updateRepairers() {
        for (Repairer repairer : repairers) {
            if (!repairer.isAvailable()) {
                repairer.performTask();
                if (repairer.isAvailable()) {
                    FactoryEntity repairedEntity = repairer.getLastRepairedEntity();
                    if (repairedEntity != null) {
                        boolean removed = beingRepairedEntities.remove(repairedEntity);
                        if (removed) {
                            System.out.println("Repairer ID " + repairer.getId() + " dokončil opravu Entity ID " + repairedEntity.getId());
                        }
                    }
                }

            } else {
                FactoryEntity brokenEntity = findBrokenEntity();
                if (brokenEntity != null) {
                    repairer.repairEntity(brokenEntity);
                    beingRepairedEntities.add(brokenEntity);
                    System.out.println("Repairer ID " + repairer.getId() + " assigned to repair Entity ID " + brokenEntity.getId());
                }
            }
        }
    }

    /**
     * Najde první rozbitou entitu k opravě.
     *
     * @return Rozbitá entita, nebo null, pokud žádná neexistuje.
     */
    private FactoryEntity findBrokenEntity() {
        for (ProductionLine pl : productionLines) {
            for (FactoryEntity entity : pl.getEntities()) {
                if (entity.getStatus() == Status.BROKEN && !beingRepairedEntities.contains(entity)) {
                    return entity;
                }
            }
        }
        return null;
    }
}