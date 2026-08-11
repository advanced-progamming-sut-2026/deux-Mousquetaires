package pvz.controller;

import pvz.model.leaderboard.Leaderboard;

class LeaderboardController {

    private final GameApp app;

    LeaderboardController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Command: show -c <progress|minigames|daily-quests|quests|score>" + " -o <asc|desc> | menu exit");
            return;}

        if (line.startsWith("show")) {
            Args args = Args.parse(line);
            Leaderboard.Column column = Leaderboard.Column.parse(args.get("c", "progress"));
            if (column == null) {
                app.view.error("Unknown column. Options: progress, minigames," + " daily-quests, quests, score");
                return;}

            boolean ascending = "asc".equalsIgnoreCase(args.get("o", "desc"));
            Leaderboard.print(app.auth.getAllUsers(), column, ascending);
            return;
        }
        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }
}
