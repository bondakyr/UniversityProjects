package cz.cvut.fel.omo.smartfactory.production;

import cz.cvut.fel.omo.smartfactory.models.Status;
import cz.cvut.fel.omo.smartfactory.patterns.observer.FactoryEntity;
import cz.cvut.fel.omo.smartfactory.patterns.state.ActiveState;
import lombok.Getter;

public class Repairer {
    @Getter
    private int id;
    @Getter
    private FactoryEntity currentRepairingEntity;
    @Getter
    private FactoryEntity lastRepairedEntity;
    private int repairProgress;
    private final int repairTime;
    @Getter
    private boolean available;

    public Repairer(int id, int repairTime) {
        this.id = id;
        this.repairTime = repairTime;
        this.available = true;
        this.repairProgress = 0;
        this.currentRepairingEntity = null;
        this.lastRepairedEntity = null;
    }

    /**
     * Přiřadí entitu k opravě, pokud je opravář dostupný.
     *
     * @param entity Entita, kterou je třeba opravit.
     */
    public void repairEntity(FactoryEntity entity) {
        if (available && entity != null && entity.getStatus() == Status.BROKEN) {
            this.currentRepairingEntity = entity;
            this.repairProgress = 0;
            this.available = false;
            System.out.println("Repairer ID " + id + " začal opravovat " + entity.getClass().getSimpleName() + " ID " + entity.getId());
        }
    }

    /**
     * Pokračuje v opravě aktuálně přiřazené entity.
     */
    public void performTask() {
        if (currentRepairingEntity != null) {
            repairProgress++;
            System.out.println("Repairer ID " + id + " opravuje " + currentRepairingEntity.getClass().getSimpleName() + " ID " + currentRepairingEntity.getId() +
                    " (Progress: " + repairProgress + "/" + repairTime + ")");

            if (repairProgress >= repairTime) {
                System.out.println("Repairer ID " + id + " dokončil opravu " + currentRepairingEntity.getClass().getSimpleName() + " ID " + currentRepairingEntity.getId());
                boolean repaired = currentRepairingEntity.repair();
                if (repaired) {
                    currentRepairingEntity.setState(new ActiveState(currentRepairingEntity));
                    System.out.println("Entity ID " + currentRepairingEntity.getId() + " byla opravena a je nyní ACTIVE.");
                    lastRepairedEntity = currentRepairingEntity;
                }
                currentRepairingEntity = null;
                available = true;
            }
        }
    }
}
