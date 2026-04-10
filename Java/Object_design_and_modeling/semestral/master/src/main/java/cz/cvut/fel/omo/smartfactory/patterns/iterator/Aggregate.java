package cz.cvut.fel.omo.smartfactory.patterns.iterator;

/**
 * Aggregate interface for creating iterators.
 *
 * @param <T> Type of elements in the collection.
 */
public interface Aggregate<T> {
    /**
     * Creates an iterator for the collection.
     *
     * @return Iterator instance.
     */
    Iterator<T> createIterator();
}
