package com.stanieldev.relativity.history;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record EntitySnapshot(
        long tick,
        Vec3 position,
        float yRot,
        float xRot,
        float yHeadRot,
        float yBodyRot,
        float limbPos,
        float limbSpeed,
        CompoundTag nbt
) {}