package pvz.controller;

class NewsController {
    private final GameApp app;
    NewsController(GameApp app) {this.app = app;}
    void handle(String line) {
        switch (line){
            case "help":
                return;
            case "show-unread":
                app.news.showUnread();
                return;
            case "show-all":
                app.news.showAll();
                return;
            default:
        }
    }
}
