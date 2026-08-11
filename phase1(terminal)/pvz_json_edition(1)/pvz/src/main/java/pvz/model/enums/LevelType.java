package pvz.model.enums;

/** All level rule variants described by the project document. */
public enum LevelType {
    NORMAL,
    CONVEYOR_BELT,      /// belt delivers a random plant every 12 seconds and no seed selection
    LOCKED_PLANTS,      /// some seed slots or plants are locked for this level
    SAVE_OUR_SEEDS,     ///pre-placed plants; losing any ----> game over
    SAVE_THE_PLANTS,    /// all plants pre-placed; protect them
    TIMED_WAR,          /// kill n zombies within the time limit
    NIGHT_OPS,          /// no sky-fall sun;for producer plants only
    DEAD_LINE,          /// any zombie crossing a vertical line ---> instant loss
    LOVE_YOUR_PLANTS,   /// lose if more than n plants are lost
    PLANT_WHAT_YOU_GET, /// fixed starting sun, no sky sun, no sunflowers
    SCORED              /// bonus: MeoPoint scored game
}
