package pvz.controller;

import pvz.model.enums.MiniGameType;

class TravelLogController {

    private final GameApp app;

    TravelLogController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: travel log page <story|epic|daily|repeatable|minigames>" + " | claim <quest_id> | play minigame -t <type> -l <level 1-3> | menu exit");
            return;}
        if (line.startsWith("travel log page ")) {
            String page = line.substring("travel log page ".length()).trim();
            if (page.equalsIgnoreCase("minigames") || page.equalsIgnoreCase("minigame")) {
                printMiniGames();
                return;}

            app.quests.printPage(page);
            return;}

        if (line.startsWith("play minigame")) {
            Args args = Args.parse(line);
            Integer level = args.getInt("l");
            app.miniGameController.begin(args.get("t"), level == null ? 1 : level);
            return;}
        if (line.startsWith("claim ")) {
            String questId = line.substring("claim ".length()).trim();
            String result = app.quests.claim(questId, app.currentUser, app.news);
            app.view.info(result);
            app.saveAll();
            return;}
        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void printMiniGames() {
        StringBuilder stringBuilder = new StringBuilder("Mini-games (3 levels each):\n");
        for (MiniGameType type : MiniGameType.values()) {
            stringBuilder.append("  ").append(type).append('\n');}
        stringBuilder.append("Play with: play minigame -t <type> -l <level 1-3>");
        app.view.info(stringBuilder.toString());
    }
}
