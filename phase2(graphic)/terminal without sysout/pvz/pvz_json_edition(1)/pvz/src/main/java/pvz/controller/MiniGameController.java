package pvz.controller;

import pvz.model.enums.MenuContext;
import pvz.model.minigame.Beghouled;
import pvz.model.minigame.IZombie;
import pvz.model.minigame.MiniGame;
import pvz.model.minigame.Vasebreaker;
import pvz.model.minigame.WallnutBowling;
import pvz.model.minigame.Zombotany;

class MiniGameController {
    private final GameApp app;
    private MiniGame current;
    private String currentName;

    MiniGameController(GameApp app) {this.app = app;}
    void begin(String typeName, int level) {
        String key = typeName.toLowerCase().replace('_', '-');
        switch (key) {
            case "vasebreaker":
                current = new Vasebreaker(level);
                break;
            case "wallnut-bowling":
            case "bowling":
                current = new WallnutBowling(level);
                break;
            case "i-zombie":
            case "izombie":
                current = new IZombie(level);
                break;
            case "beghouled":
                current = new Beghouled(level);
                break;
            case "zombotany":
                current = new Zombotany(level);
                break;
            default:
                app.view.error("Unknown mini-game: " + typeName + ". Options: vasebreaker," + " wallnut-bowling, i-zombie, beghouled, zombotany");
                return;
        }
        currentName = key;
        app.menuStack.push(MenuContext.MINI_GAME);
        current.start();
    }

    void handle(String line) {
        if (current == null) {
            app.view.error("No mini-game is running.");
            return;}

        if (line.equals("menu exit")) {
            abort();
            return;}

        if (line.equals("menu show current")) {
            app.view.menuPath("mini-game (" + currentName + " level " + current.getLevel() + ")");
            return;}

        current.handle(line);
        if (current.isGameover()) finish();
    }

    void abort() {
        app.view.info("Mini-game abandoned.");
        current = null;
        app.menuStack.pop();
        app.view.menuPath(app.current().getDisplayName());}

    private void finish() {
        MiniGame finished = current;
        current = null;
        if (finished.isWon()) {
            int coins = 100 * finished.getLevel();
            app.currentUser.addCoins(coins);
            app.currentUser.recordMiniGameCompleted();
            app.currentUser.getProgress().unlockMiniGame(currentName);
            app.quests.onEvent("minigame_completed", 1);
            app.news.publish("Mini-game victory!", "You beat " + currentName + " level " + finished.getLevel() + " and earned " + coins + " coins!");
            app.view.success("Reward: " + coins + " coins (now " + app.currentUser.getCoins() + ").");
        }
        app.saveAll();
        app.menuStack.pop();
        app.view.menuPath(app.current().getDisplayName());
    }
}
