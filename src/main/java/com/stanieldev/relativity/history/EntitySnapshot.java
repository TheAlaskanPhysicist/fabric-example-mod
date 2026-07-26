package com.stanieldev.relativity.history;

import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class EntitySnapshot {
    // TODO: Add more data like rotation, velocity, pose, animation, equipment, etc.

    private final long tick;
    private final Vec3 position;

    public EntitySnapshot(long tick, Vec3 position) {
        this.tick = tick;
        this.position = Objects.requireNonNull(position);
    }

    public long getTick() {
        return tick;
    }

    public Vec3 getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Tick " + tick + " Pos " + position;
    }
}
