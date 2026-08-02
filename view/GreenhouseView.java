package pvz.view;

import pvz.model.auth.User;
import pvz.model.greenhouse.Greenhouse;
import pvz.model.greenhouse.Pot;

class GreenhouseView {

    void render(User user) {
        Greenhouse greenhouse = user.getGreenhouse();
        StringBuilder stringBuilder = new StringBuilder("Greenhouse (" + greenhouse.getUnlockedRows() + "/" + Greenhouse.ROWS + " rows unlocked) | Pots in stock: " + user.getPots() + "\n");
        for (int y = 1; y <= Greenhouse.ROWS; y++) {
            stringBuilder.append("  row ").append(y).append(": ");
            if (!greenhouse.isRowUnlocked(y)) {
                stringBuilder.append("[locked - ").append(greenhouse.nextRowUnlockCost()).append(" coins]\n");
                continue;
            }
            for (int x = 1; x <= Greenhouse.COLUMNS; x++) appendPot(stringBuilder,greenhouse.getPot(x,y));
            stringBuilder.append('\n');
        }
        stringBuilder.append("Stored boosts: ").append(user.getStoredBoostMap());
        System.out.println(stringBuilder);
    }

    private void appendPot(StringBuilder stringBuilder, Pot pot) {
        if (pot == null) stringBuilder.append("[empty] ");
        else if (pot.isReady()) stringBuilder.append('[').append(pot.getPlantType()).append(": READY] ");
        else stringBuilder.append('[').append(pot.getPlantType()).append(": ").append(pot.remainingMinutes()).append("m left] ");
    }
}
