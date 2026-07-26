package com.stanieldev.relativity.history;

import net.minecraft.world.phys.Vec3;

public record EntitySnapshot(
        // TODO: Add more data like rotation, velocity, pose, animation, equipment, etc.
        long tick,
        Vec3 position,
        Vec3 velocity,
        float yaw,
        float pitch,
        float headYaw,
        float bodyYaw,
        float limbSwing,
        float limbSwingAmount,
        int age
) {
    // Displacement helpers

    // Velocity helpers
    public double speed() { return velocity.length(); }
    public boolean isMoving() { return !velocity.equals(Vec3.ZERO); }
}