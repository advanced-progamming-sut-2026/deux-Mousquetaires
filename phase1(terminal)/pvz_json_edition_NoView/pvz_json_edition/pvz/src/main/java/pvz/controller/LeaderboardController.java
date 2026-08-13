package pvz.controller;

import pvz.model.leaderboard.Leaderboard;

class LeaderboardController {

    private final GameApp app;

    LeaderboardController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            return;}

        if (line.startsWith("show")) {
            Args args = Args.parse(line);
            Leaderboard.Column column = Leaderboard.Column.parse(args.get("c", "progress"));
            if (column == null) {
                return;}

            boolean ascending = "asc".equalsIgnoreCase(args.get("o", "desc"));
            Leaderboard.print(app.auth.getAllUsers(), column, ascending);
            return;
        }
    }
}
