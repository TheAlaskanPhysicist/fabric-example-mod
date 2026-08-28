package com.stanieldev.relativity.history;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.phys.Vec3;

import static com.stanieldev.relativity.config.RelativityConfig.MAX_SNAPSHOT_COUNT;
import static com.stanieldev.relativity.config.RelativityConfig.SPEED_OF_LIGHT;

public class EntityHistory {

    private long lastRecordedTick;
    public synchronized long getLastRecordedTick() { return lastRecordedTick; }

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

    public synchronized EntitySnapshot getTicksAgo(int ticks) {
        if (ticks < 0 || ticks >= snapshots.size()) {
            return null;
        }
        Iterator<EntitySnapshot> iterator = snapshots.descendingIterator();
        for (int i = 0; i < ticks; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * Finds the exact sub-tick timestamp (t_ret) where light emitted by the entity intersects
     * the observer's camera light cone: t_obs - t_ret = |x_obs - x_entity(t_ret)| / c
     */
    public synchronized double solveRetardedTime(double currentTick, Vec3 cameraPos) {
        if (snapshots.isEmpty()) return currentTick;

        // Iterate backwards through snapshots to solve light intersection
        EntitySnapshot newer = null;
        for (Iterator<EntitySnapshot> it = snapshots.descendingIterator(); it.hasNext(); ) {
            EntitySnapshot snap = it.next();
            if (newer != null) {
                double timeDifference = currentTick - snap.tick();
                double distance = cameraPos.distanceTo(snap.position());
                double lightDelay = distance / SPEED_OF_LIGHT;

                // Light cone intersection found between snap and newer
                if (timeDifference >= lightDelay) {
                    double t0 = snap.tick();
                    double t1 = newer.tick();
                    double d0 = cameraPos.distanceTo(snap.position()) / SPEED_OF_LIGHT;
                    double d1 = cameraPos.distanceTo(newer.position()) / SPEED_OF_LIGHT;

                    // Linear root finding for exact fractional tick
                    double denom = (t1 - t0) + (d1 - d0);
                    if (Math.abs(denom) < 1e-5) return t0;
                    double alpha = (currentTick - t0 - d0) / denom;
                    return t0 + Math.max(0.0, Math.min(1.0, alpha)) * (t1 - t0);
                }
            }
            newer = snap;
        }

        // Return oldest snapshot time if light target exceeds snapshot capacity
        return snapshots.peekFirst().tick();
    }


    public synchronized RenderState getInterpolatedState(double targetTick) {
        if (snapshots.isEmpty()) return null;

        EntitySnapshot oldest = snapshots.peekFirst();
        EntitySnapshot newest = snapshots.peekLast();

        if (targetTick <= oldest.tick()) return RenderState.fromSnapshot(oldest);
        if (targetTick >= newest.tick()) return RenderState.fromSnapshot(newest);

        EntitySnapshot prev = oldest;
        EntitySnapshot next = newest;

        for (EntitySnapshot snap : snapshots) {
            if (snap.tick() <= targetTick) {
                prev = snap;
            } else {
                next = snap;
                break;
            }
        }

        if (prev == next) return RenderState.fromSnapshot(prev);

        double delta = next.tick() - prev.tick();
        float alpha = delta == 0 ? 0.0f : (float) ((targetTick - prev.tick()) / delta);

        return RenderState.lerp(prev, next, alpha);
    }

    public synchronized float getPlaybackSpeedFactor(double currentTick, Vec3 cameraPos) {
        double tRetCurrent = solveRetardedTime(currentTick, cameraPos);
        double tRetPrev = solveRetardedTime(currentTick - 0.5, cameraPos);
        double dtRet = tRetCurrent - tRetPrev;
        return (float) Math.max(0.0, dtRet * 2.0); // Normalize to per-tick factor
    }
}
