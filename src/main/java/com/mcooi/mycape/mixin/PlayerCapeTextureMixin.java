package com.mcooi.mycape.mixin;

import com.mcooi.mycape.util.CapeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class PlayerCapeTextureMixin {
    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void onGetCapeTexture(CallbackInfoReturnable<Identifier> cir) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)(Object)this;
        if (player == MinecraftClient.getInstance().player) {
            Identifier customCape = CapeManager.getInstance().getCurrentCapeTexture();
            if (customCape != null) {
                cir.setReturnValue(customCape);
            }
        }
    }

    @Inject(method = "getElytraTexture", at = @At("HEAD"), cancellable = true)
    private void onGetElytraTexture(CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(null);
    }
} 