package pvz.model.minigame;

import java.util.Random;

public abstract class MiniGame {

    protected final int level; /// 1-->2--->3
    protected final Random random = new Random();
    protected boolean gameover;
    protected boolean won;

    protected MiniGame(int level) {
        this.level = Math.max(1, Math.min(3, level));
    }
    public abstract void start();
    public abstract void handle(String line);
    public boolean isGameover() { return gameover; }
    public boolean isWon() { return won; }
    public int getLevel() { return level; }

    protected void win(String message) {
        gameover = true;
        won = true;
    }
    protected void lose(String message) {
        gameover = true;
        won = false;
    }
}
