package pvz.controller;

import pvz.model.entity.plant.PlantCatalog;
import pvz.model.entity.plant.PlantFactory;
import pvz.model.enums.MenuContext;
import pvz.model.enums.PlantType;
import pvz.model.session.GameSession;
import pvz.model.session.LevelSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PlantSelectController {

    static final int MAX_SLOTS = 8;
    private static final int BOOST_PRICE_GEMS = 2;
    private final GameApp app;
    private LevelSpec pendingSpec;
    private final List<PlantType> chosen = new ArrayList<>();
    private final Set<PlantType> boosted = new HashSet<>();

    PlantSelectController(GameApp app) {this.app = app;}

    void begin(LevelSpec spec) {
        pendingSpec = spec;
        chosen.clear();
        boosted.clear();
        app.menuStack.push(MenuContext.PLANT_SELECT);
        app.view.info("Level " + spec.getId() + " (" + spec.getWorld() + ", " + spec.getLevelType() + ") - " + spec.getWaveCount() + " waves.");
        if (spec.isConveyor()) {
            app.view.info("This is a conveyor-belt level: the belt hands you plants," + " planting is free, and there is no seed selection. Type 'start game'.");
            return;}

        if (!spec.getLockedPlants().isEmpty()) app.view.info("Locked for this level: " + spec.getLockedPlants());

        if (!spec.isSunProducersAllowed()) app.view.info("Night ops: sun-producing plants are not allowed here.");

        if (spec.isPlantWhatYouGet()) { app.view.info("Plant What You Get: you start with " + spec.getInitialSun()
                    + " sun, planting is free until you type 'start zombie waves' in game.");}

        app.view.info("Pick up to " + MAX_SLOTS + " plants: add plant -t <type>." + " Type 'help' for all commands.");
    }

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: show all plants | show available plants" + " | add plant -t <type> | remove plant -t <type>"
                    + " | boost plant -t <type> (" + BOOST_PRICE_GEMS + " gems or a stored boost)" + " | start game | menu exit");
            return;}

        if (line.equals("show all plants")) {
            showPlants(true);
            return;}

        if (line.equals("show available plants")) {
            showPlants(false);
            return;}

        if (line.startsWith("add plant")) {
            addPlant(Args.parse(line).get("t"));
            return;}

        if (line.startsWith("remove plant")) {
            removePlant(Args.parse(line).get("t"));
            return;}

        if (line.startsWith("boost plant")) {
            boostPlant(Args.parse(line).get("t"));
            return;}

        if (line.equals("start game")) {
            startGame();
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void showPlants(boolean all) {
        StringBuilder stringBuilder = new StringBuilder(all ? "All plants:\n" : "Available for this level:\n");
        for (PlantType type : PlantType.values()) {
            boolean unlocked = app.currentUser.getCollection().isPlantUnlocked(type);
            boolean available = unlocked && isAllowed(type);
            if (!all && !available) continue;

            PlantCatalog.Stats stats = PlantCatalog.of(type);
            stringBuilder.append("  ").append(type).append(" (").append(stats.sunCost).append(" sun)");
            if (!unlocked) stringBuilder.append(" [locked in collection]");
            else if (!isAllowed(type)) stringBuilder.append(" [locked for this level]");
            if (chosen.contains(type)) stringBuilder.append(" [CHOSEN]");
            if (boosted.contains(type)) stringBuilder.append(" [BOOSTED]");
            stringBuilder.append("\n");
        }
        app.view.info(stringBuilder.toString().stripTrailing());
    }

    private boolean isAllowed(PlantType type) {
        return !pendingSpec.getLockedPlants().contains(type);
    }

    private void addPlant(String raw) {
        if (pendingSpec.isConveyor()) {
            app.view.error("Conveyor-belt levels have no seed selection.");
            return;}

        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown plant type: " + raw);
            return;}

        if (!app.currentUser.getCollection().isPlantUnlocked(type)) {
            app.view.error("You have not unlocked " + type + " yet.");
            return;}

        if (!isAllowed(type)) {
            app.view.error(type + " is locked for this level.");
            return;}

        if (chosen.contains(type)) {
            app.view.error(type + " is already on your seed bar.");
            return;}

        if (chosen.size() >= MAX_SLOTS) {
            app.view.error("Your seed bar is full (" + MAX_SLOTS + " slots).");
            return;}

        chosen.add(type);
        app.view.success(type + " added (" + chosen.size() + "/" + MAX_SLOTS + ").");
    }

    private void removePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null || !chosen.remove(type)) {
            app.view.error("That plant is not on your seed bar.");
            return;}

        boosted.remove(type);
        app.view.success(type + " removed.");
    }

    private void boostPlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown plant type: " + raw);
            return;}

        if (!chosen.contains(type) && !pendingSpec.isConveyor()) {
            app.view.error("Add " + type + " to your seed bar first.");
            return;}

        if (boosted.contains(type)) {
            app.view.error(type + " is already boosted.");
            return;}

        if (app.currentUser.useStoredBoost(type)) {
            boosted.add(type);
            app.view.success(type + " boosted using your greenhouse boost (free)!");
            return;}

        if (!app.currentUser.spendGems(BOOST_PRICE_GEMS)) {
            app.view.error("Boosting costs " + BOOST_PRICE_GEMS + " gems; you have " + app.currentUser.getGems() + ".");
            return;}

        boosted.add(type);
        app.view.success(type + " boosted for " + BOOST_PRICE_GEMS + " gems. It gets a free plant-food effect when planted!");
    }

    private void startGame() {
        if (!pendingSpec.isConveyor() && chosen.isEmpty()) {
            app.view.error("Pick at least one plant first.");
            return;
        }
        GameSession session = new GameSession(app.currentUser, pendingSpec, chosen, boosted, System.nanoTime());
        app.menuStack.pop();
        app.inGameController.begin(session);
    }
}
