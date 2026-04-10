package cz.cvut.fel.omo.smartfactory.production;

import cz.cvut.fel.omo.smartfactory.patterns.iterator.Aggregate;
import cz.cvut.fel.omo.smartfactory.patterns.iterator.Iterator;
import cz.cvut.fel.omo.smartfactory.patterns.iterator.ProductionLineIterator;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.models.Status;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductionLine implements Aggregate<FactoryEntity> {

    @Getter
    private int id;
    @Getter
    private String name;
    private final List<FactoryEntity> entities;

    @Setter
    @Getter
    private int itemsToProduce;
    @Getter
    private int itemsProduced;
    @Setter
    private int timePerItem;
    private int progress;

    public ProductionLine(int id, String name) {
        this.id = id;
        this.name = name;
        this.entities = new ArrayList<>();

        this.itemsToProduce = 0;
        this.itemsProduced = 0;
        this.timePerItem = 1;
        this.progress = 0;
    }

    public void addEntity(FactoryEntity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public List<FactoryEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public Iterator<FactoryEntity> createIterator() {
        return new ProductionLineIterator(this.entities);
    }

    public void doProductionTick() {
        if (itemsToProduce <= 0) {
            return;
        }

        if (!areAllEntitiesActive()) {
            System.out.println(name + " is halted because at least one entity is BROKEN.");
            return;
        }

        progress++;

        if (progress >= timePerItem) {
            itemsProduced++;
            itemsToProduce--;
            progress = 0;
        }
    }

    /**
     * Zkontrolujeme, jestli jsou všechny entity (Robot, Machine, Operator)
     * ve statusu ACTIVE. Pokud kdokoliv je BROKEN (nebo REPAIRING) → linka stojí.
     */
    private boolean areAllEntitiesActive() {
        for (FactoryEntity e : entities) {
            if (e.getStatus() != Status.ACTIVE) {
                return false;
            }
        }
        return true;
    }
}
