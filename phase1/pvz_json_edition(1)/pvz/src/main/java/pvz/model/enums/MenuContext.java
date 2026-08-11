package pvz.model.enums;

/** Represents which menu screen the player is currently viewing. */
public enum MenuContext {
    REGISTER("register menu"),
    LOGIN("login menu"),
    MAIN("main menu"),
    GAME("game menu"),
    SETTINGS("settings menu"),
    NEWS("news menu"),
    PROFILE("profile menu"),
    COLLECTION("collection menu"),
    PLANT_SELECT("plant selection menu"),
    IN_GAME("in-game"),
    GREENHOUSE("greenhouse"),
    SHOP("shop"),
    TRAVEL_LOG("travel log"),
    LEADERBOARD("leaderboard"),
    MINI_GAME("mini-game");

    private final String displayName;

    MenuContext(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
