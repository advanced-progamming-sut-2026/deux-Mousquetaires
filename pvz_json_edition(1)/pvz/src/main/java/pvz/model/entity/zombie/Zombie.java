package pvz.model.entity.zombie;

import pvz.model.entity.Entity;
import pvz.model.entity.plant.Plant;

public abstract class Zombie extends Entity {

    ///field
    protected float speed;
    protected int   damage;
    protected int   armor;
    protected int   waveCost;
    protected int   lane;                     // 0–4
    /// status
    protected boolean isHypnotized;
    protected boolean isSlowed;
    protected float   slowMultiplier;
    protected int     slowTicksRemaining;
    protected boolean isFrozen;
    protected int     frozenTicksRemaining;
    protected boolean isPoisoned;
    protected int     poisonDmgPerTick;
    protected int     poisonTicksRemaining;
    protected Plant currentTarget;
    ///constructor
    protected Zombie(int x, int y, int hp, String assetId, float speed, int damage, int armor, int waveCost, int lane) {
        super(x, y, hp, assetId);
        this.speed           = speed;
        this.damage          = damage;
        this.armor           = armor;
        this.waveCost        = waveCost;
        this.lane            = lane;
        this.slowMultiplier  = 1.0f;
    }
    public abstract void specialAbility();
    @Override public void update() {
        if (!alive) return;
        tickStatusEffects();
        if (!isFrozen) move();
        if (currentTarget != null && currentTarget.isAlive()) attack(currentTarget);
    }
    @Override public void draw() {
        System.out.println("[" + assetId + "] (" + x + "," + y + ")" + " hp=" + hp + "/" + maxHp +
                (isFrozen    ? " [FROZEN]"              : "") +
                (isSlowed    ? " [SLOW×" + slowMultiplier + "]" : "") +
                (isPoisoned  ? " [POISON " + poisonDmgPerTick + "/t]" : "") +
                (isHypnotized? " [HYPNO]"               : ""));
    }
    public void move() { x -= (int) getEffectiveSpeed(); }
    public void attack(Plant p) { if (p != null && p.isAlive()) p.takeDamage(damage); }

    @Override public void takeDamage(int d) {
        if (!alive) return;
        int net = Math.max(0, d - armor);
        hp -= net;
        if (hp <= 0) { hp = 0; die(); }
    }

    @Override public void die() {
        onDeath();
        System.out.println("[" + assetId + "] died");
    }

    public void freeze(int ticks) {
        isFrozen = true;
        frozenTicksRemaining = ticks;
        isSlowed = false; slowMultiplier = 1.0f;
        System.out.println("[" + assetId + "] frozen " + ticks + "t");
    }
    public void thaw() {
        isFrozen = false; frozenTicksRemaining = 0;
        System.out.println("[" + assetId + "] thawed");
    }
    public void slow(float mult, int ticks) {
        if (isFrozen) return;
        isSlowed = true;
        slowMultiplier     = mult;
        slowTicksRemaining = Math.max(slowTicksRemaining, ticks);
        System.out.println("[" + assetId + "] slowed " + mult + "× " + ticks + "t");
    }
    public void poison(int dmgPerTick) {
        isPoisoned        = true;
        poisonDmgPerTick  = Math.max(poisonDmgPerTick, dmgPerTick);
        poisonTicksRemaining = 300;
        System.out.println("[" + assetId + "] poisoned " + dmgPerTick + "/t");
    }
    public void hypnotize() {
        isHypnotized = true;
        System.out.println("[" + assetId + "] hypnotized");
    }
    private void tickStatusEffects() {
        if (isFrozen  && --frozenTicksRemaining  <= 0) thaw();
        if (isSlowed && !isFrozen && --slowTicksRemaining <= 0) {isSlowed = false; slowMultiplier = 1.0f;}
        if (isPoisoned) {
            hp -= poisonDmgPerTick;              // poison bypasses armor
            if (hp <= 0) { hp = 0; die(); return; }
            if (--poisonTicksRemaining <= 0) { isPoisoned = false; poisonDmgPerTick = 0; }
        }
    }
    public float getEffectiveSpeed() {
        if (isFrozen) return 0f;
        return isSlowed ? speed * slowMultiplier : speed;
    }
    /// get and sets
    public float getSpeed()          { return speed; }
    public int   getDamage()         { return damage; }
    public int   getArmor()          { return armor; }
    public int   getWaveCo3st()       { return waveCost; }
    public int   getLane()           { return lane; }
    public void  setLane(int lane)   { this.lane = lane; }
    public boolean isHypnotized()    { return isHypnotized; }
    public boolean isSlowed()        { return isSlowed; }
    public float  getSlowMultiplier(){ return slowMultiplier; }
    public boolean isFrozen()        { return isFrozen; }
    public boolean isPoisoned()      { return isPoisoned; }
    public Plant  getCurrentTarget() { return currentTarget; }
    public void   setCurrentTarget(Plant p) { this.currentTarget = p; }
}
