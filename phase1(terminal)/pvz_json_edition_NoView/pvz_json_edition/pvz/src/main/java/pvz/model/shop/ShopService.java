package pvz.model.shop;

import pvz.model.auth.User;
import pvz.model.enums.PlantType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShopService {

    public static final int POT_PRICE_COINS = 2000;
    public static final int PLANT_FOOD_PRICE_GEMS = 3;
    public static final int RANDOM_BUNDLE_PRICE_COINS = 1000;
    public static final int RANDOM_BUNDLE_PACKETS = 5;
    public static final int CHOSEN_BUNDLE_PRICE_GEMS = 5;
    public static final int CHOSEN_BUNDLE_PACKETS = 10;
    public static final int CONVERSION_PRICE_GEMS = 5;
    public static final int CONVERSION_COINS = 500;
    public static final int DAILY_BASE_PRICE = 2000;
    public static final int DAILY_DISCOUNT_PRICE = 1600;
    public static final int DAILY_PACKETS = 10;

    private LocalDate dailyOfferDate;
    private PlantType dailyOfferPlant;
    private String lastDailyPurchaseUser;
    private LocalDate lastDailyPurchaseDate;
    /// refresh at 24
    private void refreshDailyOffer(User user) {
        LocalDate today = LocalDate.now();
        if (today.equals(dailyOfferDate) && dailyOfferPlant != null) return;
        dailyOfferDate = today;
        List<PlantType> unlocked = unlockedPlants(user);
        Random random = new Random(today.toEpochDay());
        dailyOfferPlant = unlocked.isEmpty() ? PlantType.PEASHOOTER : unlocked.get(random.nextInt(unlocked.size()));
    }

    private List<PlantType> unlockedPlants(User user) {
        List<PlantType> unlocked = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            if (user.getCollection().isPlantUnlocked(type)) unlocked.add(type);
        }
        return unlocked;
    }

    /// print
    public void printCatalog(User user) {
    }

    public void printDailyOffer(User user) {
        refreshDailyOffer(user);
        if (hasBoughtDailyToday(user)) {}
    }

    private boolean hasBoughtDailyToday(User user) {
        return LocalDate.now().equals(lastDailyPurchaseDate) && user.getUsername().equalsIgnoreCase(lastDailyPurchaseUser);
    }

    /// buy
    public String buy(User user, int itemId, int count, PlantType chosen) {
        if (count <= 0) return "Count must be positive.";
        switch (itemId) {
            case 1: return buyPots(user, count);
            case 2: return buyPlantFood(user, count);
            case 3: return buyRandomBundle(user, count);
            case 4: return buyChosenBundle(user, count, chosen);
            case 5: return buyCoins(user, count);
            case 6: return buyDaily(user, count);
            default: return "Unknown item id: " + itemId;
        }
    }

    private String buyPots(User user, int count) {
        if (user.getPots() + count > User.MAX_POTS) return "You cannot own more than " + User.MAX_POTS + " pots.";
        if (!user.spendCoins(POT_PRICE_COINS * count)) return "Not enough coins.";
        for (int i = 0; i < count; i++) user.addPot();
        return "Bought " + count + " pot(s). You now own " + user.getPots() + ".";
    }

    private String buyPlantFood(User user, int count) {
        if (user.getPlantFoods() + count > User.MAX_PLANT_FOOD) return "You cannot store more than " + User.MAX_PLANT_FOOD + " plant foods.";
        if (!user.spendGems(PLANT_FOOD_PRICE_GEMS * count)) return "Not enough gems.";
        for (int i = 0; i < count; i++) user.addPlantFood();
        return "Bought " + count + " plant food(s). You now store " + user.getPlantFoods() + ".";
    }

    private String buyRandomBundle(User user, int count) {
        List<PlantType> unlocked = unlockedPlants(user);
        if (unlocked.isEmpty()) return "You have no unlocked plants yet.";
        if (!user.spendCoins(RANDOM_BUNDLE_PRICE_COINS * count)) return "Not enough coins.";
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder("You received:");
        for (int i = 0; i < count; i++) {
            PlantType type = unlocked.get(random.nextInt(unlocked.size()));
            user.addSeedPackets(type, RANDOM_BUNDLE_PACKETS);
            stringBuilder.append(' ').append(RANDOM_BUNDLE_PACKETS).append("x ").append(type);
            if (i < count - 1) stringBuilder.append(',');
        }
        return stringBuilder.toString();
    }

    private String buyChosenBundle(User user, int count, PlantType chosen) {
        if (chosen == null) return "Item 4 needs -t <plant_type>.";
        if (!user.getCollection().isPlantUnlocked(chosen)) return "You can only buy packets for unlocked plants.";
        if (!user.spendGems(CHOSEN_BUNDLE_PRICE_GEMS * count)) return "Not enough gems.";
        user.addSeedPackets(chosen, CHOSEN_BUNDLE_PACKETS * count);
        return "Bought " + (CHOSEN_BUNDLE_PACKETS * count) + " seed packets of " + chosen + " (you now have " + user.getSeedPackets(chosen) + ").";
    }

    private String buyCoins(User user, int count) {
        if (!user.spendGems(CONVERSION_PRICE_GEMS * count)) return "Not enough gems.";
        user.addCoins(CONVERSION_COINS * count);
        return "Converted " + (CONVERSION_PRICE_GEMS * count) + " gems into " + (CONVERSION_COINS * count) + " coins.";
    }

    private String buyDaily(User user, int count) {
        refreshDailyOffer(user);
        if (count != 1) return "The daily offer can only be bought once per day.";
        if (hasBoughtDailyToday(user)) return "You already bought today's offer. It refreshes at midnight.";
        if (!user.spendCoins(DAILY_DISCOUNT_PRICE)) return "Not enough coins.";
        user.addSeedPackets(dailyOfferPlant, DAILY_PACKETS);
        lastDailyPurchaseUser = user.getUsername();
        lastDailyPurchaseDate = LocalDate.now();
        return "Daily offer purchased: " + DAILY_PACKETS + " packets of " + dailyOfferPlant + ".";
    }

    /// accessibility
    public LocalDate getLastDailyPurchaseDate() { return lastDailyPurchaseDate; }
    public String getLastDailyPurchaseUser() { return lastDailyPurchaseUser; }
    public void restoreDailyPurchase(String username, LocalDate date) {
        this.lastDailyPurchaseUser = username;
        this.lastDailyPurchaseDate = date;
    }
}
