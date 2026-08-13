package pvz.model.persist;

import pvz.model.auth.AuthService;
import pvz.model.auth.User;
import pvz.model.enums.Gender;
import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;
import pvz.model.greenhouse.Pot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class UpdaterPerSecond {

    private static final Path SAVE_DIR = Paths.get("saves");
    private static final Path USERS_FILE = SAVE_DIR.resolve("users.dat");
    private static final Path SESSION_FILE = SAVE_DIR.resolve("session.dat");
    private UpdaterPerSecond() {}

    /// save
    public static void saveAll(AuthService auth, User stayLoggedInUser) {
        try {
            Files.createDirectories(SAVE_DIR);
            try (BufferedWriter writer = Files.newBufferedWriter(USERS_FILE, StandardCharsets.UTF_8)) {
                for (User user : auth.getAllUsers()) {
                    writeUser(writer, user);
                    writer.write("---");
                    writer.newLine();
                }
            }
            if (stayLoggedInUser != null && stayLoggedInUser.isStayLoggedIn()) {
                Files.write(SESSION_FILE, stayLoggedInUser.getUsername().getBytes(StandardCharsets.UTF_8));
            }
            else Files.deleteIfExists(SESSION_FILE);
        } catch (IOException e) {
        }
    }

    private static void writeUser(BufferedWriter writer, User user) throws IOException {
        put(writer, "username", user.getUsername());
        put(writer, "passwordHash", user.getPasswordHash());
        put(writer, "email", user.getEmail());
        put(writer, "nickname", user.getNickname());
        put(writer, "gender", user.getGender().name());
        put(writer, "coins", String.valueOf(user.getCoins()));
        put(writer, "gems", String.valueOf(user.getGems()));
        put(writer, "pots", String.valueOf(user.getPots()));
        put(writer, "plantFoods", String.valueOf(user.getPlantFoods()));
        put(writer, "gamesPlayed", String.valueOf(user.getGamesPlayed()));
        put(writer, "bestMooPoints", String.valueOf(user.getBestMooPoints()));
        put(writer, "miniGamesCompleted", String.valueOf(user.getMiniGamesCompleted()));
        put(writer, "dailyQuestsDone", String.valueOf(user.getDailyQuestsDone()));
        put(writer, "otherQuestsDone", String.valueOf(user.getOtherQuestsDone()));
        put(writer, "zombiesKilled", String.valueOf(user.getZombiesKilled()));
        put(writer, "stayLoggedIn", String.valueOf(user.isStayLoggedIn()));
        put(writer, "difficulty", String.valueOf(user.getSettings().getDifficulty()));
        put(writer, "greenhouseRows", String.valueOf(user.getGreenhouse().getUnlockedRows()));
        for (String[] qa : user.getSecurityQAs()) {
            put(writer, "securityQA", qa[0].replace("|", " ") + "|" + qa[1]);
        }
        for (Map.Entry<PlantType, Boolean> e : user.getCollection().getPlants().entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                put(writer, "plantUnlocked", e.getKey().name() + "|" + user.getCollection().getPlantLevel(e.getKey()));}
        }
        for (Map.Entry<ZombieType, Boolean> e : user.getCollection().getZombies().entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) put(writer, "zombieUnlocked", e.getKey().name());
        }
        for (String level : user.getProgress().getClearedLevels()) {put(writer, "levelCleared", level);}
        for (Map.Entry<PlantType, Integer> e : user.getSeedPacketMap().entrySet()) {
            if (e.getValue() > 0) put(writer, "seedPackets", e.getKey().name() + "|" + e.getValue());
        }
        for (Map.Entry<PlantType, Integer> e : user.getStoredBoostMap().entrySet()) {
            if (e.getValue() > 0) put(writer, "storedBoost", e.getKey().name() + "|" + e.getValue());
        }
        for (Map.Entry<String, Pot> e : user.getGreenhouse().getPots().entrySet()) {
            put(writer, "pot", e.getKey() + "|" + e.getValue().getPlantType().name() + "|" + e.getValue().getPlantedAt());
        }
    }

    private static void put(BufferedWriter writer, String key, String value) throws IOException {
        writer.write(key + "=" + value);
        writer.newLine();
    }

    /// load
    public static User loadAll(AuthService auth) {
        if (!Files.exists(USERS_FILE)) return null;
        try (BufferedReader reader = Files.newBufferedReader(USERS_FILE, StandardCharsets.UTF_8)) {
            List<String> block = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("---")) {
                    User user = parseUser(block);
                    if (user != null) auth.restoreUser(user);
                    block.clear();
                }
                else if (!line.isEmpty()) block.add(line);
            }
            return loadSession(auth);
        } catch (IOException e) {
            return null;
        }
    }

    private static User loadSession(AuthService auth) throws IOException {
        if (!Files.exists(SESSION_FILE)) return null;
        String username = new String(Files.readAllBytes(SESSION_FILE), StandardCharsets.UTF_8).trim();
        User user = auth.findByUsername(username);
        if (user != null && user.isStayLoggedIn()) return user;
        return null;
    }

    private static User parseUser(List<String> block) {
        String username = null;
        String passwordHash = null;
        String email = null;
        String nickname = null;
        Gender gender = Gender.PREFER_NOT_TO_SAY;
        for (String line : block) {
            String[] kv = splitKeyValue(line);
            switch (kv[0]) {
                case "username": username = kv[1]; break;
                case "passwordHash": passwordHash = kv[1]; break;
                case "email": email = kv[1]; break;
                case "nickname": nickname = kv[1]; break;
                case "gender": gender = Gender.valueOf(kv[1]); break;
                default: break;
            }
        }
        if (username == null || passwordHash == null) return null;
        User user = new User(username, passwordHash, email, nickname, gender);
        for (String line : block) {applyField(user, splitKeyValue(line));}
        return user;
    }

    private static void applyField(User user, String[] kv) {
        String key = kv[0];
        String value = kv[1];
        switch (key) {
            case "coins": user.setCoins(Integer.parseInt(value)); break;
            case "gems": user.setGems(Integer.parseInt(value)); break;
            case "pots": user.setPots(Integer.parseInt(value)); break;
            case "plantFoods": user.setPlantFoods(Integer.parseInt(value)); break;
            case "gamesPlayed": user.setGamesPlayed(Integer.parseInt(value)); break;
            case "bestMooPoints": user.setBestMooPoints(Integer.parseInt(value)); break;
            case "miniGamesCompleted": user.setMiniGamesCompleted(Integer.parseInt(value)); break;
            case "dailyQuestsDone": user.setDailyQuestsDone(Integer.parseInt(value)); break;
            case "otherQuestsDone": user.setOtherQuestsDone(Integer.parseInt(value)); break;
            case "zombiesKilled": user.setZombiesKilled(Integer.parseInt(value)); break;
            case "stayLoggedIn":
                if (Boolean.parseBoolean(value)) user.generateSessionToken();
                break;
            case "difficulty": user.getSettings().changeDifficulty(Integer.parseInt(value)); break;
            case "greenhouseRows": user.getGreenhouse().setUnlockedRows(Integer.parseInt(value)); break;
            case "securityQA": applySecurityQA(user, value); break;
            case "plantUnlocked": applyPlantUnlocked(user, value); break;
            case "zombieUnlocked": user.getCollection().unlockZombieQuietly(ZombieType.valueOf(value)); break;
            case "levelCleared": user.getProgress().getClearedLevels().add(value); break;
            case "seedPackets": applySeedPackets(user, value); break;
            case "storedBoost": applyStoredBoost(user, value); break;
            case "pot": applyPot(user, value); break;
            default: break;
        }
    }

    private static void applySecurityQA(User user, String value) {
        String[] parts = value.split("\\|", 2);
        user.addSecurityQA(parts[0], parts[1]);
    }

    private static void applyPlantUnlocked(User user, String value) {
        String[] parts = value.split("\\|");
        PlantType type = PlantType.valueOf(parts[0]);
        user.getCollection().unlockPlantQuietly(type);
        int level = Integer.parseInt(parts[1]);
        user.getCollection().setPlantLevel(type, level);
    }

    private static void applySeedPackets(User user, String value) {
        String[] parts = value.split("\\|");
        user.addSeedPackets(PlantType.valueOf(parts[0]), Integer.parseInt(parts[1]));
    }

    private static void applyStoredBoost(User user, String value) {
        String[] parts = value.split("\\|");
        int count = Integer.parseInt(parts[1]);
        for (int i = 0; i < count; i++) {user.addStoredBoost(PlantType.valueOf(parts[0]));}
    }

    private static void applyPot(User user, String value) {
        String[] parts = value.split("\\|");
        String[] xy = parts[0].split(",");
        user.getGreenhouse().placePot(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]), new Pot(PlantType.valueOf(parts[1]), LocalDateTime.parse(parts[2])));
    }

    private static String[] splitKeyValue(String line) {
        int index = line.indexOf('=');
        return new String[]{line.substring(0, index), line.substring(index + 1)};
    }
}
