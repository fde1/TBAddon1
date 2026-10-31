package com.odtheking.odinaddon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odinaddon.features.impl.render.Animations;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyExpressionValue(
            method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getModifiedSwingDuration(Lnet/minecraft/world/item/component/SwingAnimation;)I"
            )
    )
    private int modifySwingDuration(int original) {
        Player player = Minecraft.getInstance().player;
        if ((Object) this != player) return original;

        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getIgnoreHaste()) {
            return Animations.INSTANCE.getSpeed();
        }
        return original;
    }

    @Inject(
            method = "swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventClientSwingSpam(InteractionHand hand, SwingAnimation animation, boolean sync,
                                        CallbackInfoReturnable<Boolean> cir) {
        Player player = Minecraft.getInstance().player;
        if ((Object) this != player) return;
        if (!Animations.INSTANCE.getEnabled() || !Animations.INSTANCE.getStopSwingSpam()) return;

        if (((LivingEntity) (Object) this).isSwinging()) {
            cir.setReturnValue(false);
        }
    }
}
