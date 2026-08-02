package pvz.model.session;

import pvz.model.enums.SunType;

public class FallingSun {

    public static final int FALL_TICKS = 50;

    private final int x;
    private final int y;
    private SunType type;
    private int ticksLeft = FALL_TICKS;

    public FallingSun(int x, int y, SunType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
    public boolean tick() {
        return --ticksLeft <= 0;
    }
    public void land() {
        if (type == SunType.RADIOACTIVE) type = SunType.NORMAL;
    }

    public boolean isRadioactive() {
        return type == SunType.RADIOACTIVE;
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public SunType getType() { return type; }
    public int getValue() { return type.getValue(); }
}
