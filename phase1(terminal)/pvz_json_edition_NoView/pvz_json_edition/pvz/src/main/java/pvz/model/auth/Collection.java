package pvz.model.auth;

import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;

import java.util.EnumMap;
import java.util.Map;

public class Collection {

    public static final int MAX_PLANT_LEVEL = 5;

    private final Map<PlantType, Boolean> plants = new EnumMap<>(PlantType.class);
    private final Map<ZombieType, Boolean> zombies = new EnumMap<>(ZombieType.class);
    private final Map<PlantType, Integer> plantLevels = new EnumMap<>(PlantType.class);

    ///plant
    public void unlockPlant(PlantType type) {
        boolean wasNew = !isPlantUnlocked(type);
        unlockPlantQuietly(type);
        if (wasNew) {}
    }
    public void unlockPlantQuietly(PlantType type) {
        plants.put(type,true);
        plantLevels.putIfAbsent(type, 1);
    }
    public boolean isPlantUnlocked(PlantType type) {
        return plants.getOrDefault(type, false);
    }

    public boolean upgradePlant(PlantType type) {
        if (!isPlantUnlocked(type)) return false;
        int level = getPlantLevel(type);
        if (level >= MAX_PLANT_LEVEL) return false;
        plantLevels.put(type, level + 1);
        return true;
    }
    public int getPlantLevel(PlantType type) {
        return plantLevels.getOrDefault(type, 1);
    }
    public void setPlantLevel(PlantType type, int level) {
        plantLevels.put(type, Math.max(1, Math.min(MAX_PLANT_LEVEL, level)));
    }

    ///zombie
    public void unlockZombie(ZombieType type) {
        zombies.put(type, true);
    }
    public void unlockZombieQuietly(ZombieType type) {
        zombies.put(type, true);
    }
    public boolean isZombieUnlocked(ZombieType type) {
        return zombies.getOrDefault(type, false);
    }
    public Map<PlantType, Boolean> getPlants() { return plants; }
    public Map<ZombieType, Boolean> getZombies() { return zombies; }
}
