package org.destroyermob.mobscombat.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobscombat.combat.DualWieldSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWeaponItemMixin {
    @Inject(method = "getWeaponItem", at = @At("HEAD"), cancellable = true)
    private void mobscombat$getDualWieldWeaponItem(CallbackInfoReturnable<ItemStack> callback) {
        ItemStack override = DualWieldSystem.getWeaponItemOverride((LivingEntity) (Object) this);
        if (!override.isEmpty()) {
            callback.setReturnValue(override);
        }
    }
}
