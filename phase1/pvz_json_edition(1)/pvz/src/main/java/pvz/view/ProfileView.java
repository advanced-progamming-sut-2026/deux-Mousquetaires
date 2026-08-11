package pvz.view;

import pvz.model.auth.User;

class ProfileView {

    void render(User user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Username:").append(user.getUsername()).append('\n');
        stringBuilder.append("Nickname:").append(user.getNickname()).append('\n');
        stringBuilder.append("Email:").append(user.getEmail()).append('\n');
        stringBuilder.append("Gender:").append(user.getGender()).append('\n');
        stringBuilder.append("Games played:").append(user.getGamesPlayed()).append('\n');
        stringBuilder.append("Coins:").append(user.getCoins()).append('\n');
        stringBuilder.append("Gems:").append(user.getGems()).append('\n');
        stringBuilder.append("Levels cleared:").append(user.getProgress().getClearedLevels().size()).append("/16\n");
        stringBuilder.append("Best MooPoints:").append(user.getBestMooPoints()).append('\n');
        stringBuilder.append("Pots:").append(user.getPots()).append('/').append(User.MAX_POTS).append('\n');
        stringBuilder.append("Plant foods:").append(user.getPlantFoods()).append('/').append(User.MAX_PLANT_FOOD).append('\n');
        stringBuilder.append("Mini-games done:").append(user.getMiniGamesCompleted()).append('\n');
        stringBuilder.append("Quests done:").append(user.getDailyQuestsDone() + user.getOtherQuestsDone()).append(" (").append(user.getDailyQuestsDone()).append(" daily)\n");
        stringBuilder.append("Zombies killed:").append(user.getZombiesKilled());
        System.out.println(stringBuilder);
    }
}
