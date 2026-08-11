package pvz.view;

import pvz.model.auth.GameProgress;
import pvz.model.auth.User;

class ChaptersView {

    private static final String[] WORLDS = {
            "Ancient Egypt", "Frostbite Caves", "Big Wave Beach", "Dark Ages",
    };

    void render(User user) {
        GameProgress progress = user.getProgress();
        StringBuilder stringBuilder = new StringBuilder("Adventure chapters:\n");
        for (int chapter = 1; chapter <= 4; chapter++) {
            stringBuilder.append("  Chapter ").append(chapter).append(" - ").append(WORLDS[chapter - 1]).append(": ");
            for (int level = 1; level <= 4; level++) {
                String id = chapter + "-" + level;
                boolean cleared = progress.isLevelCleared(id);
                boolean unlocked = progress.isLevelUnlocked(id);
                stringBuilder.append('[').append(id).append(cleared ? " v" : unlocked ? " o" : " x").append("] ");
            }
            stringBuilder.append('\n');
        }
        stringBuilder.append("v = cleared, o = unlocked, x = locked.").append(" Also: start scored game (MooPoints!)");
        System.out.println(stringBuilder);
    }
}
