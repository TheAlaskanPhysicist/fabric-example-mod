package com.stanieldev.relativity.history;

import net.minecraft.world.entity.Entity;

import java.util.*;

public class HistoryManager {

    private static final Map<UUID, EntityHistory> histories = new HashMap<>();

    public static void record(Entity entity, long tick) {
        EntityHistory history = histories.computeIfAbsent(
                entity.getUUID(),
                id -> new EntityHistory()
        );

        history.add(new EntitySnapshot(
                tick,
                entity.position()
        ));
    }

    public static EntityHistory getHistory(UUID uuid) {
        return histories.get(uuid);
    }

    public static int getEntityCount() {
        return histories.size();
    }

    public static int getSnapshotCount(UUID uuid) {
        EntityHistory history = histories.get(uuid);

        if (history == null) {
            return 0;
        }

        return history.getSnapshots().size();
    }

    public static Map<UUID, EntityHistory> getAllHistories() {
        return histories;
    }

    public static Collection<EntityHistory> getAllHistoriesCopy() {
        return new ArrayList<>(histories.values());
    }
}
