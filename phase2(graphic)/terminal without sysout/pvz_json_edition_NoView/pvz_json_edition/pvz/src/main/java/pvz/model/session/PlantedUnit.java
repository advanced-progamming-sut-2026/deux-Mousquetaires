package pvz.model.session;

import pvz.model.entity.plant.CatalogPlant;
import pvz.model.enums.PlantType;

public class PlantedUnit {

    final CatalogPlant plant;
    final int col;          /// 1--->9
    final int row;          /// 1--->5
    final boolean protectedPlant;

    int actionTimer;
    int ageTicks;
    boolean armed;          /// potato mine
    int snowballHits;       // Hunter ---> 3 hits ---> frozen
    boolean frozen;
    boolean turnedIntoCat;
    boolean onLilyPad;
    double damageBonus = 1.0;

    public PlantedUnit(CatalogPlant plant, int column, int row, boolean protectedPlant) {
        this.plant = plant;
        this.col = column;
        this.row = row;
        this.protectedPlant = protectedPlant;
        this.actionTimer = plant.getActionIntervalTicks();
    }
    public PlantType getType() {
        return plant.getType();
    }
    public boolean isDisabled() {
        return frozen || turnedIntoCat;
    }
    public CatalogPlant getPlant() { return plant; }
    public int getCol() { return col; }
    public int getRow() { return row; }
    public boolean isProtectedPlant() { return protectedPlant; }
    public boolean isFrozen() { return frozen; }
}
