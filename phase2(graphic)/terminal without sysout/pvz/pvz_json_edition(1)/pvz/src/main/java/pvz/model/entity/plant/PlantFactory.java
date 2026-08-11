package pvz.model.entity.plant;

import pvz.model.enums.PlantType;

public final class PlantFactory {

    private PlantFactory() {}

    public static CatalogPlant createPlant(PlantType type) {
        return createPlant(type, 0, 0);
    }
    public static CatalogPlant createPlant(PlantType type, int x, int y) {
        return new CatalogPlant(type, x, y);
    }
    public static PlantType parseType(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String normalized = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (PlantType type : PlantType.values()) {
            if (type.name().equals(normalized) || type.name().replace("_", "").equals(normalized.replace("_", ""))) {
                return type;
            }
        }
        return null;
    }
}
