package pvz.model.entity.zombie;

import pvz.model.enums.ZombieType;
import pvz.util.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ZombieCatalog {
    public static final class Stats {
        public final int hp;
        public final int armorHp;
        public final boolean metalArmor;     // Magnet shroom can steal it
        public final float cellsPerSecond;
        public final int damagePerSecond;
        public final int waveCost;
        public final boolean freezeImmune;   // Frostbite caves natives
        public final boolean fireImmune;     //dragon
        public final String description;
        
        Stats(int hp, int armorHp, boolean metalArmor, float cellsPerSecond,
              int damagePerSecond, int waveCost, boolean freezeImmune,
              boolean fireImmune, String description) {
            this.hp = hp;
            this.armorHp = armorHp;
            this.metalArmor = metalArmor;
            this.cellsPerSecond = cellsPerSecond;
            this.damagePerSecond = damagePerSecond;
            this.waveCost = waveCost;
            this.freezeImmune = freezeImmune;
            this.fireImmune = fireImmune;
            this.description = description;
        }
    }

    private static final Map<ZombieType, Stats> SHEET = new EnumMap<>(ZombieType.class);
    private static final String[][] JSON_ALIASES = {
            {"ZombieDefault", "BASIC"},
            {"ZombieArmor1", "CONEHEAD"},
            {"ZombieArmor2", "BUCKETHEAD"},
            {"ZombieArmor4", "BLOCKHEAD"},
            {"ZombieDarkArmor3", "KNIGHT"},
            {"ZombieGargantuar", "GARGANTUAR"},
            {"ZombieImp", "IMP"},
            {"ZombieRa", "RA"},
            {"ZombieExplorer", "EXPLORER"},
            {"ZombieTombRaiser", "TOMBRAISER"},
            {"ZombieIceAgeDodo", "DODO_RIDER"},
            {"ZombieIceAgeHunter", "HUNTER"},
            {"ZombieIceAgeTroglobite", "TROGLOBITE"},
            {"ZombieBeachFisherman", "FISHERMAN"},
            {"ZombieBeachOctopus", "OCTOPUS"},
            {"ZombieBeachSnorkel", "SNORKEL"},
            {"ZombieDarkJuggler", "JESTER"},
            {"ZombieWizard", "WIZARD"},
            {"ZombieDarkKing", "KING"},
            {"ZombieDarkImpDragon", "IMP_DRAGON"},
            {"ZombieModernAllStar", "ALL_STAR"},
            {"ZombieLostCityJane", "PARASOL"},
            {"ZombieCrystalSkull", "TURQUOISE_SKULL"},
            {"ZombieProspector", "PROSPECTOR"},
            {"ZombiePiano", "PIANIST"},
            {"ZombieNewspaper", "NEWSPAPER"},
            {"ZombieArcade", "ARCADE"},
    };

    private static void put(ZombieType type, int hp, int armorHp, boolean metalArmor,
                            float speed, int dps, int waveCost, boolean freezeImmune,
                            boolean fireImmune, String desc) {
        SHEET.put(type, new Stats(hp, armorHp, metalArmor, speed, dps, waveCost, freezeImmune, fireImmune, desc));
    }
    static {
        seedBuiltInDefaults();
        loadFromJson();
    }
    private static void seedBuiltInDefaults() {
        ///zombies
        put(ZombieType.BASIC, 190, 0, false, 0.185f, 100, 100, false, false,
                "Ordinary walker. Eats plants, wants your brain.");
        put(ZombieType.CONEHEAD, 190, 370, false, 0.185f, 100, 200, false, false,
                "Basic zombie wearing a 370 HP traffic cone (not metal).");
        put(ZombieType.BUCKETHEAD, 190, 1100, true, 0.185f, 100, 400, false, false,
                "Basic zombie with a 1100 HP metal bucket (magnet-vulnerable).");
        put(ZombieType.BLOCKHEAD, 190, 2200, false, 0.185f, 100, 700, false, false,
                "Carries a 2200 HP brick block on its head (not metal).");
        put(ZombieType.KNIGHT, 190, 3200, true, 0.185f, 100, 550, false, false,
                "Knight's crown AND shoulder armor, 1600 HP each piece (metal).");
        put(ZombieType.GARGANTUAR, 3600, 0, false, 0.24f, 1500, 1500, false, false,
                "3600 HP giant; crushes plants with a 1500 DPS smash.");
        put(ZombieType.IMP, 190, 0, false, 0.22f, 100, 100, false, false,
                "Tiny and quick; thrown deep into your defence by the Gargantuar.");
        ///Ancient Egypt
        put(ZombieType.RA, 190, 0, false, 0.2f, 100, 100, false, false,
                "Raises its staff and steals sun lying on the lawn.");
        put(ZombieType.EXPLORER, 250, 0, false, 0.25f, 100, 250, false, false,
                "Burns the first plant it reaches with its torch.");
        put(ZombieType.TOMBRAISER, 380, 0, false, 0.185f, 100, 300, false, false,
                "Summons tombstones on the lawn as it shambles.");
        ///Frostbite Caves
        put(ZombieType.DODO_RIDER, 490, 0, false, 0.3f, 100, 600, true, false,
                "Rides a dodo and hops over your front line.");
        put(ZombieType.HUNTER, 700, 0, false, 0.12f, 100, 500, true, false,
                "Throws freezing snowballs that ice your plants over.");
        put(ZombieType.TROGLOBITE, 470, 0, false, 0.185f, 100, 600, true, false,
                "Pushes frozen blocks that crush your plants.");
        ///Big Wave Beach
        put(ZombieType.FISHERMAN, 1000, 0, false, 0.185f, 100, 700, false, false,
                "Hooks your plants and reels them into the water.");
        put(ZombieType.OCTOPUS, 910, 0, false, 0.12f, 100, 900, false, false,
                "Throws octopi that bind and disable your plants.");
        put(ZombieType.SNORKEL, 350, 0, false, 0.185f, 100, 200, false, false,
                "Submerges in water where straight shots cannot hit it.");
        ///Dark Ages
        put(ZombieType.JESTER, 420, 0, false, 0.2f, 100, 450, false, false,
                "Spins and reflects straight projectiles back at your plants.");
        put(ZombieType.WIZARD, 490, 0, false, 0.12f, 100, 800, false, false,
                "Turns your plants into harmless sheep.");
        put(ZombieType.KING, 1000, 0, false, 0.0f, 100, 750, false, false,
                "Sits on his throne and knights basic zombies into Knights.");
        put(ZombieType.IMP_DRAGON, 190, 0, false, 0.185f, 100, 150, false, true,
                "Small dragon immune to every fire projectile.");
        ///Other
        put(ZombieType.ALL_STAR, 1100, 0, false, 0.16f, 100, 1000, false, false,
                "Football bruiser that sprints and tackles your first plant.");
        put(ZombieType.ARCADE, 490, 0, false, 0.19f, 100, 600, false, false,
                "Pushes an arcade cabinet that spawns 8-bit zombies.");
        put(ZombieType.PARASOL, 350, 0, false, 0.25f, 100, 200, false, false,
                "Her parasol deflects every lobbed projectile.");
        put(ZombieType.TURQUOISE_SKULL, 250, 0, false, 0.185f, 100, 500, false, false,
                "Its crystal skull channels a plant-melting energy beam.");
        put(ZombieType.PROSPECTOR, 190, 0, false, 0.16f, 100, 200, false, false,
                "Lights a dynamite stick and rockets behind your defence.");
        put(ZombieType.PIANIST, 840, 0, false, 0.12f, 4000, 450, false, false,
                "Rolling piano that flattens plants (4000 DPS) and changes lanes.");
        put(ZombieType.NEWSPAPER, 460, 800, false, 0.22f, 200, 700, false, false,
                "800 HP newspaper shield; furious 200 DPS bites once it tears.");
        put(ZombieType.BARREL_ROLLER, 190, 800, false, 0.185f, 100, 400, false, false,
                "Rolls an 800 HP barrel that bursts into Imps when destroyed.");
        ///Zombotany
        put(ZombieType.PEASHOOTER_ZOMBIE, 200, 0, false, 0.2f, 100, 200, false, false,
                "Zombie with a pea-shooting head - shoots your plants from afar.");
        put(ZombieType.WALLNUT_ZOMBIE, 1100, 0, false, 0.15f, 100, 300, false, false,
                "Wall-nut head makes it a walking shield.");
        put(ZombieType.JALAPENO_ZOMBIE, 200, 0, false, 0.25f, 100, 300, false, false,
                "Explodes in a fiery lane blast when it reaches your plants.");
        put(ZombieType.SQUASH_ZOMBIE, 250, 0, false, 0.25f, 100, 300, false, false,
                "Squashes the first plant it reaches, then walks on.");
        ///
        put(ZombieType.SUN_PRODUCER_ZOMBIE, 1300, 0, false, 0.2f, 100, 400, false, false, "Buckethead-tough zombie that produces ever-growing sun for its master.");
    }
/// json
    @SuppressWarnings("unchecked")
    private static void loadFromJson() {
        Path dir = findDataDir();
        if (dir == null) {
            System.out.println("[!] data/zombies.json not found - using built-in zombie stats.");
            return;
        }
        try {
            Map<String, Integer> armorHealth = new HashMap<>();
            Map<String, Boolean> armorMetal = new HashMap<>();
            List<Object> armorRoot =
                    (List<Object>) Json.parseFile(dir.resolve("ArmorTypeData.json"));
            for (Object element : armorRoot) {
                Map<String, Object> entry = (Map<String, Object>) element;
                String alias = (String) ((List<Object>) entry.get("aliases")).get(0);
                Map<String, Object> data = (Map<String, Object>) entry.get("objdata");
                armorHealth.put(alias, intOf(data, "BaseHealth", 0));
                List<Object> flags = (List<Object>) data.get("ArmorFlags");
                armorMetal.put(alias, flags != null && flags.contains("metallic"));
            }
            ///Zombie property sheet: alias -> objdata.
            Map<String, Map<String, Object>> sheets = new HashMap<>();
            List<Object> zombieRoot = (List<Object>) Json.parseFile(dir.resolve("zombies.json"));
            for (Object element : zombieRoot) {
                Map<String, Object> entry = (Map<String, Object>) element;
                String alias = (String) ((List<Object>) entry.get("aliases")).get(0);
                sheets.put(alias, (Map<String, Object>) entry.get("objdata"));
            }
            for (String[] pair : JSON_ALIASES) {
                Map<String, Object> data = sheets.get(pair[0]);
                if (data == null) continue;
                ZombieType type = ZombieType.valueOf(pair[1]);
                Stats defaults = SHEET.get(type);

                int hp = intOf(data, "Hitpoints", defaults.hp);
                float speed = (float) doubleOf(data, "Speed", defaults.cellsPerSecond);
                int dps = intOf(data, "EatDPS", defaults.damagePerSecond);
                if (dps == 0) dps = intOf(data, "SmashDamage", defaults.damagePerSecond);
                int waveCost = intOf(data, "WavePointCost", defaults.waveCost);
                int armorHp = 0;
                boolean metal = false;
                Object armorProps = data.get("ZombieArmorProps");
                if (armorProps instanceof List) {
                    for (Object ref : (List<Object>) armorProps) {
                        String alias = rtidAlias(String.valueOf(ref));
                        armorHp += armorHealth.getOrDefault(alias, 0);
                        metal = metal || Boolean.TRUE.equals(armorMetal.get(alias));
                    }
                }

                boolean freezeImmune = Boolean.TRUE.equals(data.get("ChillInsteadOfFreeze"));
                boolean fireImmune = data.containsKey("FireDamageMultiplier") && doubleOf(data, "FireDamageMultiplier", 1) == 0;

                put(type, hp, armorHp, metal, speed, dps, waveCost, freezeImmune, fireImmune, defaults.description);
            }
        } catch (Exception e) {
            System.out.println("[!] Could not read zombie JSON data (" + e.getMessage() + ") - using built-in stats.");}
    }
    private static Path findDataDir() {
        for (String candidate : new String[]{"data", "../data", "pvz/data"}) {
            if (Files.exists(Paths.get(candidate, "zombies.json")) && Files.exists(Paths.get(candidate, "ArmorTypeData.json"))) {
                return Paths.get(candidate);}
        }
        return null;
    }
    private static String rtidAlias(String rtid) {
        int open = rtid.indexOf('(');
        int at = rtid.indexOf('@');
        return open >= 0 && at > open ? rtid.substring(open + 1, at) : rtid;
    }

    private static int intOf(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? (int) Math.round(((Number) value).doubleValue()) : fallback;
    }

    private static double doubleOf(Map<String, Object> data, String key, double fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private ZombieCatalog() {}
    public static Stats of(ZombieType type) {
        Stats stats = SHEET.get(type);
        if (stats == null) throw new IllegalArgumentException("No catalog entry for " + type);
        return stats;
    }
}
