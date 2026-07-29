package pvz.controller;

class NewsController {
    private final GameApp app;
    NewsController(GameApp app) {this.app = app;}
    void handle(String line) {
        switch (line){
            case "help":
                app.view.info("Commands: show-unread | show-all | menu exit");
                return;
            case "show-unread":
                app.news.showUnread();
                return;
            case "show-all":
                app.news.showAll();
                return;
            default:
                app.view.error("Unknown command. Type 'help' for this menu's commands.");
        }
    }
}
