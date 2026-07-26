package com.stanieldev.relativity.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class EntityHistory {
    private final Deque<EntitySnapshot> snapshots = new ArrayDeque<>();

    private final int maxSize = 200;

    public void add(EntitySnapshot snapshot) {
        snapshots.addLast(snapshot);

        while (snapshots.size() > maxSize) {
            snapshots.removeFirst();
        }
    }

    public List<EntitySnapshot> getSnapshots() {
        return List.copyOf(snapshots);
    }
}
