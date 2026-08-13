package pvz.model.entity.zombie;

import pvz.model.enums.ZombieType;

public class CatalogZombie extends Zombie {

    private final ZombieType type;
    private int armorHp;
    private boolean enraged; ///newspaper
    public CatalogZombie(ZombieType type, int x, int lane) {
        super(x, lane, ZombieCatalog.of(type).hp,
                type.name().toLowerCase(),
                ZombieCatalog.of(type).cellsPerSecond,
                ZombieCatalog.of(type).damagePerSecond,
                0, ZombieCatalog.of(type).waveCost,lane);
        this.type = type;
        this.armorHp = ZombieCatalog.of(type).armorHp;
    }

    public ZombieType getType() {
        return type;
    }

    public int getArmorHp() {
        return armorHp;
    }

    public boolean hasMetalArmor() {
        return armorHp > 0 && ZombieCatalog.of(type).metalArmor;
    }

    public boolean isFreezeImmune() {
        return ZombieCatalog.of(type).freezeImmune;
    }

    public boolean isFireImmune() {
        return ZombieCatalog.of(type).fireImmune;
    }

    public boolean isEnraged() {
        return enraged;
    }

    public void stripArmor() {
        armorHp = 0;
    }

    public void scaleDifficulty(float multiplier) {
        hp = Math.max(1, (int) (hp * multiplier));
        maxHp = hp;
        armorHp = (int) (armorHp * multiplier);
        damage = Math.max(1, (int) (damage * multiplier));
    }
    public void enrage() {
        if (!enraged) {
            enraged = true;
            speed *= 2f;
        }
    }
    @Override
    public void takeDamage(int d) {
        if (!alive || d <= 0) return;

        if (armorHp > 0) {
            int absorbed = Math.min(armorHp, d);
            armorHp -= absorbed;
            d -= absorbed;
            if (armorHp == 0 && type == ZombieType.NEWSPAPER) enrage();
        }
        if (d > 0) { hp -= d;
            if (hp <= 0) { hp = 0;
                die();}
        }
    }
    public void takeDamageIgnoringArmor(int d) {
        if (!alive || d <= 0) return;
        hp -= d;
        if (hp <= 0) { hp = 0;
            die();
        }
    }

    @Override
    public void freeze(int ticks) {
        if (isFreezeImmune()) return;
        super.freeze(ticks);
    }

    @Override
    public void slow(float mult, int ticks) {
        if (isFreezeImmune()) return;
        super.slow(mult, ticks);
    }
    @Override
    public void die() {onDeath();}
    @Override
    public void specialAbility() {}
}
