package pvz.model.enums;

public enum SunType {
    NORMAL(25, 80),        ///  25 sun, 80% chance
    SPECIAL(100, 15),      ///  100sun, 15% chance
    RADIOACTIVE(25, 5);    ///  middle of air

    private final int value;
    private final int dropChancePercent;

    SunType(int value, int dropChancePercent) {
        this.value = value;
        this.dropChancePercent = dropChancePercent;
    }
    public int getValue() {
        return value;
    }
    public int getDropChancePercent() {
        return dropChancePercent;
    }
}
