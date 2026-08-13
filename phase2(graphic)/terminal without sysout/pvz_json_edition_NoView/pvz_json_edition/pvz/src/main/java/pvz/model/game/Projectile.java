package pvz.model.game;

public class Projectile {

    public enum ProjectileType {
        NORMAL_PEA, ICE_PEA, FIRE_PEA, SPIKE, BOMB
    }

    ///informations
    private int x;
    private int y;
    private int lane;
    private int damage;
    private float speed;
    private ProjectileType type;
    private boolean active;

    ///homing
    private boolean homing;
    private Object target;
    ///ice effection
    private int   slowDuration;
    private float slowFactor;
    ///constructor
    public Projectile(int x, int y, int lane, int damage, float speed, ProjectileType type) {
        this.x = x;
        this.y = y;
        this.lane = lane;
        this.damage = damage;
        this.speed = speed;
        this.type = type;
        this.active = true;
    }
    public void update() {
        if (!active) return;
        if (!homing) x += (int) speed;
    }
    public void ignite(int bonusDamage) {
        this.type   = ProjectileType.FIRE_PEA;
        this.damage += bonusDamage;
    }
    public void deactivate() { this.active = false; }

    ///get and setters
    public int getX()               { return x; }
    public void setX(int x)         { this.x = x; }
    public int getY()               { return y; }
    public void setY(int y)         { this.y = y; }
    public int getLane()            { return lane; }
    public int getDamage()          { return damage; }
    public void setDamage(int d)    { this.damage = d; }
    public float getSpeed()         { return speed; }
    public ProjectileType getType() { return type; }
    public void setType(ProjectileType t) { this.type = t; }
    public boolean isActive()       { return active; }
    public boolean isHoming()       { return homing; }
    public void setHoming(boolean h){ this.homing = h; }
    public Object getTarget()       { return target; }
    public void setTarget(Object t) { this.target = t; }
    public int getSlowDuration()    { return slowDuration; }
    public void setSlowDuration(int d) { this.slowDuration = d; }
    public float getSlowFactor()    { return slowFactor; }
    public void setSlowFactor(float f) { this.slowFactor = f; }
}
