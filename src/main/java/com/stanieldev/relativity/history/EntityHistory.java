package com.stanieldev.relativity.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class EntityHistory {
    private static final int MAX_SIZE = 200;
    private final Deque<EntitySnapshot> snapshots = new ArrayDeque<>();

    public synchronized void add(EntitySnapshot snapshot) {
        snapshots.addLast(snapshot);
        while (snapshots.size() > MAX_SIZE) {
            snapshots.removeFirst();
        }
    }

    public synchronized List<EntitySnapshot> getSnapshots() {

        // Try to force concurrency crash if possible
        // Note: This makes you game "lag" heavily
        // try { Thread.sleep(5); }
        // catch (InterruptedException ignored) {}

        return List.copyOf(snapshots);
    }
}
