package pvz.model.entity.plant;

import pvz.model.entity.Entity;
import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantTag;
import pvz.model.game.SeedPacket;

import java.util.EnumSet;

public abstract class Plant extends Entity {

    ///field
    protected int             sunCost;
    protected int             seedRechargeTime;  // ticks
    protected float           range;
    protected int             level;             // 1–5
    protected int             plantFoodStock;    // 0–3
    protected EnumSet<PlantTag> tags;
    protected PlantFamily     family;
    protected boolean         isBoosted;
    protected int             boostTicksRemaining;
    protected SeedPacket      seedPacket;

    ///constructor
    protected Plant(int x, int y, int hp, String assetId, int sunCost, int seedRechargeTime, float range,
                    PlantFamily family, EnumSet<PlantTag> tags) {
        super(x, y, hp, assetId);
        this.sunCost           = sunCost;
        this.seedRechargeTime  = seedRechargeTime;
        this.range             = range;
        this.family            = family;
        this.tags              = tags;
        this.level             = 1;
        this.plantFoodStock    = 0;
        this.isBoosted         = false;
        this.boostTicksRemaining = 0;
        this.seedPacket        = new SeedPacket(null, sunCost, seedRechargeTime);
    }
    public abstract void attack();
    public abstract void produce();
    public abstract void onPlantFood();

    @Override
    public void takeDamage(int d) {
        if (!alive) return;
        hp -= d;
        if (hp <= 0) { hp = 0; die(); }
    }
    @Override
    public void die() { onDeath(); }
    public void upgrade() { if (level < 5) level++; }
    public void boost(int duration) {
        isBoosted = true;
        boostTicksRemaining = Math.max(boostTicksRemaining, duration);
    }
    protected void tickBoost() {
        if (!isBoosted) return;
        if (--boostTicksRemaining <= 0) {
            isBoosted = false;
            boostTicksRemaining = 0;
        }
    }
    public boolean usePlantFood() {
        if (plantFoodStock > 0) { plantFoodStock--; onPlantFood(); return true; }
        return false;
    }
    public boolean hasTag(PlantTag t) { return tags.contains(t); }

    /// get and set
    public int getSunCost() { return sunCost; }
    public int getSeedRechargeTime() { return seedRechargeTime; }
    public float getRange() { return range; }
    public int getLevel() { return level; }
    public int getPlantFoodStock() { return plantFoodStock; }
    public void addPlantFood() { if (plantFoodStock < 3) plantFoodStock++; }
    public PlantFamily getFamily() { return family; }
    public boolean isBoosted() { return isBoosted; }
    public SeedPacket getSeedPacket() { return seedPacket; }
    public EnumSet<PlantTag> getTags() { return tags.clone(); }
}
