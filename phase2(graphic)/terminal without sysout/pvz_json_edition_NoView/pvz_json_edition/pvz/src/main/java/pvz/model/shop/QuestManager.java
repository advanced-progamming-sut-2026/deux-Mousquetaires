package pvz.model.shop;

import pvz.model.auth.User;
import pvz.model.entity.plant.PlantFactory;
import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantType;
import pvz.model.enums.QuestPriority;
import pvz.model.enums.RewardType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class QuestManager {
    /// change and daily reset
    private static final int[] SUN_AMOUNTS = {3000, 4000, 5000};
    private static final int[] MOWER_KILL_GOALS = {10, 20, 30, 40, 50};
    private static final PlantType[] KILLER_PLANTS = {
            PlantType.PEASHOOTER, PlantType.SNOW_PEA, PlantType.REPEATER,
            PlantType.CABBAGE_PULT, PlantType.FUME_SHROOM, PlantType.BONK_CHOY,
            PlantType.MELON_PULT};
    private static final PlantFamily[] FAMILIES = {
            PlantFamily.SUN_PRODUCER, PlantFamily.SHOOTER, PlantFamily.HOMING,
            PlantFamily.STRIKE_THROUGH, PlantFamily.LOBBER, PlantFamily.EXPLOSIVE,
            PlantFamily.MELEE, PlantFamily.WALL_NUT, PlantFamily.MODIFIER};

    private final List<Quest> quests = new ArrayList<>();
    private LocalDate dailyResetDate = LocalDate.now();

    public QuestManager() {
        seedStatic();
        seedDailies(LocalDate.now());
    }

    private void seedStatic() {
        int index = LocalDate.now().getDayOfYear();
        quests.add(new Quest("story-1", "Complete your first level", "story",
                QuestPriority.CRITICAL, RewardType.UNLOCKABLE, "plant:SNOW_PEA",
                1, "level_completed", false));
        /// hunter
        for (int chapter = 1; chapter <= 4; chapter++) {
            quests.add(new Quest("hunt-" + chapter,
                    "Hunter: defeat 50 zombies in chapter " + chapter, "story",
                    QuestPriority.HIGH, RewardType.INVENTORY, "packets:PEASHOOTER:10",
                    50, "chapter_kill:" + chapter, false));
        }
        /// gardner
        int maxLosses = index % 6;
        quests.add(new Quest("econ-1",
                "Frugal gardener: win a level losing at most " + maxLosses + " plants",
                "story", QuestPriority.HIGH, RewardType.INVENTORY,
                "packets:PEASHOOTER:" + (20 - maxLosses),
                1, "win_plant_losses_le:" + maxLosses, false));
        /// speed reaction
        quests.add(new Quest("speed-1",
                "Quick reflexes: defeat 10 zombies within 30s of the first wave",
                "story", QuestPriority.MEDIUM, RewardType.CURRENCY, "coins:500",
                10, "fast_kills_10", false));
        /// defence
        quests.add(new Quest("epic-defense",
                "Defense master: finish a level with exactly 0 sun in the bank",
                "epic", QuestPriority.CRITICAL, RewardType.CURRENCY, "gems:200",
                1, "win_zero_sun", false));
        /// day/night mod
        quests.add(new Quest("epic-night",
                "Night or day: win a level planting only mushrooms",
                "epic", QuestPriority.HIGH, RewardType.CURRENCY, "gems:20",
                1, "win_only_shrooms", false));
        /// mover
        int mowerGoal = MOWER_KILL_GOALS[index % MOWER_KILL_GOALS.length];
        quests.add(new Quest("epic-mower",
                "Mowing time: shred " + mowerGoal + " zombies with lawn mowers",
                "epic", QuestPriority.MEDIUM, RewardType.CURRENCY, "gems:" + mowerGoal,
                mowerGoal, "mower_kill", false));
        quests.add(new Quest("epic-minigame", "Complete 3 mini-games", "epic",
                QuestPriority.HIGH, RewardType.CURRENCY, "gems:30",
                3, "minigame_completed", false));
        quests.add(new Quest("rep-1", "Plant 25 plants", "repeatable",
                QuestPriority.LOW, RewardType.CURRENCY, "coins:250",
                25, "plants_planted", false));
    }
    private void seedDailies(LocalDate today) {
        quests.removeIf(Quest::isDaily);
        int idx = today.getDayOfYear();
        /// sun collect daily
        int sunAmount = SUN_AMOUNTS[idx % SUN_AMOUNTS.length];
        quests.add(new Quest("daily-sun",
                "Daily sun catcher: collect " + sunAmount + " sun in one day",
                "daily", QuestPriority.MEDIUM, RewardType.CURRENCY,
                "coins:" + (sunAmount / 100),
                sunAmount, "sun_collected", true));
        /// pro plant
        PlantType star = KILLER_PLANTS[idx % KILLER_PLANTS.length];
        quests.add(new Quest("daily-plant",
                "Pro plant gamer: defeat 10 zombies with your " + star,
                "daily", QuestPriority.HIGH, RewardType.UNLOCKABLE, "plant:RANDOM",
                10, "plant_kill:" + star, true));
        /// only cactus
        quests.add(new Quest("daily-cactus",
                "Only Cactus: defeat 10 zombies with the Cactus",
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:20",
                10, "plant_kill:" + PlantType.CACTUS, true));
        /// pro demolisher
        quests.add(new Quest("daily-boom",
                "Pro demolisher: use 3 explosive plants in a single level",
                "daily", QuestPriority.LOW, RewardType.CURRENCY, "coins:100",
                3, "explosives_in_level", true));
        /// تقارن
        quests.add(new Quest("daily-symmetry",
                "Symmetry: win with a garden mirrored across the middle row",
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "coins:500",
                1, "win_symmetric", true));
        /// family killer
        PlantFamily killFamily = FAMILIES[idx % FAMILIES.length];
        quests.add(new Quest("daily-family",
                "Family business: win using only the " + killFamily + " family to kill",
                "daily", QuestPriority.MEDIUM, RewardType.CURRENCY, "coins:1000",
                1, "win_only_family_kills:" + killFamily, true));
        /// without limit kill
        PlantFamily banned = FAMILIES[(idx + 4) % FAMILIES.length];
        quests.add(new Quest("daily-nofamily",
                "Bloom within limits: win without planting any " + banned + " plant",
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:100",
                1, "win_without_family:" + banned, true));
        /// win to win
        quests.add(new Quest("daily-streak",
                "Back-to-back: win 5 levels in a row (a loss resets progress)",
                "daily", QuestPriority.MEDIUM, RewardType.CURRENCY, "coins:5000",
                5, "win_streak", true));
        /// almost winner
        quests.add(new Quest("daily-almost",
                "Almost the winner: defeat 10 zombies in column 1 of rows with no mower",
                "daily", QuestPriority.MEDIUM, RewardType.CURRENCY, "coins:300",
                10, "col1_kill_no_mower", true));
        /// ocd fucking نمن
        quests.add(new Quest("daily-ocd",
                "No OCD here: win with no garden symmetry (middle row exempt)",
                "daily", QuestPriority.MEDIUM, RewardType.CURRENCY, "coins:800",
                1, "win_asymmetric", true));
        /// cloudy day
        quests.add(new Quest("daily-cloudy",
                "Cloudy day: win a level planting at most 3 sun producers",
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:10",
                1, "win_max3_sun_producers", true));
        /// one less column
        int emptyColumn = 1 + idx % 9;
        quests.add(new Quest("daily-col",
                "One column less: win without ever planting in column " + emptyColumn,
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:10",
                1, "win_empty_col:" + emptyColumn, true));
        /// without one defense row
        int emptyRow = 1 + idx % 5;
        quests.add(new Quest("daily-row",
                "Defenseless row: win without ever planting in row " + emptyRow,
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:20",
                1, "win_empty_row:" + emptyRow, true));
        /// cross undefensive
        int cross = 1 + (idx + 2) % 5;
        quests.add(new Quest("daily-cross",
                "Defenseless cross: win with column " + cross + " and row " + cross + " both empty",
                "daily", QuestPriority.HIGH, RewardType.CURRENCY, "gems:25",
                1, "win_empty_cross:" + cross, true));
    }
    private void checkDailyReset() {
        LocalDate today = LocalDate.now();
        if (!today.equals(dailyResetDate)) {
            dailyResetDate = today;
            seedDailies(today);
        }
    }
    public void onEvent(String eventKey, int amount) {
        checkDailyReset();
        if (amount <= 0 && !eventKey.equals("level_failed")) return;
        if (eventKey.equals("level_failed")) {
            for (Quest quest : quests) {
                if (quest.getEventKey().equals("win_streak") && !quest.isClaimed()) quest.setProgress(0);
            }
            return;
        }
        for (Quest quest : quests) {
            if (quest.getEventKey().equals(eventKey)) quest.advance(amount);
        }
    }
    public void printPage(String page) {
        checkDailyReset();
        List<Quest> onPage = new ArrayList<>();
        for (Quest quest : quests) {
            if (quest.getPage().equalsIgnoreCase(page)) onPage.add(quest);
        }
        if (onPage.isEmpty()) {
            return;
        }
        onPage.sort(Comparator.comparing(Quest::getPriority));
        for (Quest quest : onPage) {}
    }
    public String claim(String questId, User user, pvz.model.news.NewsManager newsManager) {
        for (Quest quest : quests) {
            if (quest.getId().equalsIgnoreCase(questId)) {
                if (!quest.isDone()) return "Quest '" + questId + "' is not finished yet.";
                if (quest.isClaimed()) return "Quest '" + questId + "' was already claimed.";
                quest.markClaimed();
                user.recordQuestDone(quest.isDaily());
                return payReward(quest,user,newsManager);
            }
        }
        return "Unknown quest id: " + questId;
    }

    private String payReward(Quest quest, User user, pvz.model.news.NewsManager newsManager) {
        String[] parts = quest.getRewardPayload().split(":");
        switch (quest.getRewardType()) {
            case CURRENCY:
                int amount = Integer.parseInt(parts[1]);
                if (parts[0].equals("coins")) user.addCoins(amount);
                else user.addGems(amount);
                return "Reward claimed: " + amount + " " + parts[0] + "!";
            case UNLOCKABLE:
                if (parts[1].equals("RANDOM")) return unlockRandomPlant(user, newsManager);
                PlantType plant = PlantFactory.parseType(parts[1]);
                if (plant != null) {
                    user.getCollection().unlockPlant(plant);
                    newsManager.publishUnlock("You unlocked the plant " + plant + "!");
                }
                return "Reward claimed: " + parts[1] + " unlocked!";
            case INVENTORY:
                PlantType packetPlant = PlantFactory.parseType(parts[1]);
                int packets = Integer.parseInt(parts[2]);
                if (packetPlant != null) user.addSeedPackets(packetPlant, packets);
                return "Reward claimed: " + packets + "x " + parts[1] + " seed packets!";
            default:
                return "Reward claimed.";
        }
    }

    private String unlockRandomPlant(User user, pvz.model.news.NewsManager newsManager) {
        List<PlantType> locked = new ArrayList<>();
        for (PlantType candidate : PlantType.values()) {
            if (!user.getCollection().isPlantUnlocked(candidate)) locked.add(candidate);}

        if (locked.isEmpty()) {
            user.addCoins(500);
            return "Every plant is already unlocked - here are 500 coins instead!";
        }
        PlantType prize = locked.get(new Random().nextInt(locked.size()));
        user.getCollection().unlockPlant(prize);
        newsManager.publishUnlock("You unlocked the plant " + prize + "!");
        return "Reward claimed: random new plant " + prize + " unlocked!";
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
