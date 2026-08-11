package pvz.model.session;

import pvz.model.enums.PlantFamily;
import pvz.model.enums.PlantTag;
import pvz.model.enums.PlantType;
import pvz.model.enums.TerrainType;
import pvz.model.enums.ZombieType;
import pvz.model.session.GameSession.DamageKind;

import java.util.ArrayList;

class PlantEngine {
    private final GameSession session;

    PlantEngine(GameSession session) {
        this.session = session;
    }

    void tick() {
        for (PlantedUnit unit : new ArrayList<>(session.plants)) {
            if (!session.plants.contains(unit)) continue;
            unit.ageTicks++;
            if (unit.isDisabled() || session.octopusBindings.containsValue(unit)) continue;
            actProximity(unit);
            if (!session.plants.contains(unit)) continue;
            unit.actionTimer--;
            if (unit.actionTimer <= 0) {
                unit.actionTimer = Math.max(1, unit.plant.getActionIntervalTicks());
                actInterval(unit);
            }
        }
    }

    /// proximity
    private void actProximity(PlantedUnit unit) {
        switch (unit.getType()) {
            case POTATO_MINE:
                if (unit.ageTicks >= 150) unit.armed = true;
                if (unit.armed) {
                    ActiveZombie onCell = zombieOnCell(unit.col, unit.row);
                    if (onCell != null) {
                        System.out.println("SPUDOW! The Potato Mine erupts at (" + unit.col + ", " + unit.row + ")!");
                        damageCellArea(unit.col, unit.row, 0, 1800, src(unit));
                        session.plants.remove(unit);
                    }
                }
                break;
            case SQUASH: {
                ActiveZombie near = frontZombieInRow(unit.row, unit.col, 1.5f);
                if (near != null) {
                    System.out.println("The Squash squashes " + near.zombie.getType() + " in lane " + unit.row + "!");
                    session.damageZombie(near, 1800, DamageKind.NORMAL, src(unit));
                    session.plants.remove(unit);
                }
                break;
            }
            case TANGLE_KELP: {
                ActiveZombie onCell = zombieOnCell(unit.col, unit.row);
                if (onCell != null) {
                    System.out.println("The Tangle Kelp drags " + onCell.zombie.getType() + " underwater!");
                    session.killZombie(onCell, src(unit));
                    session.plants.remove(unit);
                }
                break;
            }
            case CHERRY_BOMB:
                if (unit.ageTicks >= 10) {
                    System.out.println("KA-BOOM! The Cherry Bomb explodes at (" + unit.col + ", " + unit.row + ")!");
                    damageCellArea(unit.col, unit.row, 1, 1800, src(unit));
                    session.plants.remove(unit);
                }
                break;
            case JALAPENO:
                if (unit.ageTicks >= 10) {
                    System.out.println("The Jalapeno scorches the whole lane " + unit.row + "!");
                    for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                        if (activeZombie.row == unit.row) session.damageZombie(activeZombie, 1800, DamageKind.FIRE, src(unit));
                    }
                    meltRow(unit.row);
                    session.plants.remove(unit);
                }
                break;
            case ENLIGHTEN_MINT:
            case APPEASE_MINT:
            case ARMA_MINT:
            case BOMBARD_MINT:
            case ENFORCE_MINT:
            case REINFORCE_MINT:
            case ENCHANT_MINT:
            case PIERCE_MINT:
            case CATTAIL_MINT:
                if (unit.ageTicks >= 10) {
                    PlantFamily mintFamily = familyOf(unit);
                    System.out.println("The " + unit.getType() + " invigorates the " + mintFamily + " family and vanishes!");
                    for (PlantedUnit other : new ArrayList<>(session.plants)) {
                        if (other != unit && familyOf(other) == mintFamily) {
                            other.damageBonus *= 2.0;
                            applyPlantFood(other);
                        }
                    }
                    session.plants.remove(unit);
                }
                break;
            case GOLD_BLOOM:
                if (unit.ageTicks >= 10) {
                    System.out.println("The Gold Bloom bursts into 375 sun at (" + unit.col + ", " + unit.row + ")!");
                    session.groundSuns.add(new int[]{unit.col, unit.row, 375});
                    session.plants.remove(unit);
                }
                break;
            case GRAPESHOT:
                if (unit.ageTicks >= 10) {
                    System.out.println("The Grapeshot explodes at (" + unit.col + ", " + unit.row + ") and its grapes bounce around!");
                    damageCellArea(unit.col, unit.row, 1, 1800, src(unit));
                    int bounces = 4;
                    for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                        if (bounces == 0) break;
                        if (Math.abs(activeZombie.cellCol() - unit.col) > 1 || Math.abs(activeZombie.row - unit.row) > 1) {
                            session.damageZombie(activeZombie, 300, DamageKind.FIRE, src(unit));
                            bounces--;
                        }
                    }
                    session.plants.remove(unit);
                }
                break;
            case DOOM_SHROOM:
                if (unit.ageTicks >= 10) {
                    System.out.println("DOOM! The Doom-shroom nukes the entire lawn!");
                    for (ActiveZombie az : new ArrayList<>(session.zombies)) {session.damageZombie(az, 1800, DamageKind.FIRE, src(unit));}
                    session.plants.remove(unit);
                }
                break;
            case ICE_SHROOM:
                if (unit.ageTicks >= 10) {
                    System.out.println("The Ice-shroom freezes every zombie on the lawn!");
                    for (ActiveZombie activeZombie : session.zombies) {activeZombie.freeze(5 * GameSession.TICKS_PER_SECOND);}
                    session.plants.remove(unit);
                }
                break;
            case HOT_POTATO:
                if (unit.ageTicks >= 10) {
                    if (session.terrain[unit.row][unit.col] == TerrainType.FROZEN) {
                        session.terrain[unit.row][unit.col] = TerrainType.GRASS;
                        session.tombHp[unit.row][unit.col] = 0;
                        System.out.println("The Hot Potato melts the frozen tile at (" + unit.col + ", " + unit.row + ")!");
                    }
                    session.plants.remove(unit);
                }
                break;
            case GRAVE_BUSTER:
                if (unit.ageTicks >= 30) {
                    if (session.terrain[unit.row][unit.col] == TerrainType.TOMB) {
                        session.terrain[unit.row][unit.col] = TerrainType.GRASS;
                        session.tombHp[unit.row][unit.col] = 0;
                        System.out.println("The Grave Buster devours the tombstone at (" + unit.col + ", " + unit.row + ")!");
                        session.onTombDestroyed(unit.col, unit.row);
                    }
                    session.plants.remove(unit);
                }
                break;
            case PRIMAL_POTATO_MINE:
                if (unit.ageTicks >= 50) unit.armed = true;
                if (unit.armed) {
                    ActiveZombie prey = zombieOnCell(unit.col, unit.row);
                    if (prey != null) {
                        System.out.println("SPUDOW! The Primal Potato Mine erupts in a " + "3x3 blast at (" + unit.col + ", " + unit.row + ")!");
                        damageCellArea(unit.col, unit.row, 1, 2400, src(unit));
                        session.plants.remove(unit);
                    }
                }
                break;
            case ICEBERG_LETTUCE: {
                ActiveZombie stepper = zombieOnCell(unit.col, unit.row);
                if (stepper != null) {
                    System.out.println("The Iceberg Lettuce freezes " + stepper.zombie.getType() + " solid!");
                    stepper.freeze(10 * GameSession.TICKS_PER_SECOND);
                    session.plants.remove(unit);
                }
                break;
            }
            case HYPNO_SHROOM: {
                ActiveZombie biter = zombieOnCell(unit.col, unit.row);
                if (biter != null) {
                    System.out.println("The Hypno-shroom hypnotizes " + biter.zombie.getType() + " - it wanders off the lawn!");
                    session.killZombie(biter, src(unit));
                    session.plants.remove(unit);
                }
                break;
            }
            case PUFF_SHROOM:
            case SEA_SHROOM:
                if (unit.ageTicks >= 600) {
                    System.out.println("The " + unit.getType() + " withers away (60 second lifespan).");
                    session.plants.remove(unit);
                }
                break;
            default:
                break;
        }
    }

    ///interval
    private void actInterval(PlantedUnit unit) {
        PlantType type = unit.getType();
        if (unit.plant.hasTag(PlantTag.SUN_PRODUCER)) {
            produceSun(unit);
            return;}
        switch (type) {
            case MAGNET_SHROOM:
                magnetPull(unit);
                return;
            case HOMING_THISTLE:
            case CAT_TAIL:
            case CAULIPOWER:
            case ELECTRIC_BLUEBERRY:
                homingShot(unit);
                return;
            default:
                break;
        }
        if (unit.plant.hasTag(PlantTag.LOBBER)) lobShot(unit);
        else if (unit.plant.hasTag(PlantTag.STRIKE_THROUGH)) fumeShot(unit);
        else if (unit.plant.hasTag(PlantTag.SHOOTER)) straightShoot(unit);
        else if (unit.plant.hasTag(PlantTag.MELEE)) meleeStrike(unit);
    }

    private void produceSun(PlantedUnit unit) {
        int value = 25;
        if (unit.getType() == PlantType.SUN_SHROOM) value = unit.ageTicks < 60 * GameSession.TICKS_PER_SECOND ? 15 : 25;
        session.groundSuns.add(new int[]{unit.col, unit.row, value});
        System.out.println("plant " + unit.getType() + " produced a sun at (" + unit.col + ", " + unit.row + ")");
    }

    /// shoot
    private void straightShoot(PlantedUnit unit) {
        float range = rangeOf(unit);
        int obstacleCol = firstObstacleCol(unit.row, unit.col, range);
        ActiveZombie target = firstTargetableZombie(unit.row, unit.col, range);
        if (obstacleCol > 0 && (target == null || obstacleCol < target.cellCol())) {
            hitObstacle(unit, obstacleCol);
            return;}

        if (target == null) return;
        int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
        DamageKind kind = DamageKind.NORMAL;
        if (unit.plant.hasTag(PlantTag.ICE)) kind = DamageKind.ICE;
        if (unit.plant.hasTag(PlantTag.FIRE)) kind = DamageKind.FIRE;
        if (kind != DamageKind.ICE && torchwoodBetween(unit.row, unit.col, target.cellCol())) {
            damage *= 2;
            kind = DamageKind.FIRE;
        }
        /// jester
        if (target.zombie.getType() == ZombieType.JESTER) {
            System.out.println("The Jester reflects the shot!");
            PlantedUnit victim = closestPlantInRow(target.row, target.cellCol());
            if (victim != null) session.damagePlant(victim, damage);
            return;
        }
        String source = src(unit);
        session.damageZombie(target, damage, kind, source);
        /// extra
        int extraShots = unit.getType() == PlantType.REPEATER ? 1 : unit.getType() == PlantType.MEGA_GATLING_PEA ? 3 : 0;
        for (int i = 0; i < extraShots && session.zombies.contains(target); i++) {
            session.damageZombie(target, damage, kind, source);
        }
        if (unit.getType() == PlantType.THREEPEATER) {
            for (int laneShift = -1; laneShift <= 1; laneShift += 2) {
                int sideRow = unit.row + laneShift;
                if (sideRow >= 1 && sideRow <= GameSession.ROWS) {
                    ActiveZombie side = firstTargetableZombie(sideRow, unit.col, range);
                    if (side != null) session.damageZombie(side, damage, kind, source);
                }
            }
        }
        if (unit.getType() == PlantType.SPLIT_PEA) {
            ActiveZombie behind = null;
            for (ActiveZombie activeZombie : session.zombies) {
                if (activeZombie.row == unit.row && activeZombie.col < unit.col && (behind == null || activeZombie.col > behind.col)) {
                    behind = activeZombie;
                }
            }
            if (behind != null) session.damageZombie(behind, damage, kind, source);
        }
    }

    private float rangeOf(PlantedUnit unit) {
        float range = 9;
        try {
            range = pvz.model.entity.plant.PlantCatalog.of(unit.getType()).range;
        } catch (Exception ignored) {}
        return range <= 0 ? 9 : range;
    }

    private void fumeShot(PlantedUnit unit) {
        float range = rangeOf(unit);
        int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
        boolean hitAny = false;
        for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
            if (activeZombie.row == unit.row && activeZombie.col >= unit.col && activeZombie.col <= unit.col + range) {
                session.damageZombie(activeZombie, damage, DamageKind.FUME, src(unit));
                hitAny = true;
            }
        }
        if (hitAny) meltTombsInPath(unit.row, unit.col, (int) (unit.col + range));
    }

    private void lobShot(PlantedUnit unit) {
        ActiveZombie target = null;
        for (ActiveZombie activeZombie : session.zombies) {
            if (activeZombie.row == unit.row && activeZombie.col >= unit.col && (target == null || activeZombie.col < target.col)) {
                target = activeZombie;
            }
        }
        if (target == null) return;
        /// lobbed
        if (target.zombie.getType() == ZombieType.PARASOL) {
            System.out.println("The Parasol zombie deflects the lobbed shot!");
            return;
        }
        int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
        DamageKind kind = unit.plant.hasTag(PlantTag.ICE) ? DamageKind.ICE : DamageKind.LOB;
        String source = src(unit);
        session.damageZombie(target, damage, kind, source);
        /// Melon splash
        if (unit.getType() == PlantType.MELON_PULT || unit.getType() == PlantType.WINTER_MELON) {
            int targetCol = target.cellCol();
            for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                if (activeZombie != target && activeZombie.row == unit.row && Math.abs(activeZombie.cellCol() - targetCol) <= 1) {
                    session.damageZombie(activeZombie, damage / 2, kind, source);
                }
            }
        }
    }

    private void homingShot(PlantedUnit unit) {
        ActiveZombie target = null;
        double best = Double.MAX_VALUE;
        for (ActiveZombie activeZombie : session.zombies) {
            double dist = Math.abs(activeZombie.col - unit.col) + Math.abs(activeZombie.row - unit.row);
            if (dist < best) {
                best = dist;
                target = activeZombie;
            }
        }
        if (target != null) {
            int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
            session.damageZombie(target, damage, DamageKind.NORMAL, src(unit));
        }
    }

    private void magnetPull(PlantedUnit unit) {
        for (ActiveZombie activeZombie : session.zombies) {
            if (activeZombie.zombie.hasMetalArmor() && activeZombie.zombie.getArmorHp() > 0 && Math.abs(activeZombie.row - unit.row) <= 1 && Math.abs(activeZombie.col - unit.col) <= 3) {
                activeZombie.zombie.stripArmor();
                System.out.println("The Magnet-shroom rips the metal armor off " + activeZombie.zombie.getType() + "!");
                return;
            }
        }
    }
    private void meleeStrike(PlantedUnit unit) {
        float range = Math.min(rangeOf(unit), 3f);
        int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
        boolean areaHit = unit.plant.hasTag(PlantTag.AOE);
        DamageKind kind = unit.plant.hasTag(PlantTag.FIRE) ? DamageKind.FIRE : DamageKind.NORMAL;
        for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
            boolean rowOk = areaHit ? Math.abs(activeZombie.row - unit.row) <= 1 : activeZombie.row == unit.row;
            if (rowOk && Math.abs(activeZombie.col - unit.col) <= range) {
                session.damageZombie(activeZombie, damage, kind, src(unit));
                if (!areaHit) return;
            }
        }
    }
    /// Hashtag
    private String src(PlantedUnit unit) {
        return unit.getType().name() + "#" + unit.hashCode();
    }

    /// obstacle
    private int firstObstacleCol(int row, int fromCol, float range) {
        for (int x = fromCol + 1; x <= Math.min(GameSession.COLS, fromCol + range); x++) {
            if (session.terrain[row][x] == TerrainType.TOMB || session.terrain[row][x] == TerrainType.FROZEN) return x;
        }
        return -1;
    }

    private void hitObstacle(PlantedUnit unit, int column) {
        int damage = (int) (unit.plant.getBaseDamage() * unit.damageBonus);
        boolean fire = unit.plant.hasTag(PlantTag.FIRE);
        if (session.terrain[unit.row][column] == TerrainType.FROZEN && fire) {
            session.terrain[unit.row][column] = TerrainType.GRASS;
            session.tombHp[unit.row][column] = 0;
            System.out.println("The fire pea melts the frozen block at (" + column + ", " + unit.row + ")!");
            return;
        }
        session.tombHp[unit.row][column] -= damage;
        if (session.tombHp[unit.row][column] <= 0) {
            String what = session.terrain[unit.row][column] == TerrainType.TOMB ? "tombstone" : "frozen block";
            session.terrain[unit.row][column] = TerrainType.GRASS;
            System.out.println("The " + what + " at (" + column + ", " + unit.row + ") crumbles!");
            if (what.equals("tombstone")) session.onTombDestroyed(column, unit.row);
        }
    }

    private void meltTombsInPath(int row, int fromCol, int toCol) {
        for (int x = fromCol + 1; x <= Math.min(GameSession.COLS, toCol); x++) {
            if (session.terrain[row][x] == TerrainType.TOMB) {
                session.tombHp[row][x] -= 100;
                if (session.tombHp[row][x] <= 0) {
                    session.terrain[row][x] = TerrainType.GRASS;
                    System.out.println("The tombstone at (" + x + ", " + row + ") crumbles!");
                    session.onTombDestroyed(x, row);}
            }
        }
    }

    private void meltRow(int row) {
        for (int x = 1; x <= GameSession.COLS; x++) {
            if (session.terrain[row][x] == TerrainType.FROZEN) {
                session.terrain[row][x] = TerrainType.GRASS;
                session.tombHp[row][x] = 0;}
        }
        for (PlantedUnit other : session.plants) {
            if (other.row == row && other.frozen) {
                other.frozen = false;
                other.snowballHits = 0;
                System.out.println(other.getType() + " at (" + other.col + ", " + row + ") is thawed by the heat!");
            }
        }
    }

    /// targets
    private ActiveZombie firstTargetableZombie(int row, int fromCol, float range) {
        ActiveZombie best = null;
        for (ActiveZombie activeZombie : session.zombies) {
            if (activeZombie.row != row || activeZombie.col < fromCol || activeZombie.col > fromCol + range) continue;
            if (activeZombie.zombie.getType() == ZombieType.SNORKEL && session.terrain[row][activeZombie.cellCol()] == TerrainType.WATER) {
                continue;
            }
            if (best == null || activeZombie.col < best.col) best = activeZombie;
        }
        return best;
    }

    private ActiveZombie frontZombieInRow(int row, int col, float maxDistance) {
        ActiveZombie best = null;
        for (ActiveZombie activeZombie : session.zombies) {
            if (activeZombie.row == row && Math.abs(activeZombie.col - col) <= maxDistance && (best == null || activeZombie.col < best.col)) {
                best = activeZombie;
            }
        }
        return best;
    }

    private ActiveZombie zombieOnCell(int column, int row) {
        for (ActiveZombie activeZombie : session.zombies) {
            if (activeZombie.row == row && activeZombie.cellCol() == column) return activeZombie;
        }
        return null;
    }

    private boolean torchwoodBetween(int row, int fromCol, int toCol) {
        for (PlantedUnit unit : session.plants) {
            if (unit.row == row && unit.getType() == PlantType.TORCHWOOD && unit.col > fromCol && unit.col <= toCol) {
                return true;}
        }
        return false;
    }

    private PlantedUnit closestPlantInRow(int row, int col) {
        PlantedUnit best = null;
        int bestDist = Integer.MAX_VALUE;
        for (PlantedUnit unit : session.plants) {
            if (unit.row == row && Math.abs(unit.col - col) < bestDist) {
                bestDist = Math.abs(unit.col - col);
                best = unit;
            }
        }
        return best;
    }

    private void damageCellArea(int centerX, int centerY, int radius, int damage, String source) {
        for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
            if (Math.abs(activeZombie.cellCol() - centerX) <= radius && Math.abs(activeZombie.row - centerY) <= radius) {
                session.damageZombie(activeZombie, damage, DamageKind.FIRE, source);}
        }
    }

    /// plant_food

    void applyPlantFood(PlantedUnit unit) {
        PlantType type = unit.getType();
        String source = src(unit);
        if (unit.plant.hasTag(PlantTag.SUN_PRODUCER)) {
            session.groundSuns.add(new int[]{unit.col, unit.row, 150});
            System.out.println("Plant food: " + type + " bursts out 150 sun at (" + unit.col + ", " + unit.row + ")!");
            return;
        }
        if (unit.plant.hasTag(PlantTag.WALL)) {
            unit.plant.heal(unit.plant.getMaxHp());
            System.out.println("Plant food: " + type + " is fully repaired with a hard coating!");
            return;
        }
        if (type == PlantType.MAGNET_SHROOM) {
            for (ActiveZombie activeZombie : session.zombies) {
                if (activeZombie.zombie.hasMetalArmor() && activeZombie.zombie.getArmorHp() > 0) activeZombie.zombie.stripArmor();}
            System.out.println("Plant food: the Magnet-shroom strips every metal armor on the lawn!");
            return;
        }
        if (type == PlantType.GARLIC) {
            System.out.println("Plant food: the Garlic's stench moves the whole lane!");
            for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                if (activeZombie.row == unit.row) {
                    activeZombie.row = activeZombie.row == GameSession.ROWS ? activeZombie.row - 1 : activeZombie.row + 1;}}
            return;
        }
        if (unit.plant.hasTag(PlantTag.EXPLOSIVE)) {
            unit.ageTicks = 1000; // trigger the fuse immediately
            unit.armed = true;
            System.out.println("Plant food: " + type + " detonates immediately!");
            return;
        }
        if (unit.plant.hasTag(PlantTag.STRIKE_THROUGH)) {
            System.out.println("Plant food: " + type + " blankets the lane in fumes!");
            for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                if (activeZombie.row == unit.row) session.damageZombie(activeZombie, 600, DamageKind.FUME, source);
            }
            return;
        }
        if (unit.plant.hasTag(PlantTag.LOBBER)) {
            System.out.println("Plant food: " + type + " rains 5 heavy shots on the lane!");
            for (int i = 0; i < 5; i++) {
                ActiveZombie target = frontZombieInRow(unit.row, unit.col, 9);
                if (target == null) break;
                session.damageZombie(target, 300, DamageKind.LOB, source);
            }
            return;
        }
        if (unit.plant.hasTag(PlantTag.PEA) || unit.plant.hasTag(PlantTag.SHOOTER)) {
            System.out.println("Plant food: " + type + " sprays the whole lane!");
            DamageKind kind = unit.plant.hasTag(PlantTag.ICE) ? DamageKind.ICE : DamageKind.NORMAL;
            for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
                if (activeZombie.row == unit.row) {
                    session.damageZombie(activeZombie, 600, kind, source);
                    if (kind == DamageKind.ICE) activeZombie.freeze(5 * GameSession.TICKS_PER_SECOND);
                }
            }
            return;
        }
        /// 300 damage across lane
        System.out.println("Plant food: " + type + " releases a wave of energy!");
        for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
            if (activeZombie.row == unit.row) session.damageZombie(activeZombie, 300, DamageKind.NORMAL, source);}
    }
    @SuppressWarnings("unused")
    private PlantFamily familyOf(PlantedUnit unit) {
        return pvz.model.entity.plant.PlantCatalog.of(unit.getType()).family;
    }
}
