package pvz.model.minigame;

import pvz.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Zombotany extends MiniGame {

    private static final int ROWS = 5;
    private static final int COLS = 9;
    /// plants {lane, col, hp, dps, sunProducer(0/1)}
    private final List<int[]> plants = new ArrayList<>();
    /// zombies {lane, col(x10), hp, typeOrdinal, usedAbility}
    private final List<int[]> zombies = new ArrayList<>();
    private final boolean[] mowers = new boolean[ROWS + 1];
    private int sun;
    private int kills;
    private int killTarget;
    private int spawnTimer;
    private int sunTimer;

    public Zombotany(int level) {super(level);}

    @Override
    public void start() {
        sun = 150;
        killTarget = 8 + 4 * level;
        for (int lane = 1; lane <= ROWS; lane++) {mowers[lane] = true;}
        spawnZombie();
        System.out.println("=== Zombotany (level " + level + ") ===");
        System.out.println("Plant-headed zombies are coming! Defeat " + killTarget + " of them.");
        System.out.println("Plants: peashooter (100 sun), sunflower (50 sun), wallnut (50 sun)");
        System.out.println("Commands: plant <type> (x, y) | advance <n> | map | status | quit");
        printMap();
    }

    @Override
    public void handle(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("plant")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("plant\\s+(\\w+)\\s*\\((\\d+)\\s*,\\s*(\\d+)\\)")
                    .matcher(trimmed);
            if (!m.matches()) {
                System.out.println("Usage: plant <peashooter|sunflower|wallnut> (x, y)");
                return;
            }
            plant(m.group(1).toLowerCase(), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
            return;
        }
        String[] parts = trimmed.split("\\s+");
        switch (parts[0]) {
            case "advance":
                int steps = parts.length > 1 && parts[1].matches("\\d+") ? Integer.parseInt(parts[1]) : 1;
                for (int i = 0; i < Math.min(steps, 200) && !gameover; i++) {
                    advanceOneSecond();
                }
                if (!gameover) printMap();
                return;
            case "map":
                printMap();
                return;
            case "status":
                System.out.println("Sun: " + sun + " | Kills: " + kills + "/" + killTarget);
                return;
            case "quit":
                lose("You fled from the plant-headed horde.");
                return;
            default:
                System.out.println("Commands: plant <type> (x, y) | advance <n> | map | status | quit");
        }
    }

    private void plant(String type, int x, int y) {
        if (x < 1 || x > COLS || y < 1 || y > ROWS) {
            System.out.println("Position (" + x + ", " + y + ") is outside the lawn.");
            return;
        }
        if (plantAt(y, x) != null) {
            System.out.println("There is already a plant at (" + x + ", " + y + ").");
            return;
        }
        int cost;
        int[] unit;
        switch (type) {
            case "peashooter":
                cost = 100;
                unit = new int[]{y, x, 300, 20, 0};
                break;
            case "sunflower":
                cost = 50;
                unit = new int[]{y, x, 300, 0, 1};
                break;
            case "wallnut":
                cost = 50;
                unit = new int[]{y, x, 4000, 0, 0};
                break;
            default:
                System.out.println("Unknown plant: " + type);
                return;
        }
        if (sun < cost) {
            System.out.println("Not enough sun (" + sun + "/" + cost + ").");
            return;
        }
        sun -= cost;
        plants.add(unit);
        System.out.println("Planted " + type + " at (" + x + ", " + y + "). Sun left: " + sun);
    }

    private void advanceOneSecond() {
        /// sun incoming
        sunTimer++;
        if (sunTimer >= 8) {
            sunTimer = 0;
            sun += 25;}
        for (int[] plant : plants) {
            if (plant[4] == 1 && random.nextInt(100) < 12) sun += 25;
        }
        /// spawns
        spawnTimer++;
        if (spawnTimer >= Math.max(3, 7 - level)) {
            spawnTimer = 0;
            spawnZombie();}

        /// plant shoot the closest zombie in lane
        for (int[] plant : plants) {
            if (plant[3] <= 0) continue;
            int[] target = null;
            for (int[] z : zombies) {
                if (z[0] == plant[0] && z[1] / 10 >= plant[1] && (target == null || z[1] < target[1])) {
                    target = z;
                }
            }
            if (target != null) target[2] -= plant[3];
        }
        /// remove dead zombie(dead status)
        for (Iterator<int[]> it = zombies.iterator(); it.hasNext(); ) {
            int[] z = it.next();
            if (z[2] <= 0) {
                it.remove();
                kills++;
                System.out.println("Zombie of type " + ZombieType.values()[z[3]] + " is dead at (" + z[1] / 10 + ", " + z[0] + ")");
            }
        }
        /// zombie acting
        for (int[] z : new ArrayList<>(zombies)) {
            if (!zombies.contains(z)) continue;
            actZombie(z);
            if (gameover) return;
        }
        if (kills >= killTarget) {
            win("You defeated " + kills + " plant-headed zombies - Zombotany level " + level + " complete!");
        }
    }

    private void actZombie(int[] z) {
        ZombieType type = ZombieType.values()[z[3]];
        int cell = z[1] / 10;
        int[] blocking = plantAt(z[0], cell);
        /// peashooter zombies stop and shoot the plant in lane.
        if (type == ZombieType.PEASHOOTER_ZOMBIE) {
            int[] target = null;
            for (int[] plant : plants) {
                if (plant[0] == z[0] && plant[1] * 10 <= z[1] && (target == null || plant[1] > target[1])) {
                    target = plant;
                }
            }
            if (target != null) {
                target[2] -= 20;
                if (target[2] <= 0) {
                    plants.remove(target);
                    System.out.println("A Peashooter zombie shot down your plant at (" + target[1] + ", " + target[0] + ")!");
                }
                return;
            }
        }
        if (blocking != null) {
            if (type == ZombieType.JALAPENO_ZOMBIE) {
                System.out.println("The Jalapeno zombie explodes, scorching lane " + z[0] + "!");
                plants.removeIf(plant -> plant[0] == z[0]);
                zombies.remove(z);
                kills++;
                return;
            }
            if (type == ZombieType.SQUASH_ZOMBIE && z[4] == 0) {
                z[4] = 1;
                plants.remove(blocking);
                System.out.println("The Squash zombie crushes your plant at (" + blocking[1] + ", " + blocking[0] + ")!");
                return;
            }
            blocking[2] -= 100;
            if (blocking[2] <= 0) {
                plants.remove(blocking);
                System.out.println("Plant at (" + blocking[1] + ", " + blocking[0] + ") is destroyed.");
            }
            return;
        }
        z[1] -= type == ZombieType.SQUASH_ZOMBIE ? 7 : 5;
        if (z[1] < 10) {
            if (mowers[z[0]]) {
                mowers[z[0]] = false;
                int lane = z[0];
                System.out.println("The lawn mower in the row " + lane + " is triggered and killed these zombies:");
                for (Iterator<int[]> it = zombies.iterator(); it.hasNext(); ) {
                    int[] victim = it.next();
                    if (victim[0] == lane) {
                        System.out.println("  - " + ZombieType.values()[victim[3]]);
                        it.remove();
                        kills++;
                    }
                }
            }
            else lose("The zombie ate your brain; LOSER!!!");
        }
    }

    private void spawnZombie() {
        ZombieType[] pool = {ZombieType.PEASHOOTER_ZOMBIE, ZombieType.WALLNUT_ZOMBIE,
                ZombieType.JALAPENO_ZOMBIE, ZombieType.SQUASH_ZOMBIE};
        ZombieType type = pool[random.nextInt(pool.length)];
        int hp = type == ZombieType.WALLNUT_ZOMBIE ? 1100 : 250;
        zombies.add(new int[]{1 + random.nextInt(ROWS), COLS * 10, hp, type.ordinal(), 0});
        System.out.println("A " + type + " shambles onto the lawn!");
    }

    private int[] plantAt(int lane, int column) {
        for (int[] plant : plants) {
            if (plant[0] == lane && plant[1] == column) return plant;}
        return null;
    }

    private void printMap() {
        StringBuilder stringBuilder = new StringBuilder("Sun: " + sun + " | Kills: " + kills + "/" + killTarget + "\n");
        for (int lane = 1; lane <= ROWS; lane++) {
            stringBuilder.append(mowers[lane] ? "[m] " : "    ").append(lane).append(" |");
            for (int col = 1; col <= COLS; col++) {
                boolean hasPlant = plantAt(lane, col) != null;
                boolean hasZombie = false;
                for (int[] z : zombies) {
                    if (z[0] == lane && z[1] / 10 == col) {
                        hasZombie = true;
                        break;
                    }
                }
                stringBuilder.append(hasPlant && hasZombie ? "PZ " : hasPlant ? " P " : hasZombie ? " Z " : " . ");
            }
            stringBuilder.append("\n");
        }
        System.out.println(stringBuilder);
    }
}
