package pvz.controller;

import pvz.model.session.LevelSpec;

class MainMenuController {
    private final GameApp app;
    MainMenuController(GameApp app) {this.app = app;}

    void handle(String line) {
        switch (line) {
            case "help":
                if (app.news.hasUnread()) {
                    }
                return;

            case "start scored game":
                app.plantSelectController.begin(LevelSpec.scoredGame());
                return;
            case "whoami":
                return;
            default:
        }
    }
}
