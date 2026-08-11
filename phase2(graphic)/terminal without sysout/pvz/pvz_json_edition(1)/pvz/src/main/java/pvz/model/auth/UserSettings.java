package pvz.model.auth;

public class UserSettings {

    private boolean musicOn  = true;
    private boolean sfxOn    = true;
    private int difficulty = 3;   // 1–5

    public void changeDifficulty(int difficulty) {
        if (difficulty < 1 || difficulty > 5) throw new IllegalArgumentException("Difficulty must be 1–5");
        this.difficulty = difficulty;
    }
    public float getDifficultyMultiplier() {
        switch (difficulty) {
            case 1: return 0.6f;
            case 2: return 0.8f;
            case 3: return 1.0f;
            case 4: return 1.3f;
            case 5: return 1.6f;
            default: return 1.0f;
        }
    }
    public boolean isMusicOn()  { return musicOn; }
    public void setMusicOn(boolean v) { musicOn = v; }
    public boolean isSfxOn()    { return sfxOn; }
    public void setSfxOn(boolean v)   { sfxOn = v; }
    public int  getDifficulty() { return difficulty; }
}
