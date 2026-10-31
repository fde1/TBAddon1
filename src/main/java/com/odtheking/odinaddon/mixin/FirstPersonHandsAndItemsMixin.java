package com.odtheking.odinaddon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.odtheking.odinaddon.features.impl.render.Animations;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {
    @Shadow private float mainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float oOffHandHeight;

    @Inject(method = "tick", at = @At("TAIL"))
    private void stopEquipAnimation(LocalPlayer player, CallbackInfo ci) {
        if (!Animations.INSTANCE.getEnabled() || !Animations.INSTANCE.getStopEquipAnimation()) return;
        this.oMainHandHeight = 1.0f;
        this.mainHandHeight = 1.0f;
        this.oOffHandHeight = 1.0f;
        this.offHandHeight = 1.0f;
    }

    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemSwapScale(F)F")
    )
    private float overrideAttackStrengthScale(float originalValue) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getStopEquipAnimation() || Animations.INSTANCE.getStopFullSwing()) return 1.0f;
        return originalValue;
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void forceInstantlyReplace(ItemStack currentlyVisibleItem, ItemStack expectedItem, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (Animations.INSTANCE.getEnabled() && Animations.INSTANCE.getStopEquipAnimation()) cir.setReturnValue(true);
    }
}
