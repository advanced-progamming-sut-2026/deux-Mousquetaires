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

    }

    private void showPlants(boolean all) {}

    private void showZombies(boolean all) {}

    private void showPlantDetails(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            return;}

    }

    private void showZombieDetails(String raw) {
        ZombieType type = raw == null ? null : ZombieFactory.parseType(raw);
        if (type == null) {
            return;}

        if (!app.currentUser.getCollection().isZombieUnlocked(type)) {
            return;}

    }

    private void upgradePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            return;}

        Collection collection = app.currentUser.getCollection();
        if (!collection.isPlantUnlocked(type)) {
            return;}

        int level = collection.getPlantLevel(type);

        if (level >= Collection.MAX_PLANT_LEVEL) {
            return;}

        int cost = 5 * level;

        if (app.currentUser.getSeedPackets(type) < cost) {
            return;}

        app.currentUser.spendSeedPackets(type, cost);
        collection.upgradePlant(type);
        app.saveAll();
    }

    private void purchasePlant(String raw) {
        PlantType type = raw == null ? null : PlantFactory.parseType(raw);
        if (type == null) {
            return;}

        if (app.currentUser.getCollection().isPlantUnlocked(type)) {
            return;}

        if (!app.currentUser.spendCoins(PLANT_PRICE)) {
            return;}

        app.currentUser.getCollection().unlockPlant(type);
        app.news.publishUnlock("You purchased the plant " + type + " for " + PLANT_PRICE + " coins!");
        app.saveAll();
    }
}
