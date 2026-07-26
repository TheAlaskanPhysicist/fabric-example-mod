package com.stanieldev.relativity.client.render;

import net.minecraft.world.entity.Entity;

public class EntityRenderState {
    private final float yRot;
    private final float xRot;
    private final float yHeadRot;
    private final float yBodyRot;

    public EntityRenderState(Entity entity) {
        this.yRot = entity.getYRot();
        this.xRot = entity.getXRot();
        this.yHeadRot = entity.getYHeadRot();
        this.yBodyRot = entity.getYRot();
    }

    public void restore(Entity entity) {
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.setYHeadRot(yHeadRot);
        entity.setYBodyRot(yBodyRot);
    }
}