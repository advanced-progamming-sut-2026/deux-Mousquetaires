package pvz.model.enums;

public enum PlantTag {
    /// groups
    SHOOTER,         /// fires projectiles along a lane
    SUN_PRODUCER,    /// generates sun currency
    WALL,            /// high HP defensive blocker
    EXPLOSIVE,       /// AoE damage on activation
    MELEE,           /// close combat attacker
    HOMING,          /// targets specific zombies across lanes
    MODIFIER,        /// passively boosts other plants
    STRIKE_THROUGH,  /// projectiles pierce through multiple zombies
    LOBBER,          /// lobbed shots that ignore obstacles and shields
    MINT,            /// boosts its whole family on planting then die
    /// Tags
    DAY,
    NIGHT,
    SHROOM,
    RAMP_UP,
    PEA,
    ICE,
    FIRE,
    STACK,
    CHARGE,
    MAGIC,
    POISON,
    WATER,
    AOE,
    TRAP,
    MOVE_ZOMBIES,
    SUN,
    INSTANT,         // activates once then self-destructs
    SINGLE_USE,      // one use plant
    CACTI_FAMILY     //cactus / thistle family
}
