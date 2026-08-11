package pvz.controller;

import pvz.model.enums.MiniGameType;
import pvz.model.session.LevelSpec;

class GameMenuController {

    private final GameApp app;

    GameMenuController(GameApp app) {this.app = app;}

    void printOverview() {
        app.view.info("Coins: " + app.currentUser.getCoins() + " | Gems: " + app.currentUser.getGems() + " | Levels cleared: " + app.currentUser.getProgress().getClearedLevels().size() + "/16. Type 'help' for commands.");}

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: show chapters | menu enter chapter -c <chapter>"
                    + " | start level -c <chapter> -l <level> | start scored game"
                    + "\n| show minigames | play minigame -t <type> -l <level>"
                    + " | menu coin-wallet | menu gem-wallet | menu cheat add <n> <coin|diamond>"
                    + "\n| menu enter collection|greenhouse|travel-log|leaderboard|shop"
                    + " (or menu greenhouse | menu travel-log | menu leaderboard) | menu exit");
            return;
        }
        if (line.equals("show chapters")) {
            showChapters();
            return;}

        if (line.startsWith("start level")) {
            startLevel(line);
            return;}

        if (line.equals("start scored game")) {
            app.plantSelectController.begin(LevelSpec.scoredGame());
            return;}

        if (line.equals("show minigames")) {
            showMiniGames();
            return;}

        if (line.startsWith("play minigame")) {
            playMiniGame(line);
            return;}

        if (line.equals("menu coin-wallet")) {
            app.view.info("Coin wallet: " + app.currentUser.getCoins() + " coins.");
            return;}

        if (line.equals("menu gem-wallet")) {
            app.view.info("Gem wallet: " + app.currentUser.getGems() + " gems.");
            return;}

        if (line.startsWith("menu cheat add")) {
            cheatAdd(line);
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void showChapters() {
        app.view.showChapters(app.currentUser);
    }

    void enterChapter(String raw) {
        Args args = Args.parse("chapter " + raw);
        int chapter = parseChapter(args.get("c"));
        if (chapter < 0) {
            app.view.error("Usage: menu enter chapter -c <1-4 | egypt | frostbite | beach | dark>");
            return;}

        for (int level = 1; level <= 4; level++) {
            String id = chapter + "-" + level;
            if (app.currentUser.getProgress().isLevelUnlocked(id) && !app.currentUser.getProgress().isLevelCleared(id)) {
                startLevel("start level -c " + chapter + " -l " + level);
                return;
            }
        }
        if (app.currentUser.getProgress().isLevelUnlocked(chapter + "-1")) {
            startLevel("start level -c " + chapter + " -l 1");
            return;
        }
        app.view.error("Chapter " + chapter + " is still locked. Clear the previous chapter first.");
    }

    private int parseChapter(String name) {
        if (name == null || name.isEmpty()) return -1;
        String key = name.trim().toLowerCase();
        if (key.matches("[1-4]")) return Integer.parseInt(key);
        if (key.contains("egypt")) return 1;
        if (key.contains("frost") || key.contains("cave") || key.contains("ice")) return 2;
        if (key.contains("beach") || key.contains("wave")) return 3;
        return key.contains("dark") || key.contains("age") ? 4 : -1;
    }

    private void startLevel(String line) {
        Args args = Args.parse(line);
        Integer chapter = args.getInt("c");
        Integer level = args.getInt("l");
        if (chapter == null || level == null || chapter < 1 || chapter > 4 || level < 1 || level > 4) {
            app.view.error("Usage: start level -c <chapter 1-4> -l <level 1-4>");
            return;}

        String id = chapter + "-" + level;
        if (!app.currentUser.getProgress().isLevelUnlocked(id)) {
            app.view.error("Level " + id + " is locked. Clear the previous level first.");
            return;}

        app.plantSelectController.begin(LevelSpec.adventure(chapter, level));
    }

    private void showMiniGames() {
        StringBuilder stringBuilder = new StringBuilder("Mini-games (each has 3 levels):\n");
        for (MiniGameType type : MiniGameType.values()) {
            stringBuilder.append("  ").append(type.name().toLowerCase()).append("\n");
        }
        stringBuilder.append("Play with: play minigame -t <type> -l <level 1-3>");
        app.view.info(stringBuilder.toString());
    }

    private void playMiniGame(String line) {
        Args args = Args.parse(line);
        String type = args.get("t");
        Integer level = args.getInt("l");
        if (type == null || level == null || level < 1 || level > 3) {
            app.view.error("Usage: play minigame -t <type> -l <level 1-3>");
            return;}

        app.miniGameController.begin(type, level);
    }

    private void cheatAdd(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 5 || !parts[3].matches("\\d+")) {
            app.view.error("Usage: menu cheat add <n> <coin|diamond>");
            return;}

        int amount = Integer.parseInt(parts[3]);
        if (parts[4].startsWith("coin")) {
            app.currentUser.addCoins(amount);
            app.view.success("Cheat: +" + amount + " coins (now " + app.currentUser.getCoins() + ").");}
        else if (parts[4].startsWith("diamond") || parts[4].startsWith("gem")) {
            app.currentUser.addGems(amount);
            app.view.success("Cheat: +" + amount + " diamonds (now " + app.currentUser.getGems() + ").");}

        else app.view.error("Usage: menu cheat add <n> <coin|diamond>");

    }
}
