package pvz.controller;

import pvz.model.session.LevelSpec;

class MainMenuController {
    private final GameApp app;
    MainMenuController(GameApp app) {this.app = app;}

    void handle(String line) {
        switch (line) {
            case "help":
                app.view.info("Commands: menu enter game | menu enter settings"
                        + " | menu enter news | menu enter profile | menu enter leaderboard"
                        + " | start scored game | menu show current"
                        + " | menu logout | menu exit (quits the program)");
                if (app.news.hasUnread()) {
                    app.view.info("You have " + app.news.unreadCount() + " unread news item(s).");}
                return;

            case "start scored game":
                app.plantSelectController.begin(LevelSpec.scoredGame());
                return;
            case "whoami":
                app.view.info("Logged in as " + app.currentUser.getUsername() + " (" + app.currentUser.getNickname() + ")");
                return;
            default:
                app.view.error("Unknown command. Type 'help' for this menu's commands.");
        }
    }
}
