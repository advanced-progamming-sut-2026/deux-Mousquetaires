package pvz.model.entity;

public abstract class Entity {

    ///fields
    protected int x;
    protected int y;
    protected int hp;
    protected int maxHp;
    protected boolean alive;
    protected String assetId;

    ///constructor
    protected Entity(int x, int y, int hp, String assetId) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        this.alive = true;
        this.assetId = assetId;
    }

    public abstract void update();
    public abstract void draw();
    public abstract void die();
    public abstract void takeDamage(int d);
    public boolean isAlive() { return alive; }
    public int[] getPosition() { return new int[]{x, y}; }
    protected void onDeath() { alive = false; }
    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }

    /// get and setters
    public int    getX()          { return x; }
    public void   setX(int x)     { this.x = x; }
    public int    getY()          { return y; }
    public void   setY(int y)     { this.y = y; }
    public int    getHp()         { return hp; }
    public int    getMaxHp()      { return maxHp; }
    public String getAssetId()    { return assetId; }
}
