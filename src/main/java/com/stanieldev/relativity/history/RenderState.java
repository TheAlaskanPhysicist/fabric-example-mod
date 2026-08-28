package com.stanieldev.relativity.history;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record RenderState(
        Vec3 position,
        float yRot,
        float xRot,
        float yHeadRot,
        float yBodyRot,
        float limbPos,
        float limbSpeed
) {
    public static RenderState fromSnapshot(EntitySnapshot s) {
        return new RenderState(
                s.position(),
                s.yRot(),
                s.xRot(),
                s.yHeadRot(),
                s.yBodyRot(),
                s.limbPos(),
                s.limbSpeed()
        );
    }

    public static RenderState lerp(EntitySnapshot a, EntitySnapshot b, float alpha) {
        Vec3 pos = a.position().lerp(b.position(), alpha);
        float yRot = Mth.rotLerp(alpha, a.yRot(), b.yRot());
        float xRot = Mth.lerp(alpha, a.xRot(), b.xRot());
        float yHeadRot = Mth.rotLerp(alpha, a.yHeadRot(), b.yHeadRot());
        float yBodyRot = Mth.rotLerp(alpha, a.yBodyRot(), b.yBodyRot());
        float limbPos = Mth.lerp(alpha, a.limbPos(), b.limbPos());
        float limbSpeed = Mth.lerp(alpha, a.limbSpeed(), b.limbSpeed());

        return new RenderState(pos, yRot, xRot, yHeadRot, yBodyRot, limbPos, limbSpeed);
    }
}