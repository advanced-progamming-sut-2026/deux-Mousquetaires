package pvz.controller;

import pvz.model.entity.plant.PlantFactory;
import pvz.model.entity.zombie.ZombieFactory;
import pvz.model.enums.LevelType;
import pvz.model.enums.MenuContext;
import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;
import pvz.model.session.GameSession;
import pvz.model.session.LevelSpec;
import pvz.model.shop.QuestManager;

import java.util.Map;

class InGameController {

    private final GameApp app;
    private GameSession session;

    InGameController(GameApp app) {
        this.app = app;
    }

    void begin(GameSession session) {
        this.session = session;
        app.menuStack.push(MenuContext.IN_GAME);
    }

    void handle(String line) {
        if (session == null) {
            return;}

        if (line.equals("help")) {
            printHelp();
            return;}

        if (line.equals("menu exit")) {
            forfeit();
            return;}

        if (line.equals("menu show current")) {
            return;}

        route(line);
        if (session != null && session.isGameover()) {
            finish();}
    }

    private void printHelp() {
    }

    private void route(String line) {
        Args args = Args.parse(line);
        if (line.startsWith("advance time")) {
            Integer ticks = args.getInt("t");
            if (ticks == null || ticks <= 0) {
                return;}

            session.advanceTicks(Math.min(ticks, 100_000));
            return;}

        if (line.startsWith("collect sun")) {
            int[] at = Args.location(args.get("l", line));
            if (at == null) {
                return;}

            return;}

        if (line.equals("show sun amount")) {
            return;}

        if (line.startsWith("plant plant")) {
            PlantType type = args.get("t") == null ? null : PlantFactory.parseType(args.get("t"));
            int[] at = Args.location(args.get("l"));
            if (type == null || at == null) {
                return;}

            return;}

        if (line.startsWith("pluck plant")) {
            int[] at = Args.location(args.get("l", line));
            if (at == null) {
                return;}
            return;}

        if (line.startsWith("feed plant")) {
            int[] at = Args.location(args.get("l", line));
            if (at == null) {
                return;}
            return;}

        if (line.equals("show map")) {
            return;}

        if (line.equals("show plants status")) {
            return;}

        if (line.startsWith("show tile status")) {
            int[] at = Args.location(args.get("l", line));
            if (at == null) {
                return;}
            return;}

        if (line.equals("zombies info")) {
            return;}

        if (line.equals("show conveyor")) {
            return;}

        if (line.equals("start zombie waves")) {
            return;}

        if (line.startsWith("cheat add -n") && line.endsWith("suns")) {
            Integer count = args.getInt("n");
            if (count == null || count <= 0) {
                return;}
            session.cheatAddSuns(count);
            return;}

        if (line.equals("cheat add-plant-food")) {
            session.cheatAddPlantFood();
            return;}

        if (line.equals("cheat remove-cooldown")) {
            session.cheatRemoveCooldown();
            return;}

        if (line.startsWith("cheat spawn-zombie")) {
            ZombieType type = args.get("t") == null ? null : ZombieFactory.parseType(args.get("t"));
            int[] at = Args.location(args.get("l"));
            if (type == null || at == null) {
                return;}
            return;}

        if (line.equals("release the nuke")) {
            session.releaseNuke();
            return;}
    }

    void forfeit() {
        session = null;
        app.menuStack.pop();
    }

    ///ending
    private void finish() {
        GameSession finished = session;
        session = null;
        LevelSpec spec = finished.getSpec();
        int kills = finished.getKillsThisLevel();

        if (finished.isWon()) {
            int coins = 150 + 50 * spec.getWaveCount();
            app.currentUser.addCoins(coins);
            int gems = spec.getLevelType() == LevelType.NORMAL ? 1 : 2;
            app.currentUser.addGems(gems);
            PlantType reward = spec.getRewardPlant();
            if (reward != null && !app.currentUser.getCollection().isPlantUnlocked(reward)) {
                app.currentUser.getCollection().unlockPlant(reward);
                app.news.publishUnlock("You unlocked the plant " + reward + " by clearing level " + spec.getId() + "!");}

            if (!spec.isScored()) {
                boolean firstClear = !app.currentUser.getProgress().isLevelCleared(spec.getId());
                app.currentUser.getProgress().completeLevel(spec.getId());
                if (firstClear && spec.getId().endsWith("-4")) {
                    app.news.publish("Chapter complete!", "You finished chapter " + spec.getId().charAt(0) + "! A new world awaits.");
                }
            }
            app.quests.onEvent("level_completed", 1);
        }
        else {}

        if (spec.isScored()) {
            app.currentUser.recordGamePlayed(finished.getMooPoints());
        }

        //Quest
        app.quests.onEvent("zombie_killed", kills);
        app.quests.onEvent("sun_collected", finished.getSunCollectedTotal());
        app.quests.onEvent("plants_planted", finished.getPlantsPlantedCount());
        emitSheetQuestEvents(finished, app.quests, spec, kills);
        app.currentUser.recordZombiesKilled(kills);
        app.currentUser.setPlantFoods(finished.getPlantFoodsLeft());

        app.saveAll();
        app.menuStack.pop();
    }

    private static void emitSheetQuestEvents(GameSession finished, QuestManager quests,LevelSpec spec, int kills) {
        String id = spec.getId();
        if (!id.isEmpty() && Character.isDigit(id.charAt(0))) {
            quests.onEvent("chapter_kill:" + id.charAt(0), kills);}

        for (Map.Entry<PlantType, Integer> entry : finished.getKillsByPlant().entrySet()) {
            quests.onEvent("plant_kill:" + entry.getKey(), entry.getValue());}

        quests.onEvent("mower_kill", finished.getMowerKills());
        quests.onEvent("col1_kill_no_mower", finished.getCol1KillsNoMower());

        if (finished.getFastKills() >= 10) quests.onEvent("fast_kills_10", 10);
        if (finished.getExplosivesPlanted() >= 3) quests.onEvent("explosives_in_level", 3);
        if (!finished.isWon()) { quests.onEvent("level_failed", 1);
            return;}

        quests.onEvent("win_streak", 1);
        if (finished.getSunBank() == 0) quests.onEvent("win_zero_sun", 1);
        if (finished.isGardenSymmetric()) quests.onEvent("win_symmetric", 1);
        else if (finished.isGardenAsymmetric()) quests.onEvent("win_asymmetric", 1);
        if (finished.isAllPlantedShrooms() && finished.getPlantsPlantedCount() > 0) quests.onEvent("win_only_shrooms", 1);
        if (finished.getSunProducersPlanted() <= 3) quests.onEvent("win_max3_sun_producers", 1);
        for (int losses = 0; losses <= 5; losses++) {
            if (finished.getPlantsLost() <= losses) quests.onEvent("win_plant_losses_le:" + losses, 1);
        }
        for (int x = 1; x <= GameSession.COLS; x++) {
            if (!finished.getPlantedCols().contains(x)) quests.onEvent("win_empty_col:" + x, 1);
        }
        for (int y = 1; y <= GameSession.ROWS; y++) {
            if (!finished.getPlantedRows().contains(y)) quests.onEvent("win_empty_row:" + y, 1);
        }
        for (int k = 1; k <= GameSession.ROWS; k++) {
            if (!finished.getPlantedCols().contains(k) && !finished.getPlantedRows().contains(k)) {
                quests.onEvent("win_empty_cross:" + k, 1);}
        }
        for (PlantFamily family : PlantFamily.values()) {
            if (!finished.getFamiliesPlanted().contains(family)) {
                quests.onEvent("win_without_family:" + family, 1);}

            if (kills > 0 && finished.getKillFamilies().size() == 1 && finished.getKillFamilies().contains(family)) {
                quests.onEvent("win_only_family_kills:" + family, 1);}
        }
    }
}
