package pvz.controller;

import pvz.model.auth.Collection;
import pvz.model.entity.plant.PlantFactory;
import pvz.model.entity.zombie.ZombieFactory;
import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;

class CollectionController {

    private static final int PLANT_PRICE = 2000;

    private final GameApp app;

    CollectionController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: show-plants | show-all-plants | show-zombies" + " | show-all-zombies | show-plant -p <type> | show-zombie -z <type>" + " | upgrade-plant -p <type> | purchase-plant -p <type> | menu exit");
            return;
        }
        switch (line) {
            case "show-plants":
                showPlants(false);
                return;
            case "show-all-plants":
                showPlants(true);
                return;
            case "show-zombies":
                showZombies(false);
                return;
            case "show-all-zombies":
                showZombies(true);
                return;
            default:}

        Args args = Args.parse(line);
        if (line.startsWith("show-plant ")) {
            showPlantDetails(args.get("p"));
            return;}

        if (line.startsWith("show-zombie ")) {
            showZombieDetails(args.get("z"));
            return;}

        if (line.startsWith("upgrade-plant")) {
            upgradePlant(args.get("p"));
            return;}

        if (line.startsWith("purchase-plant")) {
            purchasePlant(args.get("p"));
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void showPlants(boolean all) {app.view.showPlantList(app.currentUser, all);}

    private void showZombies(boolean all) {app.view.showZombieList(app.currentUser, all);}

    private void showPlantDetails(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown plant type: " + raw);
            return;}

        app.view.showPlantDetails(app.currentUser, type);
    }

    private void showZombieDetails(String raw) {
        ZombieType type = raw == null ? null : ZombieFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown zombie type: " + raw);
            return;}

        if (!app.currentUser.getCollection().isZombieUnlocked(type)) {
            app.view.error("You have not encountered " + type + " yet - its secrets stay hidden.");
            return;}

        app.view.showZombieDetails(type);
    }

    private void upgradePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown plant type: " + raw);
            return;}

        Collection collection = app.currentUser.getCollection();
        if (!collection.isPlantUnlocked(type)) {
            app.view.error("You have not unlocked " + type + " yet.");
            return;}

        int level = collection.getPlantLevel(type);

        if (level >= Collection.MAX_PLANT_LEVEL) {
            app.view.error(type + " is already at the maximum level (" + Collection.MAX_PLANT_LEVEL + ").");
            return;}

        int cost = 5 * level;

        if (app.currentUser.getSeedPackets(type) < cost) {
            app.view.error("Upgrading " + type + " to level " + (level + 1) + " needs " + cost + " seed packets; you have " + app.currentUser.getSeedPackets(type) + ".");
            return;}

        app.currentUser.spendSeedPackets(type, cost);
        collection.upgradePlant(type);
        app.view.success(type + " upgraded to level " + collection.getPlantLevel(type) + " (damage x" + String.format("%.2f", 1 + 0.25 * (collection.getPlantLevel(type) - 1)) + ").");
        app.saveAll();
    }

    private void purchasePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            app.view.error("Unknown plant type: " + raw);
            return;}

        if (app.currentUser.getCollection().isPlantUnlocked(type)) {
            app.view.error("You already own " + type + ".");
            return;}

        if (!app.currentUser.spendCoins(PLANT_PRICE)) {
            app.view.error("Purchasing a plant costs " + PLANT_PRICE + " coins; you have " + app.currentUser.getCoins() + ".");
            return;}

        app.currentUser.getCollection().unlockPlant(type);
        app.news.publishUnlock("You purchased the plant " + type + " for " + PLANT_PRICE + " coins!");
        app.view.success(type + " unlocked! Coins left: " + app.currentUser.getCoins());
        app.saveAll();
    }
}
