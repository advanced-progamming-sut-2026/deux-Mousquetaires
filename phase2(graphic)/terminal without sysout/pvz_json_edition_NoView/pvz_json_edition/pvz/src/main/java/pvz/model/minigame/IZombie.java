package pvz.model.minigame;

import pvz.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IZombie extends MiniGame {

    private static final int ROWS = 5;
    private static final int COLS = 9;
    private static final int SUN_PRODUCER = -1;
    private static final int SUN_PRODUCER_HP = 700;

    private static final Object[][][] ROSTERS = {
            {
                    {ZombieType.IMP, 25, 100, 60},
                    {ZombieType.BASIC, 50, 200, 100},
                    {ZombieType.CONEHEAD, 75, 400, 100},
                    {ZombieType.NEWSPAPER, 100, 350, 150},
                    {ZombieType.BUCKETHEAD, 125, 700, 100},
            },
            {
                    {ZombieType.IMP_DRAGON, 40, 120, 80},
                    {ZombieType.BASIC, 50, 200, 100},
                    {ZombieType.PROSPECTOR, 100, 450, 120},
                    {ZombieType.ALL_STAR, 150, 600, 150},
                    {ZombieType.KNIGHT, 200, 900, 100},
            },
            {
                    {ZombieType.HUNTER, 100, 400, 100},
                    {ZombieType.JESTER, 125, 500, 110},
                    {ZombieType.PIANIST, 175, 800, 130},
                    {ZombieType.BLOCKHEAD, 250, 1100, 100},
                    {ZombieType.GARGANTUAR, 400, 1800, 300},
            },
    };

    ///{lane, col, hp, dps}--->plant
    private final List<int[]> plants = new ArrayList<>();
    ///{lane, col(x10), hp, dps, rosterIndex or SUN_PRODUCER}--->zombie
    private final List<int[]> myZombies = new ArrayList<>();
    private final boolean[] brainEaten = new boolean[ROWS + 1];
    private final Object[][] roster;
    private int sun;
    private int zombiesPlaced;
    private int maxZombies;
    private int secondsElapsed;

    public IZombie(int level) {
        super(level);
        this.roster = ROSTERS[Math.max(1, Math.min(3, level)) - 1];
    }

    @Override
    public void start() {
        sun = 150;
        maxZombies = 5 + level * 2;
        for (int lane = 1; lane <= ROWS; lane++) {
            int defenders = 1 + random.nextInt(1 + level);
            for (int i = 0; i < defenders; i++) {
                int col = 2 + random.nextInt(4);
                boolean shooter = random.nextBoolean();
                plants.add(new int[]{lane, col, shooter ? 300 : 1000, shooter ? 20 : 0});
            }
            myZombies.add(new int[]{lane, COLS * 10, SUN_PRODUCER_HP, 60, SUN_PRODUCER});
        }
        for (Object[] entry : roster) {
        }
        printStatus();
    }

    @Override
    public void handle(String line) {
        String[] parts = line.trim().split("\\s+");
        switch (parts[0]) {
            case "place":
                if (parts.length < 3 || !parts[2].matches("[1-5]")) {
                    return;}

                place(parts[1], Integer.parseInt(parts[2]));
                return;
            case "advance":
                int steps = parts.length > 1 && parts[1].matches("\\d+") ? Integer.parseInt(parts[1]) : 1;
                for (int i = 0; i < Math.min(steps, 200) && !gameover; i++) {advanceOneSecond();}
                if (!gameover) printStatus();
                return;
            case "status":
                printStatus();
                return;
            case "quit":
                lose("You surrendered your own horde.");
                return;
            default:
        }
    }

    private void place(String typeName, int lane) {
        if (typeName.toLowerCase().contains("sun")) {
            return;}

        int index = -1;
        for (int i = 0; i < roster.length; i++) {
            if (roster[i][0].toString().equalsIgnoreCase(typeName)) {
                index = i;
                break;}
        }
        if (index < 0) {
            return;}

        if (zombiesPlaced >= maxZombies) {
            return;}

        int cost = (int) roster[index][1];
        if (sun < cost) {
            return;}

        sun -= cost;
        zombiesPlaced++;
        myZombies.add(new int[]{lane, COLS * 10, (int) roster[index][2], (int) roster[index][3], index});
    }

    private void advanceOneSecond() {
        secondsElapsed++;
        for (Iterator<int[]> it = myZombies.iterator(); it.hasNext(); ) {
            int[] z = it.next();
            if (z[4] == SUN_PRODUCER) sun += 3 + secondsElapsed / 10;
            int[] blocking = plantAt(z[0], z[1] / 10 - 1 >= 1 ? nearestPlantCol(z) : -1);
            if (blocking != null) {
                blocking[2] -= z[3];
                if (blocking[2] <= 0) {
                    plants.remove(blocking);
                    }
            }
            else z[1] -= 5;
            /// shooter plants fire
            for (int[] plant : plants) {
                if (plant[0] == z[0] && plant[3] > 0 && plant[1] * 10 <= z[1]) z[2] -= plant[3];
            }
            if (z[2] <= 0) {
                it.remove();
                continue;}

            if (z[1] <= 10 && !brainEaten[z[0]]) {
                brainEaten[z[0]] = true;
                }
        }
        checkOutcome();
    }

    private int nearestPlantCol(int[] z) {
        int zombieCol = z[1] / 10;
        for (int[] plant : plants) {
            if (plant[0] == z[0] && plant[1] == zombieCol) return plant[1];
        }
        return -1;
    }

    private int[] plantAt(int lane, int column) {
        if (column < 0) return null;
        for (int[] plant : plants) {
            if (plant[0] == lane && plant[1] == column) return plant;
        }
        return null;
    }

    private void checkOutcome() {
        boolean allEaten = true;
        for (int lane = 1; lane <= ROWS; lane++) {
            if (!brainEaten[lane]) {
                allEaten = false;
                break;
            }
        }
        if (allEaten) {
            win("All five brains devoured - I, Zombie level " + level + " complete!");
            return;
        }
        boolean canStillPlace = zombiesPlaced < maxZombies && sun >= minRosterCost();
        if (myZombies.isEmpty() && !canStillPlace) lose("Your horde is spent and brains remain uneaten. Defeat!");
    }

    private int minRosterCost() {
        int min = Integer.MAX_VALUE;
        for (Object[] entry : roster) {
            min = Math.min(min, (int) entry[1]);
        }
        return min;
    }

    private void printStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sun: ").append(sun).append(" | Zombies placed: ").append(zombiesPlaced)
                .append("/").append(maxZombies).append("\n");
        for (int lane = 1; lane <= ROWS; lane++) {
            stringBuilder.append(brainEaten[lane] ? "( ) " : "(B) ").append("lane ").append(lane).append(" |");
            for (int column = 1; column <= COLS; column++) {
                boolean hasPlant = plantAt(lane, column) != null;
                boolean hasZombie = false;
                for (int[] z : myZombies) {
                    if (z[0] == lane && z[1] / 10 == column) {
                        hasZombie = true;
                        break;
                    }
                }
                stringBuilder.append(hasPlant && hasZombie ? "PZ " : hasPlant ? " P " : hasZombie ? " Z " : " . ");
            }
            stringBuilder.append("\n");
        }
    }
}
