package org.destroyermob.mobscombat.mixin.client;

import net.minecraft.client.Minecraft;
import org.destroyermob.mobscombat.client.compat.PunchyCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "punchy.client.animation.PunchyAnimationManager", remap = false)
public abstract class PunchyAnimationManagerMixin {
    @Inject(
            method = "isDualHandedActive(Lnet/minecraft/client/Minecraft;Lpunchy/client/animation/data/AnimationClip;F)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void mobscombat$forceDualWieldThrust(
            Minecraft minecraft,
            @Coerce Object clip,
            float time,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (PunchyCompat.shouldForceDualThrust(minecraft, clip)) {
            callback.setReturnValue(true);
        }
    }
}
