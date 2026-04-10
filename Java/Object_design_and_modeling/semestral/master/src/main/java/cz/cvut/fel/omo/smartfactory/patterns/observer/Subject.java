package cz.cvut.fel.omo.smartfactory.patterns.observer;
/**
 * Subject interface for the Observer pattern.
 */
public interface Subject {
    /**
     * Připojí pozorovatele k subjektu.
     *
     * @param observer Pozorovatel, který se připojuje.
     */
    void attach(Observer observer);

    /**
     * Odpojí pozorovatele od subjektu.
     *
     * @param observer Pozorovatel, který se odpojuje.
     */
    void detach(Observer observer);

    /**
     * Notifikuje všechny připojené pozorovatele o události.
     *
     * @param event Událost, o které se informují pozorovatelé.
     */
    void notifyObservers(Event event);

    boolean repair();
}
