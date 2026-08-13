package pvz.model.entity.plant;

import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantTag;
import pvz.model.enums.PlantType;
import pvz.util.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class PlantCatalog {

    public static final class Stats {
        public final int sunCost;
        public final int hp;
        public final int rechargeTicks; /// seed packet cooldown
        public final int damage; /// damage per shot / explosion
        public final int actionIntervalTicks; /// shot / production interval
        public final float range;
        public final PlantFamily family;
        public final EnumSet<PlantTag> tags;
        public final String description;
        public final String plantFoodEffect;
        public final String[] upgrades;      ///"Level 2" /// "Level 3" /// "Level 4"

        Stats(int sunCost, int hp, int rechargeTicks, int damage,
              int actionIntervalTicks, float range, PlantFamily family,
              EnumSet<PlantTag> tags, String description,
              String plantFoodEffect, String[] upgrades) {
            this.sunCost = sunCost;
            this.hp = hp;
            this.rechargeTicks = rechargeTicks;
            this.damage = damage;
            this.actionIntervalTicks = actionIntervalTicks;
            this.range = range;
            this.family = family;
            this.tags = tags;
            this.description = description;
            this.plantFoodEffect = plantFoodEffect;
            this.upgrades = upgrades;
        }
    }

    private static final Map<PlantType, Stats> SHEET = new EnumMap<>(PlantType.class);

    private static void put(PlantType type, int sunCost, int hp, int rechargeTicks,
                            int damage, int intervalTicks, float range,
                            PlantFamily family, EnumSet<PlantTag> tags, String desc,
                            String plantFood, String level2, String level3, String level4) {
        SHEET.put(type, new Stats(sunCost, hp, rechargeTicks, damage,
                intervalTicks, range, family, tags, desc, plantFood,
                new String[]{level2, level3, level4}));
    }

    static {
        seedBuiltInDefaults();
        loadFromJson();
    }
    private static void seedBuiltInDefaults() {
        ///sun produce
        put(PlantType.SUNFLOWER, 50, 300, 50, 0, 240, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.DAY, PlantTag.SUN),
                "Produces a 25-value sun every 24 seconds.",
                "Instantly produces 150 sun.",
                "Prod. Time -2s", "HP +150", "Double Sun Chance");
        put(PlantType.TWIN_SUNFLOWER, 125, 300, 150, 0, 240, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.DAY, PlantTag.SUN),
                "Twin heads produce double sun every 24 seconds.",
                "Instantly produces 250 sun.",
                "Prod. Time -2s", "HP +150", "Cost -25");
        put(PlantType.SUN_SHROOM, 25, 300, 50, 0, 240, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.NIGHT, PlantTag.SHROOM, PlantTag.RAMP_UP, PlantTag.SUN),
                "Cheap mushroom that starts small and ramps up its sun output.",
                "Instantly grows to full size and produces 225 sun.",
                "Grow Time -5s", "HP +150", "Double Sun Chance");
        put(PlantType.PRIMAL_SUNFLOWER, 75, 300, 50, 0, 240, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.DAY, PlantTag.SUN),
                "Prehistoric sunflower that drops bigger suns.",
                "Instantly produces 225 sun.",
                "Prod. Time -2s", "HP +150", "Cost -25");
        put(PlantType.GOLD_BLOOM, 0, 300, 750, 0, 0, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.SUN, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Free plant that bursts into 375 sun immediately, then vanishes.",
                "None (instant-use plant).",
                "Cooldown -5s", "Sun +50", "Cost -25");
        put(PlantType.MARIGOLD, 50, 300, 100, 0, 240, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.SUN_PRODUCER, PlantTag.DAY, PlantTag.SUN),
                "Greenhouse favourite; harvested Marigolds are sold for 500 coins.",
                "Instantly produces 150 sun.",
                "Prod. Time -2s", "HP +150", "Cost -25");
        ///shooters
        put(PlantType.PEASHOOTER, 100, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.DAY),
                "Fires one pea (20 dmg) every 1.5 seconds along its lane.",
                "Rapid-fire volley for a few seconds.",
                "Dmg +10", "HP +150", "Cost -25");
        put(PlantType.REPEATER, 200, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.DAY),
                "Fires two peas per volley (20 dmg each).",
                "Heavy volley plus one giant pea (20x damage).",
                "Dmg +10", "HP +200", "Cost -25");
        put(PlantType.THREEPEATER, 300, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.DAY),
                "Shoots peas into its own lane and both neighbouring lanes.",
                "Fan volley across every lane.",
                "Cost -25", "Dmg +10", "HP +200");
        put(PlantType.SNOW_PEA, 150, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.ICE),
                "Frozen peas chill and slow zombies down.",
                "Freezes the lane with an icy volley.",
                "Dmg +10", "Chill Time +2s", "Cost -25");
        put(PlantType.ROTOBAGA, 150, 300, 50, 30, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER),
                "Hovers and fires three 10-dmg shots diagonally per volley.",
                "Volley in all four diagonal directions.",
                "Dmg +10", "HP +150", "Cost -25");
        put(PlantType.PEA_POD, 125, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.STACK),
                "Stackable pod - each extra head adds another 20-dmg pea (up to 100).",
                "Fires one giant pea per head (20x damage).",
                "Dmg +10", "HP +200", "Cost -25");
        put(PlantType.SPLIT_PEA, 125, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA),
                "Two-headed shooter that also covers the lane behind it.",
                "Simultaneous volleys forward and backward.",
                "Dmg +10", "HP +200", "Cost -25");
        put(PlantType.CITRON, 350, 300, 50, 800, 90, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.CHARGE),
                "Charges for 9 seconds, then fires an 800-dmg plasma ball.",
                "Fires a plasma ball that clears the whole lane.",
                "Charge Time -1s", "Dmg +150", "Cost -50");
        put(PlantType.BOWLING_BULB, 200, 300, 50, 40, 20, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.CHARGE),
                "Lobs ricocheting bulbs: cyan 40 (2s), blue 120 (5s), orange 180 (10s).",
                "Hurls 3 huge ricocheting explosive bulbs.",
                "Regen -1s", "Dmg +15", "Cost -25");
        put(PlantType.FIRE_PEASHOOTER, 175, 300, 50, 40, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.FIRE),
                "Fires flaming peas (40 dmg) that thaw frozen tiles.",
                "Fiery volley across the whole lane.",
                "Dmg +10", "HP +200", "Cost -25");
        put(PlantType.STARFRUIT, 150, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER),
                "Shoots stars in 5 directions at once.",
                "Star volley in every direction.",
                "Atk Speed +10%", "Dmg +10", "Cost -25");
        put(PlantType.GOO_PEASHOOTER, 125, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA, PlantTag.POISON),
                "Fires poison goo peas that keep hurting zombies over time.",
                "Poison volley that intoxicates zombies on the spot.",
                "Dmg/Tick +5", "HP +150", "Cost -25");
        put(PlantType.MEGA_GATLING_PEA, 400, 300, 50, 20, 15, 9f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.PEA),
                "Sprays four peas per volley (20 dmg each).",
                "Massive extended volley plus 4 giant peas.",
                "Dmg +10", "Plant Food Chance +5%", "Cost -50");
        put(PlantType.SEA_SHROOM, 0, 300, 150, 20, 15, 3f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.SHROOM, PlantTag.WATER),
                "Free aquatic mushroom; short range, lives for 60 seconds.",
                "Volley + resets the lifespan of every Sea-shroom.",
                "Range +1 Tile", "Dmg +5", "Lifespan +10s");
        put(PlantType.PUFF_SHROOM, 0, 300, 50, 20, 15, 3f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.SHOOTER, PlantTag.SHROOM, PlantTag.NIGHT),
                "Free mushroom; short range, lives for 60 seconds.",
                "Volley + resets the lifespan of every Puff-shroom.",
                "Lifespan +10s", "Dmg +10", "Range +1 Tile");
        ///homing
        put(PlantType.CAULIPOWER, 250, 300, 150, 99999, 120, 9f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.HOMING, PlantTag.MAGIC, PlantTag.CHARGE),
                "Every 12 seconds zaps the nearest zombie out of the fight.",
                "Hypnotizes several random zombies on the lawn.",
                "Cooldown -2s", "HP +150", "Cost -50");
        put(PlantType.ELECTRIC_BLUEBERRY, 150, 300, 150, 5000, 120, 9f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.HOMING, PlantTag.CHARGE),
                "Summons a 5000-dmg lightning cloud onto a zombie every 12 seconds.",
                "Completely destroys 3 random zombies.",
                "Cooldown -2s", "Target Priority Up", "Cost -25");
        put(PlantType.MAGNET_SHROOM, 100, 300, 150, 0, 100, 3f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.HOMING, PlantTag.SHROOM, PlantTag.MAGIC),
                "Rips metal armor (buckets, helmets) off nearby zombies.",
                "Attracts and digests several metal objects at once.",
                "Range +1 Tile", "Cooldown -5s", "HP +200");
        put(PlantType.CAT_TAIL, 175, 300, 200, 15, 15, 9f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.HOMING, PlantTag.WATER),
                "Fires homing spikes at any zombie on any lane.",
                "Volley of homing spikes.",
                "Dmg +10", "HP +200", "Cost -25");
        put(PlantType.HOMING_THISTLE, 250, 300, 150, 20, 15, 9f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.HOMING, PlantTag.CACTI_FAMILY),
                "Fires homing thorns at the closest zombie on any lane.",
                "Volley of homing thorns.",
                "Dmg +10", "HP +150", "Cost -25");
        /// strike
        put(PlantType.CACTUS, 175, 300, 50, 30, 15, 9f, PlantFamily.STRIKE_THROUGH,
                EnumSet.of(PlantTag.STRIKE_THROUGH, PlantTag.CACTI_FAMILY),
                "Spikes pierce through up to 3 zombies in a row (30 dmg).",
                "Electric spikes with high damage and unlimited pierce.",
                "Pierce +1", "Dmg +10", "Cost -25");
        put(PlantType.FUME_SHROOM, 125, 300, 50, 20, 15, 4f, PlantFamily.STRIKE_THROUGH,
                EnumSet.of(PlantTag.STRIKE_THROUGH, PlantTag.SHROOM, PlantTag.NIGHT),
                "Breathes fumes that hit every zombie in a 4-tile cloud.",
                "Launches a huge fume cloud that pushes zombies back.",
                "Range +1 Tile", "Dmg +10", "Cost -25");
        ///lobb
        put(PlantType.CABBAGE_PULT, 100, 300, 50, 40, 29, 9f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.LOBBER),
                "Lobs cabbages (40 dmg) over tombstones and shields.",
                "Lobs cabbages at several random zombies.",
                "Dmg +10", "Atk Speed +15%", "HP +150");
        put(PlantType.KERNEL_PULT, 100, 300, 50, 20, 29, 9f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.LOBBER),
                "Lobs kernels (20 dmg) or butter (40 dmg) that briefly stuns.",
                "Butters the head of every zombie on the lawn.",
                "Butter +5%", "Dmg +10", "HP +150");
        put(PlantType.MELON_PULT, 325, 300, 50, 80, 29, 9f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.LOBBER, PlantTag.AOE),
                "Heavy melons (80 dmg) splash nearby zombies.",
                "Lobs a giant melon at random zombies.",
                "Cost -25", "AoE Dmg +15", "Dmg +30");
        put(PlantType.WINTER_MELON, 500, 300, 50, 80, 29, 9f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.LOBBER, PlantTag.ICE, PlantTag.AOE),
                "Icy melons (80 dmg) splash and chill groups of zombies.",
                "Lobs an icy melon at random zombies.",
                "Cost -50", "AoE Dmg +15", "Cost -25");
        put(PlantType.PEPPER_PULT, 200, 300, 50, 50, 29, 9f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.LOBBER, PlantTag.FIRE, PlantTag.AOE),
                "Lobs burning peppers (50 dmg) that warm nearby tiles.",
                "Lobs big peppers at 3 random zombies.",
                "Dmg +15", "Warmth Radius +1", "Cost -25");
        ///explose
        put(PlantType.POTATO_MINE, 25, 300, 250, 1800, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.TRAP, PlantTag.SINGLE_USE),
                "Arms in 15 seconds, then erupts for 1800 dmg on contact.",
                "Arms instantly and throws out 2 clone mines.",
                "Arm Time -3s", "Cooldown -5s", "Dmg +600");
        put(PlantType.PRIMAL_POTATO_MINE, 50, 300, 50, 2400, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.TRAP, PlantTag.SINGLE_USE),
                "Arms in only 5 seconds and blasts a 3x3 area for 2400 dmg.",
                "Arms instantly and throws 2 mines to other spots.",
                "Arm Time -1s", "Cooldown -3s", "Dmg +400");
        put(PlantType.CHERRY_BOMB, 150, 300, 350, 1800, 0, 1f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Explodes in a 3x3 blast for 1800 dmg.",
                "None (instant-use plant).",
                "Cooldown -5s", "Dmg +600", "Cost -25");
        put(PlantType.SQUASH, 50, 300, 200, 1800, 0, 1.5f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.TRAP, PlantTag.SINGLE_USE),
                "Squashes the first zombie that comes close (1800 dmg).",
                "Squashes 2 random zombies on the lawn.",
                "Cooldown -3s", "Dmg +600", "Can crush 2x");
        put(PlantType.GRAPESHOT, 150, 300, 350, 1800, 0, 1f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Explodes for 1800 dmg and bounces burning grapes around for 5s.",
                "None (instant-use plant).",
                "Dmg +600", "Bounces +1", "Cost -25");
        put(PlantType.JALAPENO, 125, 300, 350, 1800, 0, 9f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.FIRE, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Scorches its whole lane for 1800 dmg and melts ice.",
                "None (instant-use plant).",
                "Cooldown -5s", "Dmg +600", "Cost -25");
        put(PlantType.DOOM_SHROOM, 125, 300, 150, 1800, 0, 9f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.SHROOM, PlantTag.NIGHT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Nukes the entire lawn for 1800 dmg.",
                "None (instant-use plant).",
                "Cooldown -5s", "Dmg +800", "Cost -50");
        put(PlantType.TANGLE_KELP, 25, 300, 150, 99999, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.TRAP, PlantTag.WATER, PlantTag.SINGLE_USE),
                "Drags the first zombie that swims over it underwater.",
                "Drags several random zombies in water underwater.",
                "Cooldown -5s", "Targets +1", "Cost -25");
        put(PlantType.ICEBERG_LETTUCE, 0, 300, 200, 0, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.TRAP, PlantTag.ICE, PlantTag.SINGLE_USE),
                "Free trap that freezes the zombie stepping on it.",
                "Freezes every zombie on the screen.",
                "Cooldown -2s", "Freeze Time +2s", "Cost -0");
        put(PlantType.ICE_SHROOM, 75, 300, 500, 0, 0, 9f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.SHROOM, PlantTag.ICE, PlantTag.NIGHT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Instantly freezes every zombie on the lawn for a few seconds.",
                "None (instant-use plant).",
                "Freeze Time +2s", "Cooldown -5s", "Dmg +50");
        put(PlantType.HOT_POTATO, 0, 300, 50, 0, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.FIRE, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Free plant that melts the frozen tile it is planted on.",
                "None (instant-use plant).",
                "Cooldown -2s", "Melt Area 3x3", "Explode on Finish");
        put(PlantType.GRAVE_BUSTER, 0, 300, 100, 99999, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.EXPLOSIVE, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Plant it on a tombstone to devour it completely.",
                "None (instant-use plant).",
                "Eat Time -1s", "Cooldown -2s", "Explode on Finish");
        /// mele
        put(PlantType.BONK_CHOY, 150, 300, 50, 15, 2, 1.5f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MELEE),
                "Punches nearby zombies 4 times a second (15 dmg per punch).",
                "Punch flurry in a 3x3 area around itself.",
                "Dmg +5", "Atk Speed +10%", "HP +200");
        put(PlantType.PHAT_BEET, 150, 300, 50, 15, 20, 1.5f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MELEE, PlantTag.AOE),
                "Pounds every zombie in a 3x3 area around it (15 dmg).",
                "Powerful sonic blast hitting all nearby zombies.",
                "Dmg +10", "Atk Speed +10%", "HP +200");
        put(PlantType.CHOMPER, 150, 300, 50, 99999, 400, 1.5f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MELEE),
                "Swallows a zombie whole, then chews for 40 seconds.",
                "Swallows 3 zombies at once from afar.",
                "Digest -2s", "HP +200", "Digest -3s");
        put(PlantType.WASABI_WHIP, 150, 300, 50, 40, 20, 1.5f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MELEE, PlantTag.FIRE),
                "Fiery whip lashes zombies in front and behind (40 dmg).",
                "Whip spin covering a 3x3 area.",
                "Dmg +10", "Range +1 Tile", "HP +200");
        put(PlantType.KIWIBEAST, 175, 300, 50, 15, 20, 1.5f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MELEE, PlantTag.AOE, PlantTag.RAMP_UP),
                "Grows angrier as it fights: 15 / 30 / 45 dmg by size.",
                "Leaps and slams the ground with area damage.",
                "HP +200", "Dmg +15", "Max Size +1");
        ///wallnut
        put(PlantType.WALL_NUT, 50, 4000, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL),
                "4000 HP blocker that keeps the horde chewing.",
                "Gains a permanent 4000 HP extra coating.",
                "HP +1000", "Cooldown -5s", "HP +1500");
        put(PlantType.TALL_NUT, 125, 8000, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL),
                "8000 HP tower that cannot be vaulted or jumped over.",
                "Gains a permanent 8000 HP coating.",
                "HP +2000", "Cooldown -5s", "HP +3000");
        put(PlantType.ENDURIAN, 100, 3000, 150, 20, 20, 1f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.MELEE),
                "Spiky 3000 HP wall that stabs biters for 20 dmg.",
                "Gains metal armor and higher reflect damage.",
                "Reflect Dmg +5", "HP +1000", "Cost -25");
        put(PlantType.GARLIC, 50, 300, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.MOVE_ZOMBIES),
                "Zombies that bite it get disgusted and switch lanes.",
                "Forces every zombie in the lane into other lanes.",
                "HP +150", "Cooldown -3s", "HP +250");
        put(PlantType.SWEET_POTATO, 150, 3000, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.MOVE_ZOMBIES),
                "So sweet that zombies in nearby lanes switch to attack it.",
                "Attracts all nearby zombies and fully heals.",
                "HP +1000", "Cooldown -5s", "HP +1500");
        put(PlantType.EXPLODE_O_NUT, 50, 4000, 200, 1800, 0, 1f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.EXPLOSIVE, PlantTag.SINGLE_USE),
                "4000 HP wall that detonates a 3x3 blast when destroyed.",
                "Gains metal armor (also explodes when the armor breaks).",
                "HP +1000", "Explode Dmg +200", "Cost -25");
        put(PlantType.PUMPKIN, 150, 4000, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.STACK),
                "4000 HP shell that can be placed over another plant.",
                "Gains powerful metal armor.",
                "HP +1000", "Cooldown -5s", "HP +1500");
        put(PlantType.SUN_BEAN, 50, 1000, 200, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.SUN),
                "Every bite it suffers drops 5 sun for you.",
                "Gains powerful metal armor.",
                "Sun Drop +5", "HP +150", "Cost -25");
        ///modify
        put(PlantType.TORCHWOOD, 175, 300, 50, 0, 0, 0f, PlantFamily.MODIFIER,
                EnumSet.of(PlantTag.MODIFIER, PlantTag.FIRE),
                "Peas passing through its flame deal double fire damage.",
                "Blue flame (triples pea damage).",
                "HP +300", "AoE on Death", "Cost -25");
        put(PlantType.HYPNO_SHROOM, 125, 300, 200, 0, 0, 0f, PlantFamily.MODIFIER,
                EnumSet.of(PlantTag.MODIFIER, PlantTag.SHROOM, PlantTag.MAGIC, PlantTag.TRAP, PlantTag.NIGHT, PlantTag.SINGLE_USE),
                "The zombie that bites it is hypnotized out of the fight.",
                "Turns the biting zombie into an allied Gargantuar.",
                "Cost -25", "Zombie HP Buff", "Zombie Dmg Buff");
        put(PlantType.IMITATER, 0, 300, 0, 0, 0, 0f, PlantFamily.MODIFIER,
                EnumSet.of(PlantTag.MODIFIER),
                "Free mimic that lets you bring a copy of another seed.",
                "None (depends on the copied plant).",
                "Cooldown -2s", "Cost -25", "plant food on enterance");
        put(PlantType.LILY_PAD, 25, 300, 50, 0, 0, 0f, PlantFamily.MODIFIER,
                EnumSet.of(PlantTag.MODIFIER, PlantTag.WATER, PlantTag.STACK),
                "Floating pad that lets land plants stand on water.",
                "Creates copies of itself on empty water tiles.",
                "Cost -25", "HP +200", "Cooldown -2s");
        ///mints
        put(PlantType.ENLIGHTEN_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.SUN_PRODUCER,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Sun Producer on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.APPEASE_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.SHOOTER,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Shooter on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.ARMA_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.LOBBER,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Lobber on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.BOMBARD_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.EXPLOSIVE,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Explosive on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.ENFORCE_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.MELEE,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Melee plant on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.REINFORCE_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Wall-nut on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.ENCHANT_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.MODIFIER,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Modifier on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.PIERCE_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.STRIKE_THROUGH,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Strike-through plant, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        put(PlantType.CATTAIL_MINT, 0, 300, 850, 0, 0, 0f, PlantFamily.HOMING,
                EnumSet.of(PlantTag.MINT, PlantTag.INSTANT, PlantTag.SINGLE_USE),
                "Applies plant food to every Homing plant on the lawn, then vanishes.",
                "None (instant-use plant).",
                "Duration +1s", "Cooldown -5s", "reset family cooldowns");
        ///minigame
        put(PlantType.BOWLING_WALLNUT, 0, 300, 0, 550, 0, 9f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.SINGLE_USE),
                "Rolls forward and ricochets 45 degrees on first hit, 90 on later hits.",
                "None (minigame plant).",
                "-", "-", "-");
        put(PlantType.GIANT_WALLNUT, 0, 300, 0, 9999, 0, 9f, PlantFamily.WALL_NUT,
                EnumSet.of(PlantTag.WALL, PlantTag.SINGLE_USE),
                "Crushes every zombie in its path without bouncing.",
                "None (minigame plant).",
                "-", "-", "-");
    }

    @SuppressWarnings("unchecked")
    private static void loadFromJson() {
        Path file = findDataFile();
        if (file == null) {
            return;}
        try {
            List<Object> root = (List<Object>) Json.parseFile(file);
            for (Object element : root) {
                Map<String, Object> data = (Map<String, Object>) element;
                PlantType type;
                try {
                    type = PlantType.valueOf(normalise((String) data.get("name")));
                } catch (IllegalArgumentException unknownPlant) {
                    continue; // species not present in this edition
                }
                Stats defaults = SHEET.get(type);

                int cost = intOf(data, "cost", defaults.sunCost);
                int hp = intOf(data, "baseHp", 0);
                if (hp <= 0) hp = defaults.hp;
                int damage = intOf(data, "damage", defaults.damage);
                int intervalTicks = (int) Math.round(doubleOf(data, "actionInterval", 0) * 10);
                if (intervalTicks <= 0) intervalTicks = defaults.actionIntervalTicks;
                int rechargeTicks = (int) Math.round(doubleOf(data, "recharge", defaults.rechargeTicks / 10.0) * 10);
                PlantFamily family = defaults.family;
                Object category = data.get("category");
                if (category instanceof String) {
                    try {
                        family = PlantFamily.valueOf((String) category);
                    } catch (IllegalArgumentException ignored) {}
                }

                String plantFood = plantFoodText(data);
                String[] upgrades = upgradeTexts(data, defaults.upgrades);

                SHEET.put(type, new Stats(cost, hp, rechargeTicks, damage,
                        intervalTicks, defaults.range, family, defaults.tags,
                        defaults.description, plantFood, upgrades));
            }
        } catch (Exception e) {
        }
    }
    /// json
    private static Path findDataFile() {
        for (String candidate : new String[]{"data", "../data", "pvz/data"}) {
            Path path = Paths.get(candidate, "plants.json");
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }
    private static String normalise(String name) {
        return name.replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase();
    }
    private static String plantFoodText(Map<String, Object> data) {
        String type = String.valueOf(data.get("plantFoodType"));
        if ("NONE".equals(type) || "null".equals(type)) {
            return "None.";
        }
        double value = doubleOf(data, "plantFoodValue", 0);
        return prettify(type) + (value != 0 ? " (" + trim(value) + ")" : "") + ".";
    }
    @SuppressWarnings("unchecked")
    private static String[] upgradeTexts(Map<String, Object> data, String[] fallback) {
        String[] result = fallback.clone();
        Object upgrades = data.get("upgrades");
        if (!(upgrades instanceof List)) return result;
        for (Object element : (List<Object>) upgrades) {
            Map<String, Object> upgrade = (Map<String, Object>) element;
            int level = intOf(upgrade, "level", 0);
            if (level < 2 || level > 4) continue;
            String type = String.valueOf(upgrade.get("type"));
            double value = doubleOf(upgrade, "value", 0);
            String text;
            switch (type) {
                case "BUFF_DAMAGE":          text = "Dmg " + signed(value); break;
                case "BUFF_HP":              text = "HP " + signed(value); break;
                case "BUFF_COST":            text = "Cost " + signed(value); break;
                case "BUFF_ACTION_INTERVAL": text = "Action Time " + signed(value) + "s"; break;
                case "BUFF_RECHARGE":        text = "Recharge " + signed(value) + "s"; break;
                case "SPECIAL_MECHANIC":     text = prettify(String.valueOf(upgrade.get("specialTag"))); break;
                default:                     text = prettify(type) + " " + signed(value); break;
            }
            result[level - 2] = text;
        }
        return result;
    }
    /// double check
    private static String prettify(String constant) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : constant.split("_")) {
            if (word.isEmpty()) continue;
            if (stringBuilder.length() > 0) stringBuilder.append(' ');
            stringBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return stringBuilder.toString();
    }

    private static String signed(double value) {
        String number = trim(value);
        return value > 0 ? "+" + number : number;
    }

    private static String trim(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static int intOf(Map<String, Object> data, String key, int fallback) {
        Object value = data.get(key);
        return value instanceof Number ? (int) Math.round(((Number) value).doubleValue()) : fallback;
    }

    private static double doubleOf(Map<String, Object> data, String key, double fallback) {
        Object value = data.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : fallback;
    }

    private PlantCatalog() {}
    public static Stats of(PlantType type) {
        Stats stats = SHEET.get(type);
        if (stats == null) throw new IllegalArgumentException("No catalog entry for " + type);
        return stats;
    }
}
