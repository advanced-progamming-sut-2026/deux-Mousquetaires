package pvz.controller;

import pvz.model.enums.MiniGameType;
import pvz.model.session.LevelSpec;

class GameMenuController {

    private final GameApp app;

    GameMenuController(GameApp app) {this.app = app;}

    void printOverview() {
        }

    void handle(String line) {
        if (line.equals("help")) {
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
            return;}

        if (line.equals("menu gem-wallet")) {
            return;}

        if (line.startsWith("menu cheat add")) {
            cheatAdd(line);
            return;}

    }

    private void showChapters() {
    }

    void enterChapter(String raw) {
        Args args = Args.parse("chapter " + raw);
        int chapter = parseChapter(args.get("c"));
        if (chapter < 0) {
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
            return;}

        String id = chapter + "-" + level;
        if (!app.currentUser.getProgress().isLevelUnlocked(id)) {
            return;}

        app.plantSelectController.begin(LevelSpec.adventure(chapter, level));
    }

    private void showMiniGames() {
        StringBuilder stringBuilder = new StringBuilder("Mini-games (each has 3 levels):\n");
        for (MiniGameType type : MiniGameType.values()) {
            stringBuilder.append("  ").append(type.name().toLowerCase()).append("\n");
        }
        stringBuilder.append("Play with: play minigame -t <type> -l <level 1-3>");
    }

    private void playMiniGame(String line) {
        Args args = Args.parse(line);
        String type = args.get("t");
        Integer level = args.getInt("l");
        if (type == null || level == null || level < 1 || level > 3) {
            return;}

        app.miniGameController.begin(type, level);
    }

    private void cheatAdd(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length != 5 || !parts[3].matches("\\d+")) {
            return;}

        int amount = Integer.parseInt(parts[3]);
        if (parts[4].startsWith("coin")) {
            app.currentUser.addCoins(amount);
            }
        else if (parts[4].startsWith("diamond") || parts[4].startsWith("gem")) {
            app.currentUser.addGems(amount);
            }

        else {}

    }
}
