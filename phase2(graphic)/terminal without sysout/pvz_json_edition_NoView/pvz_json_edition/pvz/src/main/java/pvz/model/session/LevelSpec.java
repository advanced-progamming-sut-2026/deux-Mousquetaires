package pvz.model.session;

import pvz.model.enums.LevelType;
import pvz.model.enums.PlantType;
import pvz.model.enums.WorldType;
import pvz.model.enums.ZombieType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class LevelSpec {

    private final String id;
    private final LevelType levelType;
    private final WorldType world;
    private final int waveCount;
    private final int baseBudget;
    private final List<ZombieType> allowedZombies;
    private final PlantType rewardPlant;    /// unlocked after first win
    /// rules
    private int timeLimitSeconds;
    private int killTarget;
    private int deadlineColumn;
    private int maxPlantLosses = -1;
    private int initialSun = 50;
    private boolean skySunEnabled = true;
    private boolean sunProducersAllowed = true;
    private Set<PlantType> lockedPlants = EnumSet.noneOf(PlantType.class);
    private List<PlantType> forcedPlants = new ArrayList<>();
    private List<PlantType> conveyorPlants = new ArrayList<>();
    private final List<int[]> preplacedProtected = new ArrayList<>(); ///{plantOrdinal, x, y}
    private long scoreSeed;

    public LevelSpec(String id, LevelType levelType, WorldType world, int waveCount, int baseBudget, List<ZombieType> allowedZombies, PlantType rewardPlant) {
        this.id = id;
        this.levelType = levelType;
        this.world = world;
        this.waveCount = waveCount;
        this.baseBudget = baseBudget;
        this.allowedZombies = allowedZombies;
        this.rewardPlant = rewardPlant;
    }
    /// /// factory
    public static LevelSpec adventure(int chapter, int level) {
        if (chapter < 1 || chapter > 4 || level < 1 || level > 4) return null;
        switch (chapter) {
            case 1: return egypt(level);
            case 2: return frostbite(level);
            case 3: return beach(level);
            case 4: return darkAges(level);
            default: return null;
        }
    }

    private static LevelSpec egypt(int level) {
        List<ZombieType> pool = Arrays.asList(
                ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD,
                ZombieType.RA, ZombieType.EXPLORER, ZombieType.TOMBRAISER,
                ZombieType.NEWSPAPER, ZombieType.PROSPECTOR);
        switch (level) {
            case 1: {
                LevelSpec spec = new LevelSpec("1-1", LevelType.NORMAL, WorldType.ANCIENT_EGYPT, 3, 8, pool.subList(0, 4), PlantType.POTATO_MINE);
                return spec;
            }
            case 2: {
                LevelSpec spec = new LevelSpec("1-2", LevelType.CONVEYOR_BELT, WorldType.ANCIENT_EGYPT, 3, 10, pool.subList(0, 5), PlantType.CABBAGE_PULT);
                spec.conveyorPlants = Arrays.asList(PlantType.PEASHOOTER, PlantType.WALL_NUT, PlantType.POTATO_MINE, PlantType.CHERRY_BOMB);
                return spec;
            }
            case 3: {
                LevelSpec spec = new LevelSpec("1-3", LevelType.LOCKED_PLANTS, WorldType.ANCIENT_EGYPT, 4, 12, pool.subList(0, 6), PlantType.TORCHWOOD);
                spec.lockedPlants = EnumSet.of(PlantType.PEASHOOTER, PlantType.SNOW_PEA, PlantType.REPEATER, PlantType.MEGA_GATLING_PEA, PlantType.FIRE_PEASHOOTER);
                return spec;
            }
            default: {
                LevelSpec spec = new LevelSpec("1-4", LevelType.NORMAL, WorldType.ANCIENT_EGYPT, 5, 14, pool, PlantType.TALL_NUT);
                return spec;
            }
        }
    }

    private static LevelSpec frostbite(int level) {
        List<ZombieType> pool = Arrays.asList(
                ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD,
                ZombieType.HUNTER, ZombieType.DODO_RIDER, ZombieType.TROGLOBITE,
                ZombieType.BLOCKHEAD, ZombieType.ALL_STAR);
        switch (level) {
            case 1: {
                LevelSpec spec = new LevelSpec("2-1", LevelType.NORMAL, WorldType.FROSTBITE_CAVES, 3, 10, pool.subList(0, 5), PlantType.PUFF_SHROOM);
                return spec;
            }
            case 2: {
                LevelSpec spec = new LevelSpec("2-2", LevelType.SAVE_OUR_SEEDS, WorldType.FROSTBITE_CAVES, 4, 12, pool.subList(0, 6), PlantType.FUME_SHROOM);
                spec.preplacedProtected.add(new int[]{PlantType.SUNFLOWER.ordinal(), 2, 2});
                spec.preplacedProtected.add(new int[]{PlantType.SUNFLOWER.ordinal(), 2, 4});
                spec.preplacedProtected.add(new int[]{PlantType.WALL_NUT.ordinal(), 5, 3});
                return spec;
            }
            case 3: {
                LevelSpec spec = new LevelSpec("2-3", LevelType.TIMED_WAR, WorldType.FROSTBITE_CAVES, 4, 12, pool.subList(0, 7), PlantType.JALAPENO);
                spec.timeLimitSeconds = 240;
                spec.killTarget = 25;
                return spec;
            }
            default: {
                LevelSpec spec = new LevelSpec("2-4", LevelType.NORMAL, WorldType.FROSTBITE_CAVES, 5, 15, pool, PlantType.WINTER_MELON);
                return spec;
            }
        }
    }

    private static LevelSpec beach(int level) {
        List<ZombieType> pool = Arrays.asList(
                ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD,
                ZombieType.SNORKEL, ZombieType.FISHERMAN, ZombieType.OCTOPUS,
                ZombieType.PARASOL, ZombieType.TURQUOISE_SKULL);
        switch (level) {
            case 1: {
                LevelSpec spec = new LevelSpec("3-1", LevelType.NORMAL, WorldType.BIG_WAVE_BEACH, 3, 11, pool.subList(0, 5), PlantType.LILY_PAD);
                return spec;
            }
            case 2: {
                LevelSpec spec = new LevelSpec("3-2", LevelType.DEAD_LINE, WorldType.BIG_WAVE_BEACH, 4, 12, pool.subList(0, 6), PlantType.TANGLE_KELP);
                spec.deadlineColumn = 3;
                return spec;
            }
            case 3: {
                LevelSpec spec = new LevelSpec("3-3", LevelType.SAVE_THE_PLANTS, WorldType.BIG_WAVE_BEACH, 4, 13, pool.subList(0, 7), PlantType.MELON_PULT);
                spec.maxPlantLosses = 3;
                return spec;
            }
            default: {
                LevelSpec spec = new LevelSpec("3-4", LevelType.NORMAL, WorldType.BIG_WAVE_BEACH, 5, 16, pool, PlantType.GARLIC);
                return spec;
            }
        }
    }

    private static LevelSpec darkAges(int level) {
        List<ZombieType> pool = Arrays.asList(
                ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.KNIGHT,
                ZombieType.JESTER, ZombieType.WIZARD, ZombieType.KING,
                ZombieType.IMP_DRAGON, ZombieType.GARGANTUAR);
        switch (level) {
            case 1: {
                LevelSpec spec = new LevelSpec("4-1", LevelType.NIGHT_OPS, WorldType.DARK_AGES, 3, 12, pool.subList(0, 5), PlantType.SUN_SHROOM);
                spec.skySunEnabled = false;
                return spec;
            }
            case 2: {
                LevelSpec spec = new LevelSpec("4-2", LevelType.LOVE_YOUR_PLANTS, WorldType.DARK_AGES, 4, 13, pool.subList(0, 6), PlantType.MAGNET_SHROOM);
                spec.skySunEnabled = false;
                spec.maxPlantLosses = 2;
                return spec;
            }
            case 3: {
                LevelSpec spec = new LevelSpec("4-3", LevelType.PLANT_WHAT_YOU_GET, WorldType.DARK_AGES, 4, 14, pool.subList(0, 7), PlantType.HOMING_THISTLE);
                spec.skySunEnabled = false;
                spec.sunProducersAllowed = false;
                spec.initialSun = 800;
                return spec;
            }
            default: {
                LevelSpec spec = new LevelSpec("4-4", LevelType.NORMAL, WorldType.DARK_AGES, 6, 16, pool, PlantType.MEGA_GATLING_PEA);
                spec.skySunEnabled = false;
                return spec;
            }
        }
    }

    public static LevelSpec scoredGame() {
        LevelSpec spec = new LevelSpec("scored", LevelType.SCORED, WorldType.ANCIENT_EGYPT, 4, 12,
                Arrays.asList(ZombieType.BASIC, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD, ZombieType.NEWSPAPER, ZombieType.PIANIST, ZombieType.BARREL_ROLLER), null);
        spec.scoreSeed = LocalDate.now().toEpochDay();
        return spec;
    }

    /// get
    public String getId() { return id; }
    public LevelType getLevelType() { return levelType; }
    public WorldType getWorld() { return world; }
    public int getWaveCount() { return waveCount; }
    public int getBaseBudget() { return baseBudget; }
    public List<ZombieType> getAllowedZombies() { return allowedZombies; }
    public PlantType getRewardPlant() { return rewardPlant; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public int getKillTarget() { return killTarget; }
    public int getDeadlineColumn() { return deadlineColumn; }
    public int getMaxPlantLosses() { return maxPlantLosses; }
    public int getInitialSun() { return initialSun; }
    public boolean isSkySunEnabled() { return skySunEnabled; }
    public boolean isSunProducersAllowed() { return sunProducersAllowed; }
    public Set<PlantType> getLockedPlants() { return lockedPlants; }
    public List<PlantType> getForcedPlants() { return forcedPlants; }
    public List<PlantType> getConveyorPlants() { return conveyorPlants; }
    public List<int[]> getPreplacedProtected() { return preplacedProtected; }
    public long getScoreSeed() { return scoreSeed; }
    /// flag
    public boolean isConveyor() { return levelType == LevelType.CONVEYOR_BELT; }
    public boolean isPlantWhatYouGet() { return levelType == LevelType.PLANT_WHAT_YOU_GET; }
    public boolean isScored() { return levelType == LevelType.SCORED; }
}
