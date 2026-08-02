package pvz.model.game;

import pvz.model.enums.PlantType;

public class SeedPacket {

    private final PlantType plantType;
    private int sunCost;
    private final int rechargeTicks;
    private int currentCooldown;
    private boolean selected;
    private boolean unlocked;

    public SeedPacket(PlantType plantType, int sunCost, int rechargeTicks) {
        this.plantType = plantType;
        this.sunCost = sunCost;
        this.rechargeTicks  = rechargeTicks;
        this.currentCooldown = 0;
        this.selected = false;
        this.unlocked = false;
    }

    public void update() { if (currentCooldown > 0) currentCooldown--; }
    public void startRecharge() { currentCooldown = rechargeTicks; }
    public boolean canPlace(int availableSun) {
        return unlocked && currentCooldown == 0 && availableSun >= sunCost;
    }
    public float getRechargeProgress() {
        if (rechargeTicks == 0) return 1.0f;
        return 1.0f - (float) currentCooldown / rechargeTicks;
    }
    public PlantType getPlantType()     { return plantType; }
    public int getSunCost()             { return sunCost; }
    public void setSunCost(int cost)    { this.sunCost = cost; }
    public int getRechargeTicks()       { return rechargeTicks; }
    public int getCurrentCooldown()     { return currentCooldown; }
    public boolean isSelected()         { return selected; }
    public void setSelected(boolean select)  { this.selected = select; }
    public boolean isUnlocked()         { return unlocked; }
    public void setUnlocked(boolean unlock)  { this.unlocked = unlock; }
}
