package com.stanieldev.relativity.client.mixin;

import com.stanieldev.relativity.client.render.RelativityModelContext;
import com.stanieldev.relativity.client.render.RelativityModelContext.ModelAnimState;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(QuadrupedModel.class)
public abstract class EntityModelMixin<T extends Entity> {

    @ModifyVariable(
            method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float modifyLimbSwing(float originalLimbSwing) {
        ModelAnimState state = RelativityModelContext.get();
        return state != null ? state.limbPos : originalLimbSwing;
    }

    @ModifyVariable(
            method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private float modifyLimbSwingAmount(float originalLimbSwingAmount) {
        ModelAnimState state = RelativityModelContext.get();
        return state != null ? state.limbSpeed : originalLimbSwingAmount;
    }
}