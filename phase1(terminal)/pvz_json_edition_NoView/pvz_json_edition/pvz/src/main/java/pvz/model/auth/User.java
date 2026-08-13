package pvz.model.auth;

import pvz.model.enums.Gender;
import pvz.model.enums.PlantType;
import pvz.model.greenhouse.Greenhouse;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class User {

    public static final int MAX_POTS = 20;
    public static final int MAX_PLANT_FOOD = 3;
    private String username;
    private String passwordHash;
    private String email;
    private String nickname;
    private final Gender gender;
    private int coins;
    private int gems;
    private int pots;
    private int plantFoods;
    private final Map<PlantType, Integer> seedPackets = new EnumMap<>(PlantType.class);
    private final Map<PlantType, Integer> storedBoosts = new EnumMap<>(PlantType.class);
    private boolean stayLoggedIn;
    private String sessionToken;
    ///
    private int gamesPlayed;
    private int bestMooPoints;
    private int miniGamesCompleted;
    private int dailyQuestsDone;
    private int otherQuestsDone;
    private int zombiesKilled;
    ///
    private final UserSettings settings = new UserSettings();
    private final Collection collection = new Collection();
    private final GameProgress progress = new GameProgress();
    private final Greenhouse greenhouse = new Greenhouse();

    private final List<String[]> securityQAs = new ArrayList<>();

    public User(String username, String passwordHash, String email, String nickname, Gender gender) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        collection.unlockPlantQuietly(PlantType.SUNFLOWER);
        collection.unlockPlantQuietly(PlantType.PEASHOOTER);
        collection.unlockPlantQuietly(PlantType.WALL_NUT);
    }
    public boolean checkPassword(String raw, AuthService auth) {
        return passwordHash.equals(auth.hashPassword(raw));
    }
    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public void generateSessionToken() {
        this.stayLoggedIn = true;
        this.sessionToken = UUID.randomUUID().toString();
    }

    public void logout() {
        stayLoggedIn = false;
        sessionToken = null;
    }

    public void addSecurityQA(String question, String answerHash) {
        securityQAs.add(new String[]{question, answerHash});
    }

    public List<String[]> getSecurityQAs() {
        return securityQAs;
    }

    ///wallet
    public void addCoins(int n) { coins += n; }
    public boolean spendCoins(int n) { if (coins < n) return false; coins -= n; return true; }
    public void addGems(int n) { gems += n; }
    public boolean spendGems(int n) { if (gems < n) return false; gems -= n; return true; }
    ///
    public boolean addPot() {
        if (pots >= MAX_POTS) return false;
        pots++;
        return true;
    }
    public boolean usePot() {
        if (pots <= 0) return false;
        pots--;
        return true;
    }
    public boolean addPlantFood() {
        if (plantFoods >= MAX_PLANT_FOOD) return false;
        plantFoods++;
        return true;
    }
    public void setPlantFoodsClamped(int n) {
        plantFoods = Math.max(0, Math.min(MAX_PLANT_FOOD, n));
    }
    public void addSeedPackets(PlantType type, int n) {
        seedPackets.merge(type, n, Integer::sum);
    }
    public int getSeedPackets(PlantType type) {
        return seedPackets.getOrDefault(type, 0);
    }
    public boolean spendSeedPackets(PlantType type, int n) {
        if (getSeedPackets(type) < n) return false;
        seedPackets.put(type, getSeedPackets(type) - n);
        return true;
    }
    public void addStoredBoost(PlantType type) {
        storedBoosts.merge(type, 1, Integer::sum);
    }

    public boolean useStoredBoost(PlantType type) {
        int have = storedBoosts.getOrDefault(type, 0);
        if (have <= 0) return false;
        storedBoosts.put(type, have - 1);
        return true;
    }
    public Map<PlantType, Integer> getSeedPacketMap() { return seedPackets; }
    public Map<PlantType, Integer> getStoredBoostMap() { return storedBoosts; }
    /// change profile
    void renameTo(String newUsername) { this.username = newUsername; }
    public void setNickname(String n) { this.nickname = n; }
    public void setEmail(String e) { this.email = e; }
    ///
    public void recordGamePlayed(int mooPoints) {
        gamesPlayed++;
        bestMooPoints = Math.max(bestMooPoints, mooPoints);
    }

    public void recordMiniGameCompleted() { miniGamesCompleted++; }
    public void recordQuestDone(boolean daily) {
        if (daily) dailyQuestsDone++; else otherQuestsDone++;
    }
    public void recordZombiesKilled(int n) { zombiesKilled += n; }

    /// get
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
    public Gender getGender() { return gender; }
    public int getCoins() { return coins; }
    public int getGems() { return gems; }
    public int getPots() { return pots; }
    public int getPlantFoods() { return plantFoods; }
    public boolean isStayLoggedIn() { return stayLoggedIn; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getBestMooPoints() { return bestMooPoints; }
    public int getMiniGamesCompleted() { return miniGamesCompleted; }
    public int getDailyQuestsDone() { return dailyQuestsDone; }
    public int getOtherQuestsDone() { return otherQuestsDone; }
    public int getZombiesKilled() { return zombiesKilled; }
    public UserSettings getSettings() { return settings; }
    public Collection getCollection() { return collection; }
    public GameProgress getProgress() { return progress; }
    public Greenhouse getGreenhouse() { return greenhouse; }
    /// set
    public void setCoins(int n) { coins = n; }
    public void setGems(int n) { gems = n; }
    public void setPots(int n) { pots = Math.max(0, Math.min(MAX_POTS, n)); }
    public void setPlantFoods(int n) { setPlantFoodsClamped(n); }
    public void setGamesPlayed(int n) { gamesPlayed = n; }
    public void setBestMooPoints(int n) { bestMooPoints = n; }
    public void setMiniGamesCompleted(int n) { miniGamesCompleted = n; }
    public void setDailyQuestsDone(int n) { dailyQuestsDone = n; }
    public void setOtherQuestsDone(int n) { otherQuestsDone = n; }
    public void setZombiesKilled(int n) { zombiesKilled = n; }
}
