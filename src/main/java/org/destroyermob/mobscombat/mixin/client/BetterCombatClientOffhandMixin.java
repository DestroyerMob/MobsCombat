package org.destroyermob.mobscombat.mixin.client;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Better Combat's client hook normally checks only the main hand before it
 * begins an upswing. Its injected helper methods are added to Minecraft at
 * runtime, so this runs after that mixin and substitutes an eligible lone
 * off-hand weapon at that single check.
 */
@Mixin(value = net.minecraft.client.Minecraft.class, priority = 900)
public abstract class BetterCombatClientOffhandMixin {
    @Dynamic("Targets helper methods injected into Minecraft by Better Combat")
    @Redirect(
            method = {"pre_doAttack", "pre_handleBlockBreaking"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            ),
            remap = false,
            require = 0
    )
    private ItemStack mobscombat$useOffhandWeaponForBetterCombat(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (WeaponRegistry.getAttributes(mainHand) != null) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        WeaponAttributes offhandAttributes = WeaponRegistry.getAttributes(offHand);
        if (offhandAttributes == null || offhandAttributes.isTwoHanded()) {
            return mainHand;
        }
        return offHand;
    }
}
