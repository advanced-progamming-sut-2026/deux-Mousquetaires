package pvz.model.auth;

import pvz.model.enums.WorldType;

import java.util.ArrayList;
import java.util.List;

public class World {

    private final WorldType type;
    private final String name;
    private final String mapPath;
    private final List<String> specialFeatures;

    public World(WorldType type, String name, String mapPath) {
        this.type = type;
        this.name = name;
        this.mapPath = mapPath;
        this.specialFeatures = new ArrayList<>();
    }

    public void addSpecialFeature(String feature) { specialFeatures.add(feature); }

    public static World ancientEgypt() {
        World world = new World(WorldType.ANCIENT_EGYPT, "Ancient Egypt", "assets/maps/egypt.png");
        world.addSpecialFeature("Tombstones block planting");
        world.addSpecialFeature("Ra Zombie steals sun");
        return world;
    }

    public static World pirateSeas() {
        World world = new World(WorldType.PIRATE_SEAS, "Pirate Seas", "assets/maps/pirate.png");
        world.addSpecialFeature("Water terrain requires Lily Pad");
        world.addSpecialFeature("Fisherman zombie pulls plants");
        return world;
    }

    public static World wildWest() {
        World world = new World(WorldType.WILD_WEST, "Wild West", "assets/maps/west.png");
        world.addSpecialFeature("Mine-cart lanes shift plants");
        world.addSpecialFeature("Explorer zombie carries torch");
        return world;
    }

    public static World farFuture() {
        World world = new World(WorldType.FAR_FUTURE, "Far Future", "assets/maps/future.png");
        world.addSpecialFeature("Troglobite pushes ice columns");
        world.addSpecialFeature("Arcade zombie spawns machines");
        return world;
    }

    public static World darkAges() {
        World world = new World(WorldType.DARK_AGES, "Dark Ages", "assets/maps/dark.png");
        world.addSpecialFeature("Tombstone cells");
        world.addSpecialFeature("Parasol zombie deflects lobbers");
        return world;
    }

    public static World lostCity() {
        World world = new World(WorldType.LOST_CITY, "Lost City", "assets/maps/lost.png");
        world.addSpecialFeature("Turquoise zombie steals sun and fires laser");
        return world;
    }
    /// get
    public WorldType getType() { return type; }
    public String getName() { return name; }
    public String getMapPath() { return mapPath; }
    public List<String> getSpecialFeatures(){ return specialFeatures; }
}
