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
            app.view.info("Commands: show greenhouse | plant pot at (x, y)" + " | collect (x, y) | grow (x, y) (1 gem per remaining hour)"
                    + " | unlock row | enter shop | menu exit");
            return;}

        if (line.equals("show greenhouse")) {
            show();
            return;}

        if (line.startsWith("plant pot at")) {
            int[] at = Args.location(line);
            if (at == null) {
                app.view.error("Usage: plant pot at (x, y)");
                return;}

            plantPot(at[0], at[1]);
            return;}

        if (line.startsWith("collect")) {
            int[] at = Args.location(line);
            if (at == null) {
                app.view.error("Usage: collect (x, y)");
                return;}

            collect(at[0], at[1]);
            return;}

        if (line.startsWith("grow")) {
            int[] at = Args.location(line);
            if (at == null) {
                app.view.error("Usage: grow (x, y)");
                return;}

            grow(at[0], at[1]);
            return;}

        if (line.equals("unlock row")) {
            unlockRow();
            return;}

        if (line.equals("enter shop")) {
            app.menuStack.push(pvz.model.enums.MenuContext.SHOP);
            app.view.menuPath("shop");
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void show() {app.view.showGreenhouse(app.currentUser);}

    private void plantPot(int x, int y) {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        if (!greenhouse.inBounds(x, y)) {
            app.view.error("The greenhouse grid is " + Greenhouse.COLUMNS + "x" + Greenhouse.ROWS + ".");
            return;}

        if (!greenhouse.isRowUnlocked(y)) {
            app.view.error("Row " + y + " is locked. Unlock it for " + greenhouse.nextRowUnlockCost() + " coins with 'unlock row'.");
            return;}

        if (greenhouse.isOccupied(x, y)) {
            app.view.error("There is already a pot at (" + x + ", " + y + ").");
            return;}

        if (!app.currentUser.usePot()) {
            app.view.error("You have no pots. Zombies sometimes drop them, or buy one in the shop.");
            return;}

        List<PlantType> unlocked = new ArrayList<>();
        for (Map.Entry<PlantType, Boolean> entry
                : app.currentUser.getCollection().getPlants().entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) unlocked.add(entry.getKey());
        }
        PlantType plant = Greenhouse.rollPlant(unlocked, random);
        greenhouse.placePot(x, y, new Pot(plant, LocalDateTime.now()));
        app.view.success("A " + plant + " sprout is planted at (" + x + ", " + y + "). It" + " will be ready in " + (plant == PlantType.MARIGOLD
                ? Pot.MARIGOLD_GROW_HOURS : Pot.OTHER_GROW_HOURS) + " hours.");
        app.saveAll();
    }

    private void collect(int x, int y) {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        Pot pot = greenhouse.getPot(x, y);
        if (pot == null) {
            app.view.error("There is no pot at (" + x + ", " + y + ").");
            return;}

        if (!pot.isReady()) {
            app.view.error("That plant needs " + pot.remainingMinutes() + " more minutes. Hurry it with 'grow (" + x + ", " + y + ")'.");
            return;}

        greenhouse.removePot(x, y);
        if (pot.getPlantType() == PlantType.MARIGOLD) {
            app.currentUser.addCoins(MARIGOLD_SELL_PRICE);
            app.view.success("You sold the Marigold for " + MARIGOLD_SELL_PRICE + " coins (now " + app.currentUser.getCoins() + ").");}
        else {
            app.currentUser.addStoredBoost(pot.getPlantType());
            app.view.success("You harvested a " + pot.getPlantType() + " boost! Use it for free in the pre-game plant selection.");}
        app.saveAll();
    }

    private void grow(int x, int y) {
        Pot pot = app.currentUser.getGreenhouse().getPot(x, y);
        if (pot == null) {
            app.view.error("There is no pot at (" + x + ", " + y + ").");
            return;}

        if (pot.isReady()) {
            app.view.error("That plant is already fully grown - collect it!");
            return;}

        int cost = pot.remainingHoursCeil();
        if (!app.currentUser.spendGems(cost)) {
            app.view.error("Hurrying this plant costs " + cost + " gems (1 per remaining hour);" + " you have " + app.currentUser.getGems() + ".");
            return;}

        pot.finishNow();
        app.view.success("You spent " + cost + " gems - the " + pot.getPlantType() + " at (" + x + ", " + y + ") is fully grown!");
        app.saveAll();
    }

    private void unlockRow() {
        Greenhouse greenhouse = app.currentUser.getGreenhouse();
        if (greenhouse.getUnlockedRows() >= Greenhouse.ROWS) {
            app.view.error("All greenhouse rows are already unlocked.");
            return;}

        int cost = greenhouse.nextRowUnlockCost();
        if (!app.currentUser.spendCoins(cost)) {
            app.view.error("Unlocking the next row costs " + cost + " coins; you have " + app.currentUser.getCoins() + ".");
            return;}

        greenhouse.unlockNextRow();
        app.view.success("Row " + greenhouse.getUnlockedRows() + " unlocked for " + cost + " coins!");
        app.saveAll();
    }
}
