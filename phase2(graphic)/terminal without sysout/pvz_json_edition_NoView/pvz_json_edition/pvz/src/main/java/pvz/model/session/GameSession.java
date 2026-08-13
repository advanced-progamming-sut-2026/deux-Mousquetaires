package pvz.model.session;

import pvz.model.auth.User;
import pvz.model.entity.plant.CatalogPlant;
import pvz.model.entity.plant.PlantCatalog;
import pvz.model.entity.plant.PlantFactory;
import pvz.model.entity.zombie.CatalogZombie;
import pvz.model.entity.zombie.ZombieCatalog;
import pvz.model.entity.zombie.ZombieFactory;
import pvz.model.enums.LevelType;
import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantTag;
import pvz.model.enums.PlantType;
import pvz.model.enums.SunType;
import pvz.model.enums.TerrainType;
import pvz.model.enums.WorldType;
import pvz.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GameSession {

    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int TICKS_PER_SECOND = 10;
    private static final int FIRST_WAVE_DELAY_TICKS = 20 * TICKS_PER_SECOND;
    private static final int MAX_GLOWING = 3;
    ///
    final User user;
    final LevelSpec spec;
    final Random rng;
    final float difficultyMul;      // dl / 3
    final int difficultyLevel;      // 1..5
    /// state
    final TerrainType[][] terrain = new TerrainType[ROWS + 1][COLS + 1];
    final int[][] tombHp = new int[ROWS + 1][COLS + 1];
    ///0 = plain tomb
    /// 1 = holds 50 sun
    /// 2 = holds a plant food
    final int[][] tombBonus = new int[ROWS + 1][COLS + 1];
    final boolean[] mowers = new boolean[ROWS + 1];
    final List<PlantedUnit> plants = new ArrayList<>();
    final List<ActiveZombie> zombies = new ArrayList<>();
    final List<FallingSun> fallingSuns = new ArrayList<>();
    ///
    final List<int[]> groundSuns = new ArrayList<>();
    final Map<ActiveZombie, PlantedUnit> octopusBindings = new HashMap<>();
    final Map<ActiveZombie, List<PlantedUnit>> wizardCurses = new HashMap<>();
    /// player
    int sunBank;
    int plantFoods;
    final List<PlantType> chosenPlants;
    final Set<PlantType> boostedPlants;
    final Map<PlantType, Integer> seedCooldowns = new EnumMap<>(PlantType.class);
    boolean cooldownsDisabled;
    final List<PlantType> conveyorBelt = new ArrayList<>();
    int conveyorTimer;
    /// progressing
    long tick;
    int currentWave;                 // 0 = before the first wave
    boolean wavesStarted;
    boolean waveActive;
    int waveInitialHp;
    final List<Object[]> pendingSpawns = new ArrayList<>(); // {ZombieType, row, tickDue}
    int glowingSpawned;
    double skySunTimer;
    boolean gameover;
    boolean won;
    String endMessage = "";
    int killsThisLevel;
    int plantsLost;
    int plantsLostAtWaveStart;
    int sunCollectedTotal;
    int plantsPlantedCount;
    int coinsEarned;
    int gemsEarned;
    int potsEarned;
    int plantFoodsEarned;
    final ScoreTracker score;
    /// meow
    final Map<String, Integer> killsBySource = new HashMap<>();
    final Set<Integer> killLanes = new HashSet<>();
    int quickKills;
    /// quest
    final Map<PlantType, Integer> killsByPlant = new EnumMap<>(PlantType.class);
    final Set<PlantFamily> killFamilies = EnumSet.noneOf(PlantFamily.class);
    final Set<PlantFamily> familiesPlanted = EnumSet.noneOf(PlantFamily.class);
    final Set<Integer> plantedCols = new HashSet<>();
    final Set<Integer> plantedRows = new HashSet<>();
    int mowerKills;
    int col1KillsNoMower;
    int fastKills;
    int explosivesPlanted;
    int sunProducersPlanted;
    boolean allPlantedShrooms = true;
    long firstWaveTick = -1;
    private final PlantEngine plantEngine;
    private final ZombieEngine zombieEngine;

    public GameSession(User user, LevelSpec spec, List<PlantType> chosenPlants, Set<PlantType> boostedPlants, long seed) {
        this.user = user;
        this.spec = spec;
        this.chosenPlants = new ArrayList<>(chosenPlants);
        this.boostedPlants = new HashSet<>(boostedPlants);
        this.rng = new Random(spec.isScored() ? spec.getScoreSeed() : seed);
        this.difficultyLevel = user.getSettings().getDifficulty();
        this.difficultyMul = difficultyLevel / 3.0f;
        this.sunBank = spec.getInitialSun();
        this.plantFoods = user.getPlantFoods();
        this.score = spec.isScored() ? new ScoreTracker() : null;
        this.plantEngine = new PlantEngine(this);
        this.zombieEngine = new ZombieEngine(this);
        initField();
    }

    private void initField() {
        for (int y = 1; y <= ROWS; y++) {
            mowers[y] = true;
            for (int x = 1; x <= COLS; x++) {terrain[y][x] = TerrainType.GRASS;}
        }
        switch (spec.getWorld()) {
            case ANCIENT_EGYPT:
                placeRandomCells(TerrainType.TOMB, 2, 5, 8);
                break;
            case FROSTBITE_CAVES:
                placeRandomCells(rng.nextBoolean() ? TerrainType.ICE_SLIP_UP : TerrainType.ICE_SLIP_DOWN, 2, 3, 7);
                placeRandomCells(TerrainType.FROZEN, 3, 3, 7);
                break;
            case BIG_WAVE_BEACH:
                for (int y = 2; y <= 4; y++) {
                    for (int x = 6; x <= 8; x++) {terrain[y][x] = TerrainType.WATER;}
                }
                break;
            case DARK_AGES:
                placeRandomCells(TerrainType.NECROMANCY, 2, 4, 8);
                break;
            default:
                break;
        }
        for (int[] pre : spec.getPreplacedProtected()) {
            PlantType type = PlantType.values()[pre[0]];
            CatalogPlant plant = PlantFactory.createPlant(type, pre[1], pre[2]);
            plants.add(new PlantedUnit(plant, pre[1], pre[2], true));
        }
        if (spec.isConveyor()) refillConveyor();
    }

    private void placeRandomCells(TerrainType type, int count, int minCol, int maxCol) {
        int placed = 0;
        int guard = 0;
        while (placed < count && guard++ < 100) {
            int x = minCol + rng.nextInt(maxCol - minCol + 1);
            int y = 1 + rng.nextInt(ROWS);
            if (terrain[y][x] == TerrainType.GRASS) {
                terrain[y][x] = type;
                if (type == TerrainType.TOMB) tombHp[y][x] = 700;
                else if (type == TerrainType.FROZEN) tombHp[y][x] = 600;
                placed++;}
        }
    }
    /// game timer timing(tick)
    public void advanceTicks(int n) {
        for (int i = 0; i < n && !gameover; i++) {
            tick++;
            killsBySource.clear();
            killLanes.clear();
            quickKills = 0;
            tickSkySun();
            tickFallingSuns();
            tickConveyor();
            tickCooldowns();
            plantEngine.tick();
            zombieEngine.tick();
            tickWaves();
            flushScoreEvents();
            checkTimedWar();
            checkVictory();
        }
    }

    private void tickCooldowns() {
        for (Map.Entry<PlantType, Integer> entry : seedCooldowns.entrySet()) {
            if (entry.getValue() > 0) entry.setValue(entry.getValue() - 1);
        }
    }
    private void tickSkySun() {
        if (!spec.isSkySunEnabled()) return;
        skySunTimer -= 1;
        if (skySunTimer <= 0) {
            double seconds = tick / (double) TICKS_PER_SECOND;
            double interval = Math.min(6 + 0.05 * seconds, 12) * difficultyMul;
            skySunTimer = interval * TICKS_PER_SECOND;
            int x = 1 + rng.nextInt(COLS);
            int y = 1 + rng.nextInt(ROWS);
            SunType type = rollSunType();
            fallingSuns.add(new FallingSun(x, y, type));
        }
    }

    private SunType rollSunType() {
        int roll = rng.nextInt(100);
        if (roll < 80) return SunType.NORMAL;
        if (roll < 95) return SunType.SPECIAL;
        return SunType.RADIOACTIVE;
    }

    private void tickFallingSuns() {
        Iterator<FallingSun> it = fallingSuns.iterator();
        while (it.hasNext()) {
            FallingSun sun = it.next();
            if (sun.tick()) {
                sun.land();
                groundSuns.add(new int[]{sun.getX(), sun.getY(), sun.getValue()});
                it.remove();}
        }
    }

    public String collectSun(int x, int y) {
        for (Iterator<int[]> it = groundSuns.iterator(); it.hasNext(); ) {
            int[] sun = it.next();
            if (sun[0] == x && sun[1] == y) {
                it.remove();
                sunBank += sun[2];
                sunCollectedTotal += sun[2];
                return "Collected " + sun[2] + " sun. You now have " + sunBank + " sun.";
            }
        }
        for (Iterator<FallingSun> it = fallingSuns.iterator(); it.hasNext(); ) {
            FallingSun sun = it.next();
            if (sun.getX() == x && sun.getY() == y) {
                it.remove();
                if (sun.isRadioactive()) radioactiveBurst(x, y);
                sunBank += sun.getValue();
                sunCollectedTotal += sun.getValue();
                return "Caught a " + sun.getType() + " sun mid-air (+" + sun.getValue() + "). You now have " + sunBank + " sun.";
            }
        }
        return "There is no sun at (" + x + ", " + y + ").";
    }

    private void radioactiveBurst(int x, int y) {
        for (ActiveZombie az : new ArrayList<>(zombies)) {
            if (Math.abs(az.cellCol() - x) <= 2 && Math.abs(az.row - y) <= 2) {
                damageZombie(az, 150, DamageKind.NORMAL, "radioactive");}
        }
        for (PlantedUnit unit : new ArrayList<>(plants)) {
            if (Math.abs(unit.col - x) <= 1 && Math.abs(unit.row - y) <= 1) damagePlant(unit, 80);
        }
    }

    public String showSunAmount() {return "Current sun: " + sunBank;}

    public void cheatAddSuns(int count) {
        sunBank += 25 * count;
    }
    /// ///
    /// ///
    private void tickConveyor() {
        if (!spec.isConveyor()) return;
        conveyorTimer--;
        if (conveyorTimer <= 0) refillConveyor();
    }

    private void refillConveyor() {
        conveyorTimer = 12 * TICKS_PER_SECOND;
        if (conveyorBelt.size() < 5 && !spec.getConveyorPlants().isEmpty()) {
            PlantType next = spec.getConveyorPlants().get(rng.nextInt(spec.getConveyorPlants().size()));
            conveyorBelt.add(next);
        }
    }

    /// plant plant

    public String plantPlant(PlantType type, int x, int y) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) return "Position (" + x + ", " + y + ") is outside the lawn.";
        if (findPlant(x, y) != null) return "There is already a plant at (" + x + ", " + y + ").";
        if (terrain[y][x] == TerrainType.TOMB && type != PlantType.GRAVE_BUSTER) return "You cannot plant on a tombstone (only a Grave Buster can).";
        if (type == PlantType.GRAVE_BUSTER && terrain[y][x] != TerrainType.TOMB) return "The Grave Buster can only be planted on a tombstone.";
        if (terrain[y][x] == TerrainType.FROZEN && type != PlantType.HOT_POTATO) return "That tile is frozen solid. Melt it with fire or a Hot Potato.";
        if (type == PlantType.HOT_POTATO && terrain[y][x] != TerrainType.FROZEN) return "The Hot Potato only works on a frozen tile.";
        if (terrain[y][x] == TerrainType.WATER && type != PlantType.LILY_PAD && type != PlantType.TANGLE_KELP && type != PlantType.SEA_SHROOM && !hasLilyPad(x, y)) {
            return "You need a Lily Pad to plant on water.";}
        PlantCatalog.Stats stats = PlantCatalog.of(type);
        if (spec.isConveyor()) {
            if (!conveyorBelt.remove(type)) return "The conveyor belt has no " + type + " packet right now.";
        }
        else {
            if (!chosenPlants.contains(type)) return "You did not bring " + type + " to this level.";
            if (!spec.isSunProducersAllowed() && stats.tags.contains(PlantTag.SUN_PRODUCER)) return "Sun producers are not allowed in this level.";
            if (!cooldownsDisabled && seedCooldowns.getOrDefault(type, 0) > 0) return type + " is recharging (" + seedCooldowns.get(type) + " ticks left).";
            boolean freePhase = spec.isPlantWhatYouGet() && !wavesStarted;
            if (!freePhase && !spec.isPlantWhatYouGet()) {
                if (sunBank < stats.sunCost) return "Not enough sun (" + sunBank + "/" + stats.sunCost + ").";
                sunBank -= stats.sunCost;
            }
            if (!cooldownsDisabled) seedCooldowns.put(type, stats.rechargeTicks);
        }
        CatalogPlant plant = PlantFactory.createPlant(type, x, y);
        PlantedUnit unit = new PlantedUnit(plant, x, y, false);
        int level = user.getCollection().getPlantLevel(type);
        unit.damageBonus = 1.0 + 0.25 * (level - 1);
        if (type == PlantType.LILY_PAD) unit.onLilyPad = false;
        else if (terrain[y][x] == TerrainType.WATER) unit.onLilyPad = true;
        plants.add(unit);
        plantsPlantedCount++;
        plantedCols.add(x);
        plantedRows.add(y);
        familiesPlanted.add(stats.family);
        if (stats.tags.contains(PlantTag.EXPLOSIVE)) explosivesPlanted++;
        if (stats.tags.contains(PlantTag.SUN_PRODUCER)) sunProducersPlanted++;
        if (!stats.tags.contains(PlantTag.SHROOM)) allPlantedShrooms = false;
        String message = "Planted " + type + " at (" + x + ", " + y + ").";
        if (boostedPlants.contains(type)) {
            plantEngine.applyPlantFood(unit);
            message += " (boosted - plant food effect applied!)";
        }
        return message;
    }

    private boolean hasLilyPad(int x, int y) {
        for (PlantedUnit unit : plants) {
            if (unit.col == x && unit.row == y && unit.getType() == PlantType.LILY_PAD) return true;
        }
        return false;
    }

    public String pluckPlant(int x, int y) {
        PlantedUnit unit = findPlant(x, y);
        if (unit == null) return "There is no plant at (" + x + ", " + y + ").";
        plants.remove(unit);
        return "Plucked " + unit.getType() + " from (" + x + ", " + y + ").";
    }

    public String feedPlant(int x, int y) {
        PlantedUnit unit = findPlant(x, y);
        if (unit == null) return "There is no plant at (" + x + ", " + y + ").";
        if (plantFoods <= 0) return "You have no plant food.";
        plantFoods--;
        plantEngine.applyPlantFood(unit);
        return "Fed plant food to " + unit.getType() + " at (" + x + ", " + y + "). Plant foods left: " + plantFoods;
    }

    public void cheatAddPlantFood() {
        if (plantFoods < 3) plantFoods++;
    }

    public void cheatRemoveCooldown() {
        cooldownsDisabled = true;
        seedCooldowns.clear();
    }

    public void releaseNuke() {
        for (ActiveZombie az : new ArrayList<>(zombies)) {killZombie(az, "nuke");}
    }

    PlantedUnit findPlant(int x, int y) {
        for (PlantedUnit unit : plants) {
            if (unit.col == x && unit.row == y && unit.getType() != PlantType.LILY_PAD) return unit;
        }
        for (PlantedUnit unit : plants) {
            if (unit.col == x && unit.row == y) return unit;
        }
        return null;
    }

    /// /// wave

    public String startZombieWaves() {
        if (!spec.isPlantWhatYouGet()) return "This level's waves start automatically.";
        if (wavesStarted) return "The zombie waves have already started.";
        wavesStarted = true;
        startWave(1);
        return "The horde is on its way...";
    }

    private void tickWaves() {
        if (spec.isPlantWhatYouGet() && !wavesStarted) return;
        if (!wavesStarted && tick >= FIRST_WAVE_DELAY_TICKS) {
            wavesStarted = true;
            startWave(1);
        }
        if (!waveActive) return;
        /// spawn
        for (Iterator<Object[]> it = pendingSpawns.iterator(); it.hasNext(); ) {
            Object[] entry = it.next();
            if ((long) entry[2] <= tick) {
                spawnFromWave((ZombieType) entry[0], (int) entry[1]);
                it.remove();
            }
        }
        /// next wave at 75% of the current wave HP destroyed
        if (currentWave < spec.getWaveCount()) {
            int aliveHp = 0;
            for (ActiveZombie az : zombies) {
                if (az.spawnedWave == currentWave) aliveHp += Math.max(0, az.zombie.getHp()) + az.zombie.getArmorHp();
            }
            boolean spawnedAll = pendingSpawns.isEmpty();
            if (spawnedAll && waveInitialHp > 0 && aliveHp <= waveInitialHp * 0.25) {
                onWaveSurvivedCheck();
                startWave(currentWave + 1);
            }
        }
    }

    private void onWaveSurvivedCheck() {
        if (score != null && plantsLost == plantsLostAtWaveStart) score.onUntouchedWave(currentWave);
        plantsLostAtWaveStart = plantsLost;
    }

    private void startWave(int n) {
        if (firstWaveTick < 0) firstWaveTick = tick;
        currentWave = n;
        waveActive = true;
        if (n == spec.getWaveCount()) {
            applyFinalWaveWorldEvent();
        }
        else {}
        applyWaveWorldEvent();
        double budget = spec.getBaseBudget() * Math.pow(1.25, n - 1);
        if (n == spec.getWaveCount()) budget = 2 * spec.getBaseBudget() * Math.pow(1.25, n - 2);

        double costMul = 3.0 / difficultyLevel;
        List<ZombieType> pool = spec.getAllowedZombies();
        waveInitialHp = 0;
        int spawned = 0;
        int guard = 0;
        while (budget > 0 && guard++ < 200) {
            ZombieType type = pool.get(rng.nextInt(pool.size()));
            double cost = ZombieCatalog.of(type).waveCost * costMul;
            if (cost > budget && spawned > 0) break;
            budget -= cost;
            spawned++;
            int row = 1 + rng.nextInt(ROWS);
            long due = tick + rng.nextInt(10 * TICKS_PER_SECOND);
            pendingSpawns.add(new Object[]{type, row, due});
        }
    }

    private void applyWaveWorldEvent() {
        if (spec.getWorld() == WorldType.DARK_AGES) {
            raiseRandomTombs();
            /// tombstones raise fresh zombies at each wave
            for (int y = 1; y <= ROWS; y++) {
                for (int x = 1; x <= COLS; x++) {
                    if (terrain[y][x] == TerrainType.NECROMANCY) {
                        spawnZombieAt(ZombieType.BASIC, x, y, currentWave);
                        }
                }
            }
        }
        if (spec.getWorld() == WorldType.BIG_WAVE_BEACH) {
            /// one zombie washes ashore mid_lawn
            int row = 2 + rng.nextInt(3);
            spawnZombieAt(ZombieType.SNORKEL, 6, row, currentWave);
        }
    }

     ////dark Ages---->at the start of every wave a few tombstones may rise on
    private void raiseRandomTombs() {
        int count = rng.nextInt(3);
        for (int i = 0; i < count; i++) {
            int x = 3 + rng.nextInt(COLS - 2);
            int y = 1 + rng.nextInt(ROWS);
            if (terrain[y][x] != TerrainType.GRASS || findPlant(x, y) != null) continue;
            terrain[y][x] = TerrainType.TOMB;
            tombHp[y][x] = 700;
            int roll = rng.nextInt(100);
            tombBonus[y][x] = roll < 20 ? 1 : roll < 35 ? 2 : 0;
        }
    }

    private String describeTombBonus(int bonus) {
        if (bonus == 1) return " - it glitters with buried sun!";
        return bonus == 2 ? " - something tasty is buried under it!" : "";
    }

    /// tombstone crumbles----->Call the engines
    void onTombDestroyed(int x, int y) {
        if (tombBonus[y][x] == 1) {
            sunBank += 50;
        }
        else if (tombBonus[y][x] == 2 && plantFoods < 3) {
            plantFoods++;
        }
        tombBonus[y][x] = 0;
    }

    private void applyFinalWaveWorldEvent() {
        switch (spec.getWorld()) {
            case ANCIENT_EGYPT:
                int push = 1 + rng.nextInt(4);
                for (ActiveZombie az : zombies) {
                    az.col = Math.max(1.2, az.col - push);
                }
                break;
            case FROSTBITE_CAVES:
                int row = 1 + rng.nextInt(ROWS);
                for (PlantedUnit unit : plants) {
                    if (unit.row == row && !unit.plant.hasTag(PlantTag.FIRE)) unit.frozen = true;
                }
                break;
            default:
                break;
        }
    }

    private void spawnFromWave(ZombieType type, int row) {
        ActiveZombie az = spawnZombieAt(type, ZombieFactory.SPAWN_COLUMN, row, currentWave);
        waveInitialHp += Math.max(0, az.zombie.getHp()) + az.zombie.getArmorHp();
    }

    ActiveZombie spawnZombieAt(ZombieType type, int col, int row, int wave) {
        CatalogZombie zombie = ZombieFactory.createZombie(type, col, row);
        zombie.scaleDifficulty(difficultyMul);
        ActiveZombie az = new ActiveZombie(zombie, row, wave, tick);
        az.col = col;
        if (glowingSpawned < MAX_GLOWING && rng.nextInt(100) < 5) {
            az.glowing = true;
            glowingSpawned++;
        }
        zombies.add(az);
        user.getCollection().unlockZombie(type);
        return az;
    }

    public String cheatSpawnZombie(ZombieType type, int x, int y) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) return "Position (" + x + ", " + y + ") is outside the lawn.";
        spawnZombieAt(type, x, y, Math.max(1, currentWave));
        return "Cheat: spawned " + type + " at (" + x + ", " + y + ").";
    }

    /// damage---->dead

    enum DamageKind { NORMAL, FIRE, ICE, LOB, FUME }

    void damageZombie(ActiveZombie activeZombie, int amount, DamageKind kind, String sourceKey) {
        CatalogZombie z = activeZombie.zombie;
        if (kind == DamageKind.FIRE && z.isFireImmune()) return;
        if (kind == DamageKind.FIRE) activeZombie.thaw();
        boolean hadArmor = z.getArmorHp() > 0;
        if (kind == DamageKind.FUME) z.takeDamageIgnoringArmor(amount);
        else z.takeDamage(amount);
        if (hadArmor && z.getArmorHp() <= 0 && z.getType() == ZombieType.BARREL_ROLLER) {
            spawnZombieAt(ZombieType.IMP, activeZombie.cellCol(), activeZombie.row, activeZombie.spawnedWave);
            spawnZombieAt(ZombieType.IMP, activeZombie.cellCol(), activeZombie.row, activeZombie.spawnedWave);
        }
        if (kind == DamageKind.ICE) {
            activeZombie.slow(0.5, 3 * TICKS_PER_SECOND);
            if (z.getType() == ZombieType.EXPLORER && activeZombie.torchLit) {
                activeZombie.torchLit = false;
            }
        }
        if (!z.isAlive()) recordKill(activeZombie, sourceKey);
    }

    void killZombie(ActiveZombie activeZombie, String sourceKey) {
        activeZombie.zombie.takeDamageIgnoringArmor(1_000_000);
        recordKill(activeZombie, sourceKey);
    }

    private void recordKill(ActiveZombie activeZombie, String sourceKey) {
        if (!zombies.remove(activeZombie)) return;
        killsThisLevel++;
        trackKillForQuests(activeZombie, sourceKey);
        zombieEngine.onDeath(activeZombie);
        if (activeZombie.glowing) {
            if (plantFoods < 3) plantFoods++;
            plantFoodsEarned++;
        }
        else if (rng.nextInt(100) < 10) dropLoot();
        if (score != null) {
            score.onKill();
            killsBySource.merge(sourceKey, 1, Integer::sum);
            killLanes.add(activeZombie.row);
            if (tick - activeZombie.spawnTick <= 5 * TICKS_PER_SECOND) quickKills++;
        }
    }

    /// quest kill
    private void trackKillForQuests(ActiveZombie az, String sourceKey) {
        int sep = sourceKey.indexOf('#');
        if (sep > 0) {
            try {
                PlantType killer = PlantType.valueOf(sourceKey.substring(0, sep));
                killsByPlant.merge(killer, 1, Integer::sum);
                killFamilies.add(PlantCatalog.of(killer).family);
            } catch (IllegalArgumentException ignored) {}
        }
        boolean mowerKill = sourceKey.startsWith("mower-");
        if (mowerKill) mowerKills++;
        if (!mowerKill && az.col <= 1.5 && !mowers[az.row]) col1KillsNoMower++;
        if (firstWaveTick >= 0 && tick - firstWaveTick <= 30L * TICKS_PER_SECOND) fastKills++;
    }

    private void dropLoot() {
        int roll = rng.nextInt(3);
        if (roll == 0) {
            coinsEarned += 50;
            user.addCoins(50);
        }
        else if (roll == 1) {
            gemsEarned++;
            user.addGems(1);
        }
        else { if (user.addPot()) {potsEarned++;}
        }
    }

    void damagePlant(PlantedUnit unit, int amount) {
        unit.plant.takeDamage(amount);
        if (!unit.plant.isAlive()) destroyPlant(unit);
    }

    void destroyPlant(PlantedUnit unit) {
        if (!plants.remove(unit)) return;
        plantsLost++;
        if (unit.protectedPlant) {
            gameover = true;
            won = false;
            endMessage = "A protected plant was destroyed. Level failed!";
            return;
        }
        if (spec.getMaxPlantLosses() >= 0 && plantsLost > spec.getMaxPlantLosses()) {
            gameover = true;
            won = false;
            endMessage = "Too many plants lost (" + plantsLost + "). Level failed!";
        }
    }

    /// win/gameover

    void lose(String message) {
        if (gameover) return;
        gameover = true;
        won = false;
        endMessage = message;
    }

    private void checkTimedWar() {
        if (gameover || spec.getLevelType() != LevelType.TIMED_WAR) return;
        if (killsThisLevel >= spec.getKillTarget()) {
            winNow();
            return;
        }
        if (tick > (long) spec.getTimeLimitSeconds() * TICKS_PER_SECOND) {
            lose("Time is up! You only defeated " + killsThisLevel + "/" + spec.getKillTarget() + " zombies. The zombie ate your brain; LOSER!!!");
        }
    }

    private void checkVictory() {
        if (gameover || spec.getLevelType() == LevelType.TIMED_WAR) return;
        boolean allWavesDone = wavesStarted && currentWave >= spec.getWaveCount() && pendingSpawns.isEmpty() && zombies.isEmpty();
        if (allWavesDone) winNow();
    }

    private void winNow() {
        gameover = true;
        won = true;
        onWaveSurvivedCheck();
        if (score != null && allMowersIntact()) score.onCleanHouseWin();
        endMessage = "Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.";
    }

    private boolean allMowersIntact() {
        for (int y = 1; y <= ROWS; y++) {
            if (!mowers[y]) return false;
        }
        return true;
    }

    private void flushScoreEvents() {
        if (score == null) return;
        int maxBySource = 0;
        for (int counter : killsBySource.values()) maxBySource = Math.max(maxBySource, counter);
        score.onTickKills(maxBySource, killLanes.size(), quickKills);
    }
    long tickSince(long stamp) {return tick - stamp;}

    /// status
    public boolean isGameover() { return gameover; }
    public boolean isWon() { return won; }
    public String getEndMessage() { return endMessage; }
    public int getKillsThisLevel() { return killsThisLevel; }
    /// quest get
    public int getSunBank() { return sunBank; }
    public Map<PlantType, Integer> getKillsByPlant() { return killsByPlant; }
    public Set<PlantFamily> getKillFamilies() { return killFamilies; }
    public Set<PlantFamily> getFamiliesPlanted() { return familiesPlanted; }
    public Set<Integer> getPlantedCols() { return plantedCols; }
    public Set<Integer> getPlantedRows() { return plantedRows; }
    public int getMowerKills() { return mowerKills; }
    public int getCol1KillsNoMower() { return col1KillsNoMower; }
    public int getFastKills() { return fastKills; }
    public int getExplosivesPlanted() { return explosivesPlanted; }
    public int getSunProducersPlanted() { return sunProducersPlanted; }
    public boolean isAllPlantedShrooms() { return allPlantedShrooms; }
    public int getPlantsLost() { return plantsLost; }
    /// /////
    public boolean isGardenSymmetric() {
        for (PlantedUnit unit : plants) {
            PlantedUnit mirror = findPlant(unit.col, ROWS + 1 - unit.row);
            if (mirror == null || mirror.getType() != unit.getType()) return false;
        }
        return !plants.isEmpty();
    }
    public boolean isGardenAsymmetric() {
        boolean anyOutsideMiddle = false;
        for (PlantedUnit unit : plants) {
            int mirrorRow = ROWS + 1 - unit.row;
            if (mirrorRow == unit.row) continue;
            anyOutsideMiddle = true;
            if (findPlant(unit.col, mirrorRow) != null) return false;
        }
        return anyOutsideMiddle;
    }
    public int getSunCollectedTotal() { return sunCollectedTotal; }
    public int getPlantsPlantedCount() { return plantsPlantedCount; }
    public int getCoinsEarned() { return coinsEarned; }
    public int getGemsEarned() { return gemsEarned; }
    public int getPlantFoodsLeft() { return plantFoods; }
    public int getMooPoints() { return score == null ? 0 : score.getMeowPoints(); }
    public LevelSpec getSpec() { return spec; }
    public List<PlantType> getConveyorBelt() { return conveyorBelt; }

    /// view port

    public String showMap() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Tick ").append(tick).append(" | Sun: ").append(sunBank).append(" | Wave: ").append(currentWave).append("/").append(spec.getWaveCount()).append(" | Plant food: ").append(plantFoods).append("\n");
        stringBuilder.append("    ");
        for (int x = 1; x <= COLS; x++) {stringBuilder.append(String.format(" %-4d", x));}
        stringBuilder.append("\n");
        for (int y = 1; y <= ROWS; y++) {
            stringBuilder.append(mowers[y] ? "[m] " : "    ").append(y).append(" ");
            for (int x = 1; x <= COLS; x++) {stringBuilder.append(String.format("%-5s", cellSymbol(x, y)));}
            stringBuilder.append("\n");
        }
        stringBuilder.append("Legend: P=plant Z=zombie *=sun T=tomb($=sun &=food) F=frozen ~=water N=necromancy /=slip\n");
        return stringBuilder.toString();
    }

    private String cellSymbol(int x, int y) {
        StringBuilder cell = new StringBuilder();
        PlantedUnit plant = findPlant(x, y);
        if (plant != null) cell.append('P');
        int zombieCount = 0;
        for (ActiveZombie activeZombie : zombies) {
            if (activeZombie.row == y && activeZombie.cellCol() == x) zombieCount++;
        }
        if (zombieCount > 0) {
            cell.append('Z');
            if (zombieCount > 1) cell.append(zombieCount);
        }
        for (int[] sun : groundSuns) {
            if (sun[0] == x && sun[1] == y) {
                cell.append('*');
                break;
            }
        }
        switch (terrain[y][x]) {
            case TOMB:
                cell.append(tombBonus[y][x] == 1 ? '$' : tombBonus[y][x] == 2 ? '&' : 'T');
                break;
            case FROZEN: cell.append('F'); break;
            case WATER: cell.append('~'); break;
            case NECROMANCY: cell.append('N'); break;
            case ICE_SLIP_UP:
            case ICE_SLIP_DOWN: cell.append('/'); break;
            default: break;
        }
        return cell.length() == 0 ? "." : cell.toString();
    }

    public String showPlantsStatus() {
        if (plants.isEmpty()) return "No plants on the lawn.";
        StringBuilder stringBuilder = new StringBuilder("Plants on the lawn:\n");
        for (PlantedUnit unit : plants) {
            stringBuilder.append("  ").append(unit.getType()).append(" at (").append(unit.col).append(", ").append(unit.row).append(") HP ").append(unit.plant.getHp()).append("/").append(unit.plant.getMaxHp());
            if (unit.frozen) stringBuilder.append(" [FROZEN]");
            if (unit.turnedIntoCat) stringBuilder.append(" [CAT!]");
            if (octopusBindings.containsValue(unit)) stringBuilder.append(" [BOUND]");
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String showTileStatus(int x, int y) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) return "Position (" + x + ", " + y + ") is outside the lawn.";
        StringBuilder stringBuilder = new StringBuilder("Tile (" + x + ", " + y + "): terrain " + terrain[y][x] + "\n");
        PlantedUnit unit = findPlant(x, y);
        if (unit != null) {
            stringBuilder.append("  Plant: ").append(unit.getType()).append(" HP ").append(unit.plant.getHp()).append("/").append(unit.plant.getMaxHp()).append("\n");
        }
        for (ActiveZombie activeZombie : zombies) {
            if (activeZombie.row == y && activeZombie.cellCol() == x) {
                stringBuilder.append("  Zombie: ").append(activeZombie.zombie.getType()).append(" HP ").append(activeZombie.zombie.getHp());
                if (activeZombie.zombie.getArmorHp() > 0) {
                    stringBuilder.append(" (+").append(activeZombie.zombie.getArmorHp()).append(" armor)");
                }
                stringBuilder.append("\n");
            }
        }
        for (int[] sun : groundSuns) {
            if (sun[0] == x && sun[1] == y) stringBuilder.append("  Sun worth ").append(sun[2]).append("\n");
        }
        return stringBuilder.toString();
    }
    ///
    public List<ActiveZombie> getZombiesOnLawn() {
        return java.util.Collections.unmodifiableList(zombies);
    }
}
