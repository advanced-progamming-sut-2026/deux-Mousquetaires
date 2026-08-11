package pvz.model.shop;

import pvz.model.enums.QuestPriority;
import pvz.model.enums.RewardType;

public class Quest {

    private final String id;
    private final String title;
    private final String page;
    private final QuestPriority priority;
    private final RewardType rewardType;
    private final String rewardPayload;
    private final int target;
    private final String eventKey;
    private final boolean daily;

    private int progress;
    private boolean claimed;

    public Quest(String id, String title, String page, QuestPriority priority, RewardType rewardType, String rewardPayload, int target, String eventKey, boolean daily) {
        this.id = id;
        this.title = title;
        this.page = page;
        this.priority = priority;
        this.rewardType = rewardType;
        this.rewardPayload = rewardPayload;
        this.target = target;
        this.eventKey = eventKey;
        this.daily = daily;
    }

    public void advance(int amount) {
        if (!claimed) progress = Math.min(target, progress + amount);
    }

    public boolean isDone() {
        return progress >= target;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void markClaimed() {
        claimed = true;
    }
    /// reset
    public void reset() {
        progress = 0;
        claimed = false;
    }

    public String describe() {
        String state = claimed ? "CLAIMED" : isDone() ? "DONE - claim me!" : progress + "/" + target;
        return String.format("  [%s] (%s) %s  –  %s  [%s]", id, priority, title, rewardText(), state);
    }

    private String rewardText() {
        String[] parts = rewardPayload.split(":");
        switch (rewardType) {
            case CURRENCY:
                return "reward: " + parts[1] + " " + parts[0];
            case UNLOCKABLE:
                return "reward: unlock " + parts[1];
            case INVENTORY:
                return "reward: " + parts[2] + "x " + parts[1] + " seed packets";
            default:
                return "reward: ?";
        }
    }

    /// get
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getPage() { return page; }
    public QuestPriority getPriority() { return priority; }
    public RewardType getRewardType() { return rewardType; }
    public String getRewardPayload() { return rewardPayload; }
    public int getTarget() { return target; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = Math.min(target, progress); }
    public void setClaimed(boolean claim) { claimed = claim; }
    public String getEventKey() { return eventKey; }
    public boolean isDaily() { return daily; }
}
