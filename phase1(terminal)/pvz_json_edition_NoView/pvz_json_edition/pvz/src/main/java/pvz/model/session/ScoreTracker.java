package pvz.model.session;

public class ScoreTracker {

    public static final int MULTI_KILL_POINTS = 100;
    public static final int QUICK_KILL_POINTS = 50;
    public static final int CROSS_LANE_POINTS = 75;
    public static final int UNTOUCHED_WAVE_POINTS = 200;
    public static final int CLEAN_HOUSE_POINTS = 500;
    public static final int BASE_KILL_POINTS = 10;

    private int meowPoints;

    private void award(int points, String reason) {
        meowPoints += points;
    }

    public void onKill() {meowPoints += BASE_KILL_POINTS;}

    public void onTickKills(int killsBySingleAttack, int distinctLanes, int quickKills) {
        if (killsBySingleAttack >= 2) award(MULTI_KILL_POINTS, "multi-kill: " + killsBySingleAttack + " zombies with one attack");
        if (distinctLanes >= 2) award(CROSS_LANE_POINTS, "kills in " + distinctLanes + " lanes at once");
        for (int i = 0; i < quickKills; i++) award(QUICK_KILL_POINTS, "quick kill within 5s of spawn");
    }

    public void onUntouchedWave(int wave) {
        award(UNTOUCHED_WAVE_POINTS, "wave " + wave + " survived without losing a plant");
    }
    public void onCleanHouseWin() {
        award(CLEAN_HOUSE_POINTS, "won with all lawn mowers intact");
    }
    public int getMeowPoints() {
        return meowPoints;
    }
}
