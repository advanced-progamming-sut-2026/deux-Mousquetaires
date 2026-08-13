package pvz.model.entity.zombie;

import pvz.model.enums.ZombieType;
public final class ZombieFactory {
    public static final int SPAWN_COLUMN = 9;
    private ZombieFactory() {}

    public static CatalogZombie createZombie(ZombieType type, int lane) {
        return new CatalogZombie(type, SPAWN_COLUMN, lane);
    }
    public static CatalogZombie createZombie(ZombieType type, int column, int lane) {
        return new CatalogZombie(type, column, lane);
    }
    public static ZombieType parseType(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String normalized = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (ZombieType type : ZombieType.values()) {
            if (type.name().equals(normalized) || type.name().replace("_", "").equals(normalized.replace("_", ""))) {
                return type;}
        }
        return null;
    }
}
