package pvz.controller;

import pvz.model.entity.plant.PlantFactory;
import pvz.model.enums.PlantType;
import pvz.model.greenhouse.Greenhouse;
import pvz.model.greenhouse.Pot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

class GreenhouseController {

    private static final int MARIGOLD_SELL_PRICE = 500;
    private final GameApp app;
    private final Random random = new Random();
    GreenhouseController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            return;}

        if (line.equals("show greenhouse")) {
            show();
            return;}

        if (line.startsWith("plant pot at")) {
            int[] at = Args.location(line);
            if (at == null) {
                return;}

            plantPot(at[0], at[1]);
            return;}

        if (line.startsWith("collect")) {
            int[] at = Args.location(line);
            if (at == null) {
                return;}

            collect(at[0], at[1]);
            return;}

        if (line.startsWith("grow")) {
            int[] at = Args.location(line);
            if (at == null) {
                return;}

            grow(at[0], at[1]);
            return;}

        if (line.equals("unlock row")) {
            unlockRow();
            return;}

        if (line.equals("enter shop")) {
            app.menuStack.push(pvz.model.enums.MenuContext.SHOP);
            return;}

    }

    private void show() {}

    private void plantPot(int x, int y) {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        if (!greenhouse.inBounds(x, y)) {
            return;}

        if (!greenhouse.isRowUnlocked(y)) {
            return;}

        if (greenhouse.isOccupied(x, y)) {
            return;}

        if (!app.currentUser.usePot()) {
            return;}

        List<PlantType> unlocked = new ArrayList<>();
        for (Map.Entry<PlantType, Boolean> entry
                : app.currentUser.getCollection().getPlants().entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) unlocked.add(entry.getKey());
        }
        PlantType plant = Greenhouse.rollPlant(unlocked, random);
        greenhouse.placePot(x, y, new Pot(plant, LocalDateTime.now()));
        app.saveAll();
    }

    private void collect(int x, int y) {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        Pot pot = greenhouse.getPot(x, y);
        if (pot == null) {
            return;}

        if (!pot.isReady()) {
            return;}

        greenhouse.removePot(x, y);
        if (pot.getPlantType() == PlantType.MARIGOLD) {
            app.currentUser.addCoins(MARIGOLD_SELL_PRICE);
            }
        else {
            app.currentUser.addStoredBoost(pot.getPlantType());
            }
        app.saveAll();
    }

    private void grow(int x, int y) {
        Pot pot = app.currentUser.getGreenhouse().getPot(x, y);
        if (pot == null) {
            return;}

        if (pot.isReady()) {
            return;}

        int cost = pot.remainingHoursCeil();
        if (!app.currentUser.spendGems(cost)) {
            return;}

        pot.finishNow();
        app.saveAll();
    }

    private void unlockRow() {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        if (greenhouse.getUnlockedRows() >= Greenhouse.ROWS) {
            return;}

        int cost = greenhouse.nextRowUnlockCost();
        if (!app.currentUser.spendCoins(cost)) {
            return;}

        greenhouse.unlockNextRow();
        app.saveAll();
    }
}
