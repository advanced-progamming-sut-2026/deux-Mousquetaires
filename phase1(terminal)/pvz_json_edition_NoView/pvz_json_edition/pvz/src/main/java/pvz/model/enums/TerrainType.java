package pvz.model.enums;

public enum TerrainType {
    GRASS,          // normal landing
    TOMB,           // Egypt / Dark Ages tombstone: 700 HP, blocks straight shots, unplantable
    ICE_SLIP_UP,    // Frostbite Caves: zombie stepping here slides one row up
    ICE_SLIP_DOWN,  // Frostbite Caves: zombie stepping here slides one row down
    FROZEN,         // Frostbite Caves: frozen block (600 HP) holding a plant or zombie
    WATER,          // Big Wave Beach: requires Lily Pad or a water plant
    LOW_TIDE,       // Big Wave Beach: zombies may emerge from beneath
    NECROMANCY      // Dark Ages: tomb on this cell can spawn a zombie each wave
}
