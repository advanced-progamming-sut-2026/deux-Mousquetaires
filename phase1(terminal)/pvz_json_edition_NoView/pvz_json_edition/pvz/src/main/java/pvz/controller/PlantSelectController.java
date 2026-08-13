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
        if (spec.isConveyor()) {
            return;}

        if (!spec.getLockedPlants().isEmpty()) {}

        if (!spec.isSunProducersAllowed()) {}

        if (spec.isPlantWhatYouGet()) { }

    }

    void handle(String line) {
        if (line.equals("help")) {
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
    }

    private boolean isAllowed(PlantType type) {
        return !pendingSpec.getLockedPlants().contains(type);
    }

    private void addPlant(String raw) {
        if (pendingSpec.isConveyor()) {
            return;}

        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            return;}

        if (!app.currentUser.getCollection().isPlantUnlocked(type)) {
            return;}

        if (!isAllowed(type)) {
            return;}

        if (chosen.contains(type)) {
            return;}

        if (chosen.size() >= MAX_SLOTS) {
            return;}

        chosen.add(type);
    }

    private void removePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null || !chosen.remove(type)) {
            return;}

        boosted.remove(type);
    }

    private void boostPlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            return;}

        if (!chosen.contains(type) && !pendingSpec.isConveyor()) {
            return;}

        if (boosted.contains(type)) {
            return;}

        if (app.currentUser.useStoredBoost(type)) {
            boosted.add(type);
            return;}

        if (!app.currentUser.spendGems(BOOST_PRICE_GEMS)) {
            return;}

        boosted.add(type);
    }

    private void startGame() {
        if (!pendingSpec.isConveyor() && chosen.isEmpty()) {
            return;
        }
        GameSession session = new GameSession(app.currentUser, pendingSpec, chosen, boosted, System.nanoTime());
        app.menuStack.pop();
        app.inGameController.begin(session);
    }
}
