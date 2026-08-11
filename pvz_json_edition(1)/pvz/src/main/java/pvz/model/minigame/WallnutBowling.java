package pvz.model.minigame;

import pvz.model.enums.PlantType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WallnutBowling extends MiniGame {

    private static final int ROWS = 5;
    private static final int COLS = 9;

    ///zombie{row, col(x10 for sub-steps), hp}
    private final List<int[]> zombies = new ArrayList<>();
    private final List<PlantType> belt = new ArrayList<>();
    private int killTarget;
    private int kills;
    private int spawnTimer;
    private int beltTimer;
    public WallnutBowling(int level) {super(level);}

    @Override
    public void start() {
        killTarget = 10 + 5 * level;
        for (int i = 0; i < 3; i++) {refillBelt();}
        spawnZombie();
        spawnZombie();
        System.out.println("=== Wallnut Bowling (level " + level + ") ===");
        System.out.println("Roll nuts from behind the red line (column 3). Defeat " + killTarget + " zombies before any reach your house!");
        System.out.println("Commands: bowl <lane 1-5> | advance <n> | status | quit");
        printStatus();
    }

    @Override
    public void handle(String line) {
        String[] parts = line.trim().split("\\s+");
        switch (parts[0]) {
            case "bowl":
                if (parts.length < 2 || !parts[1].matches("[1-5]")) {
                    System.out.println("Usage: bowl <lane 1-5>");
                    return;}
                bowl(Integer.parseInt(parts[1]));

                return;

            case "advance":
                int steps = parts.length > 1 && parts[1].matches("\\d+")
                        ? Integer.parseInt(parts[1]) : 1;
                for (int i = 0; i < Math.min(steps, 100) && !gameover; i++) {
                    advanceOneSecond();
                }
                if (!gameover) printStatus();
                return;
            case "status":
                printStatus();
                return;
            case "quit":
                lose("You rolled away from the challenge.");
                return;
            default:
                System.out.println("Commands: bowl <lane> | advance <n> | status | quit");
        }
    }

    private void bowl(int lane) {
        if (belt.isEmpty()) {
            System.out.println("The conveyor belt is empty - advance time to get more nuts.");
            return;
        }
        PlantType nut = belt.remove(0);
        System.out.println("You roll a " + nut + " down lane " + lane + "!");
        switch (nut) {
            case EXPLODE_O_NUT:
                rollExplodeONut(lane);
                break;
            case GIANT_WALLNUT:
                rollGiantNut(lane);
                break;
            default:
                rollBouncingNut(lane);
                break;
        }
        checkWin();
    }

    /// normal nut
    private void rollBouncingNut(int lane) {
        int currentLane = lane;
        int hits = 0;
        for (int col = 3; col <= COLS && hits < 4; col++) {
            int[] victim = zombieAt(currentLane, col);
            if (victim != null) {
                hits++;
                kill(victim, "The nut smacks a zombie in lane " + currentLane + "!");
                int bounce = hits == 1 ? (random.nextBoolean() ? 1 : -1) : (currentLane <= 1 ? 1 : currentLane >= ROWS ? -1 : (random.nextBoolean() ? 1 : -1));
                currentLane = Math.max(1, Math.min(ROWS, currentLane + bounce));
            }
        }
        if (hits == 0) System.out.println("The nut rolls harmlessly off the lawn.");
    }

    private void rollExplodeONut(int lane) {
        for (int col = 3; col <= COLS; col++) {
            if (zombieAt(lane, col) != null) {
                System.out.println("BOOM! The Explode-o-nut detonates at column " + col + "!");
                for (Iterator<int[]> it = zombies.iterator(); it.hasNext(); ) {
                    int[] z = it.next();
                    if (Math.abs(z[0] - lane) <= 1 && Math.abs(z[1] / 10 - col) <= 1) {
                        it.remove();
                        kills++;
                        System.out.println("  A zombie is blown to bits!");}
                }
                return;
            }
        }
        System.out.println("The Explode-o-nut rolls off without a bang.");
    }

    private void rollGiantNut(int lane) {
        int flattened = 0;
        for (Iterator<int[]> it = zombies.iterator(); it.hasNext(); ) {
            int[] z = it.next();
            if (z[0] == lane && z[1] / 10 >= 3) {
                it.remove();
                kills++;
                flattened++;}
        }
        System.out.println("The Giant Wallnut flattens " + flattened + " zombie(s) in lane " + lane + "!");
    }

    private void advanceOneSecond() {
        beltTimer++;
        if (beltTimer >= 4) {
            beltTimer = 0;
            refillBelt();
        }
        spawnTimer++;
        if (spawnTimer >= Math.max(2, 6 - level)) {
            spawnTimer = 0;
            spawnZombie();
        }
        for (int[] z : zombies) {
            z[1] -= 5; // half a cell per second
            if (z[1] < 10) {
                lose("The zombie ate your brain; LOSER!!!");
                return;
            }
        }
    }

    private void spawnZombie() {
        int hp = random.nextInt(100) < 20 * level ? 2 : 1; // some need two hits
        zombies.add(new int[]{1 + random.nextInt(ROWS), COLS * 10, hp});
    }

    private void refillBelt() {
        if (belt.size() >= 5) return;
        int roll = random.nextInt(100);
        PlantType nut = roll < 70 ? PlantType.BOWLING_WALLNUT : roll < 90 ? PlantType.EXPLODE_O_NUT : PlantType.GIANT_WALLNUT;
        belt.add(nut);
        System.out.println("The conveyor belt delivers a " + nut + ".");
    }

    private int[] zombieAt(int lane, int col) {
        for (int[] z : zombies) {
            if (z[0] == lane && z[1] / 10 == col) return z;
        }
        return null;
    }

    private void kill(int[] zombie, String message) {
        zombie[2]--;
        if (zombie[2] <= 0) {
            zombies.remove(zombie);
            kills++;
            System.out.println(message);
        }
        else System.out.println("The nut staggers a tough zombie - one more hit!");
    }

    private void checkWin() {
        if (kills >= killTarget) {
            win("Strike! " + kills + " zombies bowled over - Wallnut Bowling level " + level + " complete!");
        }
    }

    private void printStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Kills: ").append(kills).append("/").append(killTarget).append(" | Belt: ").append(belt).append("\n");
        for (int lane = 1; lane <= ROWS; lane++) {
            stringBuilder.append("lane ").append(lane).append(" |");
            for (int col = 1; col <= COLS; col++) {
                stringBuilder.append(zombieAt(lane, col) != null ? " Z " : (col == 3 ? " ! " : " . "));
            }
            stringBuilder.append("\n");
        }
        stringBuilder.append("('!' marks the red line - nuts start at column 3)");
        System.out.println(stringBuilder);
    }
}
