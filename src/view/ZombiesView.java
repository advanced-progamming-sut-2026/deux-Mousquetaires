package pvz.view;

import pvz.model.enums.ZombieType;
import pvz.model.session.ActiveZombie;
import pvz.model.session.GameSession;

import java.util.List;
class ZombiesView {

    void render(List<ActiveZombie> zombies) {
        if (zombies.isEmpty()) {
            System.out.println("No zombies on the lawn.");
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (ActiveZombie activeZombie : zombies) {
            stringBuilder.append(prettyName(activeZombie.getZombie().getType())).append(":\n");
            stringBuilder.append("  position: ").append(formatCol(activeZombie.getCol())).append(", ").append(activeZombie.getRow()).append('\n');
            stringBuilder.append("  health: ").append(activeZombie.getZombie().getHp()).append('\n');
            String armor = armorLines(activeZombie);
            if (!armor.isEmpty()) stringBuilder.append("  armor:\n").append(armor);
            String effects = effectLines(activeZombie);
            if (!effects.isEmpty()) stringBuilder.append("  effects:\n").append(effects);
        }
        System.out.print(stringBuilder);
    }

    private String armorLines(ActiveZombie activeZombie) {
        int armor = activeZombie.getZombie().getArmorHp();
        if (armor <= 0) return "";
        ZombieType type = activeZombie.getZombie().getType();
        if (type == ZombieType.KNIGHT) {
            int crown = Math.min(armor, 800);
            StringBuilder stringBuilder = new StringBuilder("    crown: " + crown + "\n");
            if (armor - crown > 0) stringBuilder.append("    shoulderArmor: ").append(armor - crown).append('\n');
            return stringBuilder.toString();
        }
        return "    " + armorName(type) + ": " + armor + "\n";
    }

    private String effectLines(ActiveZombie activeZombie) {
        StringBuilder stringBuilder = new StringBuilder();
        if (activeZombie.isFrozen()) stringBuilder.append("    frozen: ").append(seconds(activeZombie.getFreezeTicks())).append("s\n");
        else if (activeZombie.getSlowTicks() > 0) stringBuilder.append("    chilled: ").append(seconds(activeZombie.getSlowTicks())).append("s\n");
        if (activeZombie.isGlowing()) stringBuilder.append("    glowing (drops plant food)\n");
        return stringBuilder.toString();
    }

    private String armorName(ZombieType type) {
        switch (type) {
            case CONEHEAD: return "cone";
            case BUCKETHEAD: return "bucket";
            case BLOCKHEAD: return "block";
            case NEWSPAPER: return "newspaper";
            case PARASOL: return "parasol";
            default: return "armor";
        }
    }

    private String formatCol(double column) {
        if (column == Math.floor(column)) return String.valueOf((int) column);
        return String.format("%.1f", column);
    }

    private String seconds(int ticks) {
        double value = ticks / (double) GameSession.TICKS_PER_SECOND;
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    private String prettyName(ZombieType type) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : type.toString().split("_")) {
            stringBuilder.append(word.charAt(0)).append(word.substring(1).toLowerCase());}
        return stringBuilder.toString();
    }
}
