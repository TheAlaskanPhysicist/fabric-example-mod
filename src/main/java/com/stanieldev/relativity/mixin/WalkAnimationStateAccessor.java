package com.stanieldev.relativity.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {

    @Accessor("position")
    void relativity$setPosition(float value);

    @Accessor("speed")
    void relativity$setSpeed(float value);
}