package pvz.model.session;

import pvz.model.enums.PlantType;
import pvz.model.enums.TerrainType;
import pvz.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.List;

class ZombieEngine {

    private final GameSession session;

    ZombieEngine(GameSession session) {
        this.session = session;
    }

    void tick() {
        for (ActiveZombie activeZombie : new ArrayList<>(session.zombies)) {
            if (!session.zombies.contains(activeZombie) || session.gameover) continue;
            tickStatusTimers(activeZombie);
            if (activeZombie.isFrozen()) continue;

            tickSpecial(activeZombie);

            if (!session.zombies.contains(activeZombie) || session.gameover) continue;
            moveOrEat(activeZombie);

            if (session.gameover) return;
        }
    }

    private void tickStatusTimers(ActiveZombie activeZombie) {
        if (activeZombie.freezeTicks > 0) activeZombie.freezeTicks--;
        if (activeZombie.slowTicks > 0) {
            activeZombie.slowTicks--;

            if (activeZombie.slowTicks == 0) activeZombie.slowMult = 1.0;
        }
        if (activeZombie.laneSwitchCooldown > 0) activeZombie.laneSwitchCooldown--;

        if (activeZombie.abilityTimer > 0) activeZombie.abilityTimer--;
    }

    /// Move or eat method

    private void moveOrEat(ActiveZombie activeZombie) {
        ZombieType type = activeZombie.zombie.getType();
        if (type == ZombieType.KING) {
            return;
            /// the king never leave his throne in the spawn edge
        }
        PlantedUnit blocking = plantAhead(activeZombie);
        if (blocking != null && !activeZombie.movingRight) {
            handleContact(activeZombie, blocking);
            return;
        }
        double speed = activeZombie.zombie.getSpeed() * activeZombie.slowMult;
        if (type == ZombieType.ALL_STAR) {
            speed = activeZombie.allStarBitten ? 0.25 : 2.0;
            speed *= activeZombie.slowMult;
        }
        if (type == ZombieType.JESTER) speed *= 1.5;

        double delta = speed / GameSession.TICKS_PER_SECOND;
        activeZombie.col += activeZombie.movingRight ? delta : -delta;
        if (activeZombie.movingRight && activeZombie.col >= GameSession.COLS) {
            activeZombie.col = GameSession.COLS;
        }
        handleIceSlip(activeZombie);
        checkDeadline(activeZombie);
        if (activeZombie.col < 1.0) reachHouse(activeZombie);
    }

    private PlantedUnit plantAhead(ActiveZombie activeZombie) {
        int cell = activeZombie.cellCol();
        for (PlantedUnit unit : session.plants) {
            if (unit.row == activeZombie.row && unit.col == cell && activeZombie.col - unit.col <= 0.4 && unit.getType() != PlantType.LILY_PAD) {
                /// Dodo Riders fly over everything except the Tall-nut
                if (activeZombie.zombie.getType() == ZombieType.DODO_RIDER && unit.getType() != PlantType.TALL_NUT) {
                    return null;}
                return unit;
            }
        }
        return null;
    }

    private void handleContact(ActiveZombie activeZombie, PlantedUnit plant) {
        ZombieType type = activeZombie.zombie.getType();
        switch (type) {
            case EXPLORER:
                if (activeZombie.torchLit) {
                    session.destroyPlant(plant);
                    return;}
                break;
            case SQUASH_ZOMBIE:
                if (!activeZombie.crushedPlant) {
                    activeZombie.crushedPlant = true;
                    session.destroyPlant(plant);
                    return;
                }
                break;
            case JALAPENO_ZOMBIE:
                for (PlantedUnit unit : new ArrayList<>(session.plants)) {
                    if (unit.row == activeZombie.row) session.destroyPlant(unit);
                }
                session.killZombie(activeZombie,"jalapeno-zombie");
                return;
            case TROGLOBITE:
            case PIANIST:
                session.destroyPlant(plant);
                return;
            case PEASHOOTER_ZOMBIE:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 3 * GameSession.TICKS_PER_SECOND;
                    session.damagePlant(plant, 20);
                }
                return;
            default:
                break;
        }
        /// Garlic divert biters to near lane
        if (plant.getType() == PlantType.GARLIC && activeZombie.laneSwitchCooldown <= 0) {
            activeZombie.laneSwitchCooldown = GameSession.TICKS_PER_SECOND;
            session.damagePlant(plant, activeZombie.zombie.getDamage() / GameSession.TICKS_PER_SECOND + 1);
            activeZombie.row = activeZombie.row == GameSession.ROWS ? activeZombie.row - 1 : activeZombie.row + 1;
            return;
        }
        if (activeZombie.zombie.getType() == ZombieType.ALL_STAR && !activeZombie.allStarBitten) activeZombie.allStarBitten = true;
        int bite = Math.max(1, activeZombie.zombie.getDamage() / GameSession.TICKS_PER_SECOND);
        session.damagePlant(plant, bite);
    }

    private void handleIceSlip(ActiveZombie activeZombie) {
        int cell = activeZombie.cellCol();
        if (cell != activeZombie.lastCellSeen) {
            activeZombie.lastCellSeen = cell;
            TerrainType type = session.terrain[activeZombie.row][cell];
            if (type == TerrainType.ICE_SLIP_UP && activeZombie.row > 1) {
                activeZombie.row--;
            }
            else if (type == TerrainType.ICE_SLIP_DOWN && activeZombie.row < GameSession.ROWS) {
                activeZombie.row++;
            }
        }
    }
    private void checkDeadline(ActiveZombie activeZombie) {
        if (session.spec.getDeadlineColumn() > 0 && activeZombie.col < session.spec.getDeadlineColumn()) {
            session.lose("A zombie crossed the dead line at column " + session.spec.getDeadlineColumn() + "! The zombie ate your brain; LOSER!!!");
        }
    }

    private void reachHouse(ActiveZombie activeZombie) {
        int row = activeZombie.row;
        if (session.mowers[row]) {
            session.mowers[row] = false;
            for (ActiveZombie victim : new ArrayList<>(session.zombies)) {
                if (victim.row == row) {
                    session.killZombie(victim, "mower-" + row);
                }
            }
        }
        else session.lose("The zombie ate your brain; LOSER!!!");
    }

    /// special mods

    private void tickSpecial(ActiveZombie activeZombie) {
        switch (activeZombie.zombie.getType()) {
            case GARGANTUAR:
                if (!activeZombie.abilityUsed && activeZombie.zombie.getHp() <= activeZombie.zombie.getMaxHp() / 2) {
                    activeZombie.abilityUsed = true;
                    session.spawnZombieAt(ZombieType.IMP, 3, activeZombie.row, activeZombie.spawnedWave);
                }
                break;
            case RA:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 10 * GameSession.TICKS_PER_SECOND;
                    if (session.sunBank >= 25) {
                        session.sunBank -= 25;
                        activeZombie.sunStolen += 25;
                    }
                }
                break;
            case TURQUOISE_SKULL:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 10 * GameSession.TICKS_PER_SECOND;
                    if (session.sunBank >= 25) {
                        session.sunBank -= 25;
                        activeZombie.sunStolen += 25;
                        if (activeZombie.sunStolen >= 75) {
                            activeZombie.sunStolen = 0;
                            for (PlantedUnit unit : new ArrayList<>(session.plants)) {
                                if (unit.row == activeZombie.row) session.damagePlant(unit, 50);
                            }
                        }
                    }
                }
                break;
            case PROSPECTOR:
                if (!activeZombie.abilityUsed && activeZombie.col <= 5) {
                    activeZombie.abilityUsed = true;
                    activeZombie.movingRight = true;
                    activeZombie.col = 1.0;
                }
                break;
            case PIANIST:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 8 * GameSession.TICKS_PER_SECOND;
                    if (session.rng.nextInt(100) < 30) {
                        for (ActiveZombie other : session.zombies) {
                            if (other != activeZombie && Math.abs(other.col - activeZombie.col) <= 2) {
                                other.row = other.row == GameSession.ROWS ? other.row - 1 : other.row + 1;
                            }
                        }
                    }
                }
                break;
            case TOMBRAISER:
                if (!activeZombie.abilityUsed && session.tickSince(activeZombie.spawnTick) >= 5 * GameSession.TICKS_PER_SECOND) {
                    activeZombie.abilityUsed = true;
                    raiseTombs();
                }
                break;
            case HUNTER:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 6 * GameSession.TICKS_PER_SECOND;
                    throwSnowball(activeZombie);
                }
                break;
            case OCTOPUS:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 8 * GameSession.TICKS_PER_SECOND;
                    bindPlant(activeZombie);
                }
                break;
            case WIZARD:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 10 * GameSession.TICKS_PER_SECOND;
                    curseFurthestPlant(activeZombie);
                }
                break;
            case KING:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 12 * GameSession.TICKS_PER_SECOND;
                    knightABasic(activeZombie);
                }
                break;
            case FISHERMAN:
                if (activeZombie.abilityTimer <= 0 && activeZombie.cellCol() >= 6) {
                    activeZombie.abilityTimer = 12 * GameSession.TICKS_PER_SECOND;
                    hookFrontPlant(activeZombie);
                }
                break;
            case ARCADE:
                if (activeZombie.abilityTimer <= 0) {
                    activeZombie.abilityTimer = 10 * GameSession.TICKS_PER_SECOND;
                    session.spawnZombieAt(ZombieType.BASIC, activeZombie.cellCol(), activeZombie.row, activeZombie.spawnedWave);
                }
                break;
            default:
                break;
        }
    }

    private void raiseTombs() {
        int raised = 0;
        int guard = 0;
        while (raised < 2 && guard++ < 50) {
            int x = 5 + session.rng.nextInt(4);
            int y = 1 + session.rng.nextInt(GameSession.ROWS);
            if (session.terrain[y][x] == TerrainType.GRASS && session.findPlant(x, y) == null) {
                session.terrain[y][x] = TerrainType.TOMB;
                session.tombHp[y][x] = 700;
                raised++;
            }
        }
    }

    private void throwSnowball(ActiveZombie activeZombie) {
        PlantedUnit target = null;
        for (PlantedUnit unit : session.plants) {
            if (unit.row == activeZombie.row && unit.col <= activeZombie.col && (target == null || unit.col > target.col)) {
                target = unit;}
        }
        if (target == null || target.frozen) return;
        target.snowballHits++;
        if (target.snowballHits >= 3) {
            target.frozen = true;
        }
    }

    private void bindPlant(ActiveZombie activeZombie) {
        for (PlantedUnit unit : session.plants) {
            if (unit.row == activeZombie.row && Math.abs(unit.col - activeZombie.col) <= 3 && !session.octopusBindings.containsValue(unit)) {
                session.octopusBindings.put(activeZombie, unit);
                return;
            }
        }
    }

    private void curseFurthestPlant(ActiveZombie activeZombie) {
        PlantedUnit target = null;
        for (PlantedUnit unit : session.plants) {
            if (unit.row == activeZombie.row && !unit.turnedIntoCat && (target == null || unit.col < target.col)) target = unit;
        }
        if (target == null) return;

        target.turnedIntoCat = true;
        session.wizardCurses.computeIfAbsent(activeZombie, k -> new ArrayList<>()).add(target);
    }

    private void knightABasic(ActiveZombie az) {
        for (ActiveZombie other : new ArrayList<>(session.zombies)) {
            if (other.zombie.getType() == ZombieType.BASIC) {
                ActiveZombie knight = session.spawnZombieAt(ZombieType.KNIGHT, other.cellCol(), other.row, other.spawnedWave);
                knight.col = other.col;
                session.zombies.remove(other);
                return;
            }
        }
    }

    private void hookFrontPlant(ActiveZombie az) {
        PlantedUnit target = null;
        for (PlantedUnit unit : session.plants) {
            if (unit.row == az.row && (target == null || unit.col > target.col)) target = unit;
        }
        if (target != null) {
            session.destroyPlant(target);
        }
    }

    /// death
    void onDeath(ActiveZombie activeZombie) {
        /// Octopus
        session.octopusBindings.remove(activeZombie);
        /// Wizard
        List<PlantedUnit> cursed = session.wizardCurses.remove(activeZombie);
        if (cursed != null) {
            for (PlantedUnit unit : cursed) {
                if (session.plants.contains(unit)) {
                    unit.turnedIntoCat = false;
                }
            }
        }
    }
}
