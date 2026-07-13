package org.destroyermob.mobscombat.mixin;

import net.bettercombat.api.AttackHand;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets Better Combat use a one-handed weapon held by itself in the off hand.
 * Better Combat normally requires a weapon in the main hand before it selects
 * an attack, so this delegates selection through its own temporary hand swap
 * and preserves its animation, timing, and server-side damage handling.
 */
@Pseudo
@Mixin(targets = "net.bettercombat.logic.PlayerAttackHelper", remap = false)
public abstract class BetterCombatOffhandMixin {
    private static final ThreadLocal<Boolean> MOBSCOMBAT_RESOLVING_OFFHAND_ATTACK = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "getCurrentAttack(Lnet/minecraft/world/entity/player/Player;I)Lnet/bettercombat/api/AttackHand;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void mobscombat$selectOffhandOnlyAttack(
            Player player,
            int comboCount,
            CallbackInfoReturnable<AttackHand> callback
    ) {
        if (player == null || MOBSCOMBAT_RESOLVING_OFFHAND_ATTACK.get()) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (WeaponRegistry.getAttributes(mainHand) != null) {
            return;
        }

        WeaponAttributes offhandAttributes = WeaponRegistry.getAttributes(offHand);
        if (offhandAttributes == null || offhandAttributes.isTwoHanded()) {
            return;
        }

        AttackHand[] delegatedAttack = new AttackHand[1];
        MOBSCOMBAT_RESOLVING_OFFHAND_ATTACK.set(true);
        try {
            PlayerAttackHelper.swapHandAttributes(player, () ->
                    delegatedAttack[0] = PlayerAttackHelper.getCurrentAttack(player, comboCount)
            );
        } finally {
            MOBSCOMBAT_RESOLVING_OFFHAND_ATTACK.remove();
        }

        AttackHand selectedAttack = delegatedAttack[0];
        if (selectedAttack == null) {
            return;
        }

        callback.setReturnValue(new AttackHand(
                selectedAttack.attack(),
                selectedAttack.combo(),
                true,
                offhandAttributes,
                offHand
        ));
    }
}
