package cz.cvut.fel.omo.smartfactory.patterns.iterator;

import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;

import java.util.List;

/**
 * Concrete Iterator for ProductionLine entities.
 */
public class ProductionLineIterator implements Iterator<FactoryEntity> {

    private final List<FactoryEntity> entities;
    private int position = 0;

    /**
     * Constructor for ProductionLineIterator.
     *
     * @param entities List of FactoryEntity to iterate over.
     */
    public ProductionLineIterator(List<FactoryEntity> entities) {
        this.entities = entities;
    }

    @Override
    public boolean hasNext() {
        return position < entities.size();
    }

    @Override
    public FactoryEntity next() {
        if (!hasNext()) {
            throw new IndexOutOfBoundsException("No more elements to iterate.");
        }
        return entities.get(position++);
    }

    @Override
    public void remove() {
        if (position <= 0) {
            throw new IllegalStateException("Cannot remove element before next() is called.");
        }
        entities.remove(--position);
    }
}
