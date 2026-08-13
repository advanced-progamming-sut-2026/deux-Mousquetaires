package pvz.controller;

class SettingsController {

    private final GameApp app;

    SettingsController(GameApp app) {this.app = app;}
    void handle(String line) {
        if (line.equals("help")) {
            return;}
        if (line.startsWith("change-difficulty")) {
            Integer level = Args.parse(line).getInt("l");
            if (level == null || level < 1 || level > 5) {
                return;}

            app.currentUser.getSettings().changeDifficulty(level);
            app.saveAll();
            return;}

        if (line.equals("show settings")) {
            return;}

        if (line.startsWith("music ")) {
            boolean on = line.endsWith("on");
            app.currentUser.getSettings().setMusicOn(on);
            return;}

        if (line.startsWith("sfx ")) {
            boolean on = line.endsWith("on");
            app.currentUser.getSettings().setSfxOn(on);
            return;}

    }
}
