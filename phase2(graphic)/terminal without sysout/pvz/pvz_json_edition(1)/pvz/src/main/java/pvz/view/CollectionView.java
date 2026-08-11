package pvz.view;

import pvz.model.auth.Collection;
import pvz.model.auth.User;
import pvz.model.entity.plant.PlantCatalog;
import pvz.model.entity.zombie.ZombieCatalog;
import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;

class CollectionView {

    void renderPlantList(User user, boolean all) {
        Collection collection = user.getCollection();
        StringBuilder stringBuilder = new StringBuilder(all ? "All plants:\n" : "Your unlocked plants:\n");
        for (PlantType type : PlantType.values()) {
            boolean unlocked = collection.isPlantUnlocked(type);
            if (!all && !unlocked) continue;
            stringBuilder.append("  ").append(type);
            if (unlocked) {
                stringBuilder.append(" (level ").append(collection.getPlantLevel(type)).append(", packets ").append(user.getSeedPackets(type)).append(')');
            }
            else stringBuilder.append(" [locked]");
            stringBuilder.append('\n');
        }
        System.out.println(stringBuilder.toString().stripTrailing());
    }

    void renderZombieList(User user, boolean all) {
        Collection collection = user.getCollection();
        StringBuilder stringBuilder = new StringBuilder(all ? "All zombies:\n" : "Zombies you have met:\n");
        boolean any = false;
        for (ZombieType type : ZombieType.values()) {
            boolean unlocked = collection.isZombieUnlocked(type);
            if (!all && !unlocked) continue;
            any = true;
            stringBuilder.append("  ").append(type).append(unlocked ? "" : " [not encountered]").append('\n');
        }
        if (!all && !any) {
            System.out.println("You have not met any zombies yet. Go play a level!");
            return;}
        System.out.println(stringBuilder.toString().stripTrailing());
    }

    void renderPlantDetails(User user, PlantType type) {
        PlantCatalog.Stats stats = PlantCatalog.of(type);
        Collection collection = user.getCollection();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(type).append(" - ").append(stats.description).append('\n');
        stringBuilder.append("  Sun cost: ").append(stats.sunCost).append(" | HP: ").append(stats.hp).append(" | Damage: ").append(stats.damage).append(" | Recharge: ").append(stats.rechargeTicks).append(" ticks\n");
        stringBuilder.append("  Family: ").append(stats.family).append(" | Unlocked: ").append(collection.isPlantUnlocked(type));
        if (collection.isPlantUnlocked(type)) {
            int level = collection.getPlantLevel(type);
            stringBuilder.append(" | Level: ").append(level).append(" (damage x").append(String.format("%.2f", 1 + 0.25 * (level - 1))).append(')');
        }
        stringBuilder.append('\n').append("  Plant food: ").append(stats.plantFoodEffect);
        stringBuilder.append('\n').append("  Upgrades: Lvl2 ").append(stats.upgrades[0]).append(" | Lvl3 ").append(stats.upgrades[1]).append(" | Lvl4 ").append(stats.upgrades[2]);
        System.out.println(stringBuilder);
    }

    void renderZombieDetails(ZombieType type) {
        ZombieCatalog.Stats stats = ZombieCatalog.of(type);
        System.out.println(type + " - " + stats.description + "\n" + "  HP: " + stats.hp + " | Armor: " + stats.armorHp + (stats.metalArmor ? " (metal)" : "")
                + " | Speed: " + stats.cellsPerSecond + " cells/s"
                + " | Bite: " + stats.damagePerSecond + "/s"
                + " | Wave cost: " + stats.waveCost);
    }
}
