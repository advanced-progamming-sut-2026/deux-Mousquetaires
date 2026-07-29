package pvz.model.greenhouse;

import pvz.model.enums.PlantType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Greenhouse {

    public static final int COLUMNS = 5;
    public static final int ROWS = 4;
    private static final int[] ROW_UNLOCK_COST = {0, 1000, 2000, 4000};
    private int unlockedRows = 1;
    private final Map<String, Pot> pots = new LinkedHashMap<>();
    public boolean inBounds(int x, int y) {
        return x >= 1 && x <= COLUMNS && y >= 1 && y <= ROWS;
    }
    public boolean isRowUnlocked(int y) {
        return y >= 1 && y <= unlockedRows;
    }
    public int nextRowUnlockCost() {
        return unlockedRows >= ROWS ? -1 : ROW_UNLOCK_COST[unlockedRows];
    }
    public void unlockNextRow() {
        if (unlockedRows < ROWS) unlockedRows++;
    }

    public Pot getPot(int x, int y) {
        return pots.get(x + "," + y);
    }

    public boolean isOccupied(int x, int y) {
        return getPot(x, y) != null;
    }

    public void placePot(int x, int y, Pot pot) {
        pots.put(x + "," + y, pot);
    }

    public void removePot(int x, int y) {
        pots.remove(x + "," + y);
    }

    public Map<String, Pot> getPots() { return pots; }
    public int getUnlockedRows() { return unlockedRows; }

    public void setUnlockedRows(int rows) {
        unlockedRows = Math.max(1, Math.min(ROWS, rows));
    }
    public static PlantType rollPlant(List<PlantType> unlockedPlants, Random random) {
        if (unlockedPlants.isEmpty() || random.nextBoolean()) return PlantType.MARIGOLD;
        return unlockedPlants.get(random.nextInt(unlockedPlants.size()));
    }
}
