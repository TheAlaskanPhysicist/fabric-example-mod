package com.stanieldev.relativity.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.stanieldev.relativity.config.RelativityConfig.MAX_SNAPSHOT_COUNT;

public class EntityHistory {

    // Entity was last recorded by the client
    private long lastRecordedTick;
    public synchronized long getLastRecordedTick() { return lastRecordedTick; }

    // Entity snapshot storage
    private final Deque<EntitySnapshot> snapshots = new ArrayDeque<>();
    public synchronized void add(EntitySnapshot snapshot) {
        snapshots.addLast(snapshot);
        lastRecordedTick = snapshot.tick();
        while (snapshots.size() > MAX_SNAPSHOT_COUNT) {
            snapshots.removeFirst();
        }
    }
    public synchronized int size() { return snapshots.size(); }
    public synchronized boolean isEmpty() { return snapshots.isEmpty(); }
    public synchronized EntitySnapshot getLastSnapshot() { return snapshots.peekLast(); }
    public synchronized EntitySnapshot getOldestSnapshot() { return snapshots.peekFirst(); }
    public synchronized List<EntitySnapshot> getSnapshotHistory() { return List.copyOf(snapshots); }
}
