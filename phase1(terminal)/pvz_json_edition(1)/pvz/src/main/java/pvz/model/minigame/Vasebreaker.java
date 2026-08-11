package pvz.model.minigame;

import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.List;

public class Vasebreaker extends MiniGame {

    private enum Content { ZOMBIE, GARGANTUAR, PLANT_PACKET, EMPTY }

    private static final int PACKET_LIFETIME = 3; // expires after 3 more breaks

    private final List<Content> vases = new ArrayList<>();
    private final List<int[]> packets = new ArrayList<>();
    private int houseHp;
    public Vasebreaker(int level) {super(level);}

    @Override
    public void start() {
        houseHp = 3;
        int count = 8 + 3 * level;
        for (int i = 0; i < count; i++) {
            int roll = random.nextInt(100);
            if (level >= 2 && roll < 8) vases.add(Content.GARGANTUAR);
            else if (roll < 55) vases.add(Content.ZOMBIE);
            else if (roll < 90) vases.add(Content.PLANT_PACKET);
            else vases.add(Content.EMPTY);
        }
        System.out.println("=== Vasebreaker (level " + level + ") ===");
        System.out.println(count + " vases stand before you. Zombies lurk inside some;" + " one-shot seed packets hide in others (they expire after "
                + PACKET_LIFETIME + " breaks).");
        System.out.println("Your house survives " + houseHp + " unblocked zombies.");
        System.out.println("Commands: break <n> | status | quit");
        printStatus();
    }

    @Override
    public void handle(String line) {
        String[] parts = line.trim().split("\\s+");
        switch (parts[0]) {
            case "break":
                if (parts.length < 2 || !parts[1].matches("\\d+")) {
                    System.out.println("Usage: break <vase-number>");
                    return;
                }
                breakVase(Integer.parseInt(parts[1]));
                return;
            case "status":
                printStatus();
                return;
            case "quit":
                lose("You abandoned the vases. The zombies celebrate quietly.");
                return;
            default:
                System.out.println("Commands: break <n> | status | quit");
        }
    }

    private void breakVase(int number) {
        if (number < 1 || number > vases.size()) {
            System.out.println("There are only " + vases.size() + " vases left. Pick 1.." + vases.size() + ".");
            return;
        }
        Content content = vases.remove(number - 1);
        agePackets();
        switch (content) {
            case EMPTY:
                System.out.println("The vase was empty. Phew!");
                break;
            case PLANT_PACKET:
                PlantType[] pool = {PlantType.PEASHOOTER, PlantType.SQUASH, PlantType.CHERRY_BOMB, PlantType.WALL_NUT};
                PlantType plant = pool[random.nextInt(pool.length)];
                packets.add(new int[]{PACKET_LIFETIME, plant.ordinal()});
                System.out.println("You found a one-shot " + plant + " seed packet!");
                break;
            case ZOMBIE:
                fight(ZombieType.BASIC, 1);
                break;
            case GARGANTUAR:
                fight(ZombieType.GARGANTUAR, 2);
                break;
        }
        if (gameover) return;
        if (vases.isEmpty()) win("All vases broken - the garden is safe! Vasebreaker level " + level + " complete!");
        else printStatus();
    }

    private void fight(ZombieType type, int packetsNeeded) {
        System.out.println("A " + type + " bursts out of the vase!");
        if (packets.size() >= packetsNeeded) {
            StringBuilder used = new StringBuilder();
            for (int i = 0; i < packetsNeeded; i++) {
                int[] packet = packets.remove(0);
                if (used.length() > 0) used.append(" and ");
                used.append(PlantType.values()[packet[1]]);
            }
            System.out.println("Your " + used + " takes it down!");
            return;
        }
        packets.clear();
        houseHp--;
        System.out.println("You had no defense ready - it shambles into the house! (" + houseHp + " HP left)");
        if (houseHp <= 0) lose("The zombie ate your brain; LOSER!!!");
    }

    private void agePackets() {
        packets.removeIf(packet -> --packet[0] < 0);
    }
    private void printStatus() {
        System.out.println("Vases left: " + vases.size() + " | House HP: " + houseHp + " | Packets held: " + packets.size());
    }
}
