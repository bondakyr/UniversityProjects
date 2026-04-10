package cz.cvut.fel.omo.smartfactory.patterns.iterator;


/**
 * Iterator interface for iterating over a collection.
 *
 * @param <T> Type of elements to iterate over.
 */
public interface Iterator<T> {
    /**
     * Checks if there are more elements to iterate over.
     *
     * @return True if there are more elements, else False.
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration.
     *
     * @return The next element.
     */
    T next();

    /**
     * Removes the last element returned by this iterator.
     * Optional operation.
     */
    void remove();
}
