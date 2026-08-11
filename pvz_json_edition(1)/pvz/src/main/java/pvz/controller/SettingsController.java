package pvz.controller;

class SettingsController {

    private final GameApp app;

    SettingsController(GameApp app) {this.app = app;}
    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: change-difficulty -l <1-5> | show settings" + " | music on|off | sfx on|off | menu exit");
            return;}
        if (line.startsWith("change-difficulty")) {
            Integer level = Args.parse(line).getInt("l");
            if (level == null || level < 1 || level > 5) {
                app.view.error("Usage: change-difficulty -l <1-5>  (3 is the normal game)");
                return;}

            app.currentUser.getSettings().changeDifficulty(level);
            app.view.success("Difficulty set to " + level + " (zombie strength x" + String.format("%.2f", level / 3.0) + ").");
            app.saveAll();
            return;}

        if (line.equals("show settings")) {
            app.view.info("Difficulty: " + app.currentUser.getSettings().getDifficulty()
                    + " | Music: " + (app.currentUser.getSettings().isMusicOn() ? "on" : "off")
                    + " | SFX: " + (app.currentUser.getSettings().isSfxOn() ? "on" : "off"));
            return;}

        if (line.startsWith("music ")) {
            boolean on = line.endsWith("on");
            app.currentUser.getSettings().setMusicOn(on);
            app.view.success("Music " + (on ? "on" : "off") + ".");
            return;}

        if (line.startsWith("sfx ")) {
            boolean on = line.endsWith("on");
            app.currentUser.getSettings().setSfxOn(on);
            app.view.success("Sound effects " + (on ? "on" : "off") + ".");
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }
}
