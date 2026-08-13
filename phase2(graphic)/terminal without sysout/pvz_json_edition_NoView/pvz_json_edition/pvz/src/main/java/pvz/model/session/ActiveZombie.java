package pvz.model.session;

import pvz.model.entity.zombie.CatalogZombie;

public class ActiveZombie {

    final CatalogZombie zombie;
    double col;
    int row;
    final int spawnedWave;
    final long spawnTick;
    int freezeTicks;
    int slowTicks;
    double slowMult = 1.0;
    boolean glowing;        /// glowing zombies drop plant food on death
    int abilityTimer;       /// generic per type ability cooldown
    boolean abilityUsed;
    boolean movingRight;
    int sunStolen;
    int laneSwitchCooldown;
    boolean torchLit = true;
    boolean allStarBitten;
    boolean crushedPlant;
    int lastCellSeen;

    public ActiveZombie(CatalogZombie zombie, int row, int spawnedWave, long spawnTick) {
        this.zombie = zombie;
        this.col = zombie.getX();
        this.row = row;
        this.spawnedWave = spawnedWave;
        this.spawnTick = spawnTick;
        this.lastCellSeen = cellCol();
    }
    public int cellCol() {
        return Math.max(1, Math.min(9, (int) Math.ceil(col)));
    }
    public boolean isFrozen() {
        return freezeTicks > 0;
    }
    public void freeze(int ticks) {
        if (!zombie.isFreezeImmune()) freezeTicks = Math.max(freezeTicks, ticks);
    }

    public void slow(double mult, int ticks) {
        if (!zombie.isFreezeImmune()) {
            slowMult = mult;
            slowTicks = Math.max(slowTicks, ticks);
        }
    }

    public void thaw() {
        freezeTicks = 0;
        slowTicks = 0;
        slowMult = 1.0;
    }

    public CatalogZombie getZombie() { return zombie; }
    public double getCol() { return col; }
    public int getRow() { return row; }
    public boolean isGlowing() { return glowing; }
    public int getFreezeTicks() { return freezeTicks; }
    public int getSlowTicks() { return slowTicks; }
}
