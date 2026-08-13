package pvz.model.entity.plant;

import pvz.model.enums.PlantType;

public class CatalogPlant extends Plant {

    private final PlantType type;

    public CatalogPlant(PlantType type, int x, int y) {
        super(x, y, PlantCatalog.of(type).hp,
                type.name().toLowerCase(),
                PlantCatalog.of(type).sunCost,
                PlantCatalog.of(type).rechargeTicks,
                PlantCatalog.of(type).range,
                PlantCatalog.of(type).family,
                PlantCatalog.of(type).tags);
        this.type = type;
    }

    public PlantType getType() {
        return type;
    }
    public int getBaseDamage() {
        return PlantCatalog.of(type).damage;
    }
    public int getActionIntervalTicks() {
        return PlantCatalog.of(type).actionIntervalTicks;
    }
    @Override
    public void update() {
        tickBoost();
    }

    @Override
    public void draw() {
    }

    @Override
    public void attack() {}

    @Override
    public void produce() {}
    @Override
    public void onPlantFood() {}
    @Override
    public void die() {onDeath();}
}
