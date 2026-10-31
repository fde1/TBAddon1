package com.odtheking.odinaddon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.odtheking.odinaddon.features.impl.render.Animations;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
    @Invoker("applyItemArmTransform")
    protected abstract void invokeApplyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight);

    @Invoker("applyItemArmAttackTransform")
    protected abstract void invokeApplyItemArmAttackTransform(PoseStack poseStack, HumanoidArm arm, float attackValue);

    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
            )
    )
    private void applyCustomHeldItemTransform(
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state,
            float partialTicks,
            float xRot,
            InteractionHand hand,
            float attack,
            ItemStack itemStack,
            float inverseArmHeight,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            CallbackInfo ci
    ) {
        if (hand == InteractionHand.MAIN_HAND) Animations.INSTANCE.hookItemTransform(poseStack);
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void stopSwing(float animation, PoseStack poseStack, int invert, HumanoidArm arm, CallbackInfo ci) {
        if (!Animations.INSTANCE.getEnabled() || !Animations.INSTANCE.getStopFullSwing()) return;
        ci.cancel();
        this.invokeApplyItemArmTransform(poseStack, arm, 0.0f);
        this.invokeApplyItemArmAttackTransform(poseStack, arm, animation);
    }
}
