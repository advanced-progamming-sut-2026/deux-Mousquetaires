package pvz.model.game;
public class Sun {

    public static final int DEFAULT_VALUE  = 25;
    public static final int MAX_LIFETIME   = 600;   // ~10 s at 60 ticks/s
    public static final int SKY_DROP_SPEED = 2;

    private int x;
    private int y;
    private final int value;
    private boolean collected;
    private int lifetime;
    private boolean falling;
    private int targetY;
    public Sun(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value= value;
        this.collected = false;
        this.lifetime = 0;
        this.falling = false;
        this.targetY = y;
    }
    public Sun(int x, int targetY) {
        this(x,0, DEFAULT_VALUE);
        this.falling = true;
        this.targetY = targetY;
    }
    public void update() {
        if (collected) return;
        if (falling && y < targetY) {
            y += SKY_DROP_SPEED;
            if (y >= targetY) { y = targetY; falling = false; }
        }
        lifetime++;
    }
    public int collect() {
        if (!collected) { collected = true; return value; }
        return 0;
    }
    public boolean isExpired()   { return !collected && lifetime >= MAX_LIFETIME; }
    public boolean isCollected() { return collected; }
    public boolean isFalling()   { return falling; }
    public int getX()            { return x; }
    public int getY()            { return y; }
    public int getValue()        { return value; }
    public int getLifetime()     { return lifetime; }
}
