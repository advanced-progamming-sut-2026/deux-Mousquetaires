package pvz.model.greenhouse;

import pvz.model.enums.PlantType;

import java.time.Duration;
import java.time.LocalDateTime;
public class Pot {

    public static final int MARIGOLD_GROW_HOURS = 2;
    public static final int OTHER_GROW_HOURS = 8;

    private final PlantType plantType;
    private LocalDateTime plantedAt;

    public Pot(PlantType plantType, LocalDateTime plantedAt) {
        this.plantType = plantType;
        this.plantedAt = plantedAt;
    }

    public int growHours() {
        return plantType == PlantType.MARIGOLD ? MARIGOLD_GROW_HOURS : OTHER_GROW_HOURS;
    }

    public LocalDateTime readyAt() {
        return plantedAt.plusHours(growHours());
    }

    public boolean isReady() {
        return !LocalDateTime.now().isBefore(readyAt());
    }

    public long remainingMinutes() {
        long minutes = Duration.between(LocalDateTime.now(), readyAt()).toMinutes();
        return Math.max(0, minutes);
    }

    public int remainingHoursCeil() {
        return (int) Math.ceil(remainingMinutes() / 60.0);
    }

    public void finishNow() {
        plantedAt = LocalDateTime.now().minusHours(growHours());
    }

    public PlantType getPlantType() { return plantType; }
    public LocalDateTime getPlantedAt() { return plantedAt; }
}
