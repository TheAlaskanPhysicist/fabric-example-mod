package com.stanieldev.relativity.history;

import net.minecraft.world.entity.Entity;

import java.util.*;

import static com.stanieldev.relativity.Relativity.LOGGER;
import static com.stanieldev.relativity.config.RelativityConfig.TEMP_MAX_TIME_RETARDATION;

public class EntityHistoryManager {

    // Entity history storage
    private static final Map<UUID, EntityHistory> histories = new HashMap<>();
    public static void record(Entity entity, long tick) {
        EntityHistory history = histories.computeIfAbsent(entity.getUUID(), id -> new EntityHistory());
        history.add(new EntitySnapshot(
                tick,
                entity.position(),
                entity.getDeltaMovement()  // Velocity in blocks/tick
        ));
    }
    public static void prune(long currentTick) {
//        histories.entrySet().removeIf(entry -> {
//            EntityHistory history = entry.getValue();
//            // Todo, max time should be dependent on retarded time in ticks, not constant
//            return currentTick - history.getLastRecordedTick() > TEMP_MAX_TIME_RETARDATION;
//        });

        histories.entrySet().removeIf(entry -> {
            EntityHistory history = entry.getValue();
            long age = currentTick - history.getLastRecordedTick();
            LOGGER.info("History age: {}", age);
            return age > TEMP_MAX_TIME_RETARDATION;
        });
    }
    public static int getEntityCount() { return histories.size(); }
    public static boolean isEmpty() { return histories.isEmpty(); }
    public static EntityHistory getEntityHistory(UUID uuid) {
        return histories.get(uuid);
    }
    public static Collection<EntityHistory> getEntityHistories() { return new ArrayList<>(histories.values()); }
}
