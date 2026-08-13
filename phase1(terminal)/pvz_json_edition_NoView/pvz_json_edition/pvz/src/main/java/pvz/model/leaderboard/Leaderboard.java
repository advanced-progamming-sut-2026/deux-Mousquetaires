package pvz.model.leaderboard;

import pvz.model.auth.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
public final class Leaderboard {
    public enum Column {
        PROGRESS, MINIGAMES, DAILY_QUESTS, QUESTS, SCORE;

        public static Column parse(String raw) {
            if (raw == null) return PROGRESS;
            switch (raw.toLowerCase()) {
                case "progress": return PROGRESS;
                case "minigames": return MINIGAMES;
                case "daily-quests": case "daily": return DAILY_QUESTS;
                case "quests": return QUESTS;
                case "score": return SCORE;
                default: return null;
            }
        }
    }

    private Leaderboard() {}
    public static void print(List<User> users, Column column, boolean ascending) {
        List<User> sorted = new ArrayList<>(users);
        Comparator<User> comparator = comparatorFor(column);
        if (!ascending) comparator = comparator.reversed();
        sorted.sort(comparator);

        int rank = 1;
        for (User user : sorted) {
        }
    }

    private static Comparator<User> comparatorFor(Column column) {
        switch (column) {
            case MINIGAMES:
                return Comparator.comparingInt(User::getMiniGamesCompleted);
            case DAILY_QUESTS:
                return Comparator.comparingInt(User::getDailyQuestsDone);
            case QUESTS:
                return Comparator.comparingInt(User::getOtherQuestsDone);
            case SCORE:
                return Comparator.comparingInt(User::getBestMooPoints);
            case PROGRESS:
            default:
                return Comparator.comparingInt(u -> u.getProgress().getClearedLevels().size());
        }
    }

    private static String progressLabel(User user) {
        int cleared = user.getProgress().getClearedLevels().size();
        int chapter = Math.min(4, cleared / 4 + 1);
        int level = cleared % 4 + 1;
        if (cleared >= 16) return "finished";
        return "ch" + chapter + "-lv" + level;
    }
}
