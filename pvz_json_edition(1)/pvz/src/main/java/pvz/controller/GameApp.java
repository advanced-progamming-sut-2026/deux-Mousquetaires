package pvz.controller;

import pvz.model.auth.AuthService;
import pvz.model.auth.User;
import pvz.model.enums.MenuContext;
import pvz.model.news.NewsManager;
import pvz.model.persist.UpdaterPerSecond;
import pvz.model.shop.QuestManager;
import pvz.model.shop.ShopService;
import pvz.view.ConsoleView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class GameApp {

    final ConsoleView view = new ConsoleView();
    final AuthService auth = new AuthService();
    final NewsManager news = new NewsManager();
    final QuestManager quests = new QuestManager();
    final ShopService shop = new ShopService();
    final Deque<MenuContext> menuStack = new ArrayDeque<>();

    User currentUser;
    boolean running = true;

    private final AuthController authController = new AuthController(this);
    private final ProfileController profileController = new ProfileController(this);
    private final SettingsController settingsController = new SettingsController(this);
    private final NewsController newsController = new NewsController(this);
    private final MainMenuController mainMenuController = new MainMenuController(this);
    private final GameMenuController gameMenuController = new GameMenuController(this);
    private final CollectionController collectionController = new CollectionController(this);
    final PlantSelectController plantSelectController = new PlantSelectController(this);
    final InGameController inGameController = new InGameController(this);
    private final GreenhouseController greenhouseController = new GreenhouseController(this);
    private final ShopController shopController = new ShopController(this);
    private final TravelLogController travelLogController = new TravelLogController(this);
    private final LeaderboardController leaderboardController = new LeaderboardController(this);
    final MiniGameController miniGameController = new MiniGameController(this);

    public void run() {
        view.banner();
        User stayLoggedIn = UpdaterPerSecond.loadAll(auth);
        if (stayLoggedIn != null) {
            currentUser = stayLoggedIn;
            currentUser.generateSessionToken();
            menuStack.push(MenuContext.MAIN);
            view.info("Welcome back, " + currentUser.getNickname() + "! You are still logged in.");}

        else menuStack.push(MenuContext.REGISTER);

        Scanner scanner = new Scanner(System.in);
        while (running) {
            view.prompt(current().getDisplayName());
            if (!scanner.hasNextLine()) break;

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            try {
                dispatch(line);
            } catch (Exception e) {
                view.error("Something went wrong: " + e.getMessage());
            }
        }
        saveAll();
        view.info("Progress saved. Goodbye!");
    }

    MenuContext current() {
        return menuStack.peek();
    }

    //router
    private void dispatch(String line) {
        if (current() == MenuContext.IN_GAME) {
            inGameController.handle(line);
            return;}

        if (current() == MenuContext.MINI_GAME) {
            miniGameController.handle(line);
            return;}

        if (line.equals("exit program")) {
            running = false;
            return;}

        if (line.equals("menu show current")) {
            view.menuPath(current().getDisplayName());
            return;}

        if (line.equals("menu exit")) {
            exitMenu();
            return;}

        if (line.equals("menu logout")) {
            logout();
            return;}

        if (line.startsWith("menu enter ")) {
            enterMenu(line.substring("menu enter ".length()).trim());
            return;}

        if (routePrefixed(line)) return;

        switch (current()) {
            case REGISTER:
            case LOGIN:
                authController.handle(line);
                return;
            case MAIN:
                mainMenuController.handle(line);
                return;
            case PROFILE:
                profileController.handle(line);
                return;
            case SETTINGS:
                settingsController.handle(line);
                return;
            case NEWS:
                newsController.handle(line);
                return;
            case GAME:
                gameMenuController.handle(line);
                return;
            case COLLECTION:
                collectionController.handle(line);
                return;
            case PLANT_SELECT:
                plantSelectController.handle(line);
                return;
            case GREENHOUSE:
                greenhouseController.handle(line);
                return;
            case SHOP:
                shopController.handle(line);
                return;
            case TRAVEL_LOG:
                travelLogController.handle(line);
                return;
            case LEADERBOARD:
                leaderboardController.handle(line);
                return;
            default:
                view.error("Unknown command. Type 'help' for this menu's commands.");}
    }

    private boolean routePrefixed(String line) {
        if (line.equals("menu greenhouse") || line.equals("menu travel-log") || line.equals("menu leaderboard")) {
            enterMenu(line.substring("menu ".length()));
            return true;}

        if (line.startsWith("menu profile ") && allows(MenuContext.PROFILE)) {
            profileController.handle(line.substring("menu profile ".length()).trim());
            return true;}

        if (line.startsWith("menu settings ") && allows(MenuContext.SETTINGS)) {
            settingsController.handle(line.substring("menu settings ".length()).trim());
            return true;}

        if (line.startsWith("menu news ") && allows(MenuContext.NEWS)) {
            newsController.handle(line.substring("menu news ".length()).trim());
            return true;}

        if (line.startsWith("menu collection ") && allows(MenuContext.COLLECTION)) {
            collectionController.handle(line.substring("menu collection ".length()).trim());
            return true;
        }
        return false;
    }

    private boolean allows(MenuContext owner) {return current() == owner || isReachable(owner);}

    //menu
    private void enterMenu(String name) {
        if (name.startsWith("chapter")) {
            if (current() != MenuContext.GAME) view.error("You can enter a chapter from the game menu.");
            else gameMenuController.enterChapter(name.substring("chapter".length()).trim());
            return;}

        MenuContext target = parseMenuName(name);
        if (target == null) {
            view.error("Unknown menu: " + name);
            return;}

        if (!isReachable(target)) {
            view.error("You cannot enter the " + target.getDisplayName() + " from the " + current().getDisplayName() + ".");
            return;}

        menuStack.push(target);
        view.menuPath(target.getDisplayName());
        onMenuEntered(target);
    }

    private MenuContext parseMenuName(String raw) {
        switch (raw.toLowerCase().replace('_', '-')) {
            case "register": return MenuContext.REGISTER;
            case "login": return MenuContext.LOGIN;
            case "main": return MenuContext.MAIN;
            case "game": return MenuContext.GAME;
            case "settings": return MenuContext.SETTINGS;
            case "news": return MenuContext.NEWS;
            case "profile": return MenuContext.PROFILE;
            case "collection": return MenuContext.COLLECTION;
            case "greenhouse": return MenuContext.GREENHOUSE;
            case "shop": return MenuContext.SHOP;
            case "travel-log": return MenuContext.TRAVEL_LOG;
            case "leaderboard": return MenuContext.LEADERBOARD;
            default: return null;
        }
    }

    private boolean isReachable(MenuContext target) {
        switch (current()) {
            case REGISTER:
                return target == MenuContext.LOGIN;
            case LOGIN:
                return target == MenuContext.REGISTER;
            case MAIN:
                return target == MenuContext.GAME || target == MenuContext.SETTINGS || target == MenuContext.NEWS || target == MenuContext.PROFILE || target == MenuContext.LEADERBOARD;
            case GAME:
                return target == MenuContext.COLLECTION || target == MenuContext.GREENHOUSE || target == MenuContext.TRAVEL_LOG || target == MenuContext.LEADERBOARD || target == MenuContext.SHOP;
            case GREENHOUSE:
                return target == MenuContext.SHOP;
            default:
                return false;
        }
    }

    private void onMenuEntered(MenuContext target) {
        switch (target) {
            case NEWS:
                view.info("Unread news: " + news.unreadCount() + ". Commands: show-unread | show-all");
                return;
            case SHOP:
                if (currentUser != null) {
                    view.info("Coins: " + currentUser.getCoins() + " | Gems: " + currentUser.getGems() + ". Commands: shop list | shop daily | shop buy -i <id> -n <count> [-t <plant>]");}
                return;
            case GAME:
                gameMenuController.printOverview();
                return;
            default:
        }
    }

    void exitMenu() {
        switch (current()) {
            case REGISTER:
                running = false;
                return;
            case LOGIN:
                switchTo(MenuContext.REGISTER);
                return;
            case MAIN:
                running = false;
                return;
            case IN_GAME:
                inGameController.forfeit();
                return;
            case MINI_GAME:
                miniGameController.abort();
                return;
            default:
                menuStack.pop();
                if (menuStack.isEmpty()) {
                    menuStack.push(currentUser == null ? MenuContext.REGISTER : MenuContext.MAIN);}
                view.menuPath(current().getDisplayName());
        }
    }

    void switchTo(MenuContext target) {
        menuStack.clear();
        menuStack.push(target);
        view.menuPath(target.getDisplayName());
    }

    private void logout() {
        if (current() != MenuContext.MAIN) {
            view.error("You can only log out from the main menu.");
            return;}

        saveAll();

        if (currentUser != null) currentUser.logout();

        currentUser = null;
        view.info("Logged out. See you soon!");
        switchTo(MenuContext.REGISTER);
    }

    void saveAll() {
        User stay = currentUser != null && currentUser.isStayLoggedIn() ? currentUser : null;
        UpdaterPerSecond.saveAll(auth,stay);}

    void onLoggedIn(User user) {
        currentUser = user;
        switchTo(MenuContext.MAIN);
        view.info("Welcome, " + user.getNickname() + "! Unread news: " + news.unreadCount());}
}
