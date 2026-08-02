package pvz.model.auth;

import pvz.model.enums.WorldType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameProgress {

    public static final int CHAPTERS = 4;
    public static final int LEVELS_PER_CHAPTER = 4;

    private int currentWorldIndex;   // 0-based index into WorldType.values()
    private int currentChapter = 1;  // chapter within the world (1-based)
    private final Set<String> clearedLevels = new HashSet<>();
    private final Map<String, Boolean> miniGamesUnlocked = new HashMap<>();
    private boolean shopUnlocked;

    //level progressing
    public void completeLevel(String levelId) {
        clearedLevels.add(levelId);
        shopUnlocked = true;
        int chapter = chapterOf(levelId);
        if (chapter > 0) {
            currentChapter = Math.max(currentChapter, chapter);
            while (currentWorldIndex < chapter - 1 && currentWorldIndex < WorldType.values().length - 1) {
                advanceWorld();
            }
        }
    }

    public boolean isLevelCleared(String levelId) {
        return clearedLevels.contains(levelId);
    }

    public boolean isLevelUnlocked(String levelId) {
        if ("1-1".equals(levelId)) return true;
        if (clearedLevels.contains(levelId)) return true;
        String previous = previousLevelId(levelId);
        return previous != null && clearedLevels.contains(previous);
    }

    private static String previousLevelId(String levelId) {
        int chapter = chapterOf(levelId);
        int level = levelOf(levelId);
        if (chapter <= 0 || level <= 0) return null;
        if (level > 1) return chapter + "-" + (level - 1);
        if (chapter > 1) return (chapter - 1) + "-" + LEVELS_PER_CHAPTER;
        return null;
    }

    private static int chapterOf(String levelId) {
        int dash = levelId == null ? -1 : levelId.indexOf('-');
        if (dash <= 0) return -1;
        try {
            return Integer.parseInt(levelId.substring(0, dash));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int levelOf(String levelId) {
        int dash = levelId == null ? -1 : levelId.indexOf('-');
        if (dash <= 0 || dash == levelId.length() - 1) return -1;
        try {
            return Integer.parseInt(levelId.substring(dash + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    //mini games
    public void unlockMiniGame(String name) {
        miniGamesUnlocked.put(name,true);
    }
    public void advanceWorld() {
        if (currentWorldIndex < WorldType.values().length - 1) {
            currentWorldIndex++;
            currentChapter = 1;
        }
    }
    public Set<String> getClearedLevels() {
        return clearedLevels;
    }
}
