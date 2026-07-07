package org.destroyermob.mobscombat.mixin.client;

import net.minecraft.client.Minecraft;
import org.destroyermob.mobscombat.client.MobsCombatClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftStartAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void mobscombat$startDualWieldAttack(CallbackInfoReturnable<Boolean> callback) {
        if (MobsCombatClient.tryStartDualWieldAttack()) {
            callback.setReturnValue(false);
            callback.cancel();
        }
    }
}
