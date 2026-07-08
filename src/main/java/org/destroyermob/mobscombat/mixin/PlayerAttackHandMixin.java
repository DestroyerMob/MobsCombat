package org.destroyermob.mobscombat.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobscombat.combat.DualWieldSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerAttackHandMixin {
    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"
            )
    )
    private double mobscombat$getActiveAttackDamage(Player player, Holder<Attribute> attribute) {
        return DualWieldSystem.getAttackDamageAttributeOverride(player, attribute, player.getAttributeValue(attribute));
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F"
            )
    )
    private float mobscombat$getActiveAttackStrength(Player player, float partialTicks) {
        return DualWieldSystem.getAttackStrengthScaleOverride(player, player.getAttackStrengthScale(partialTicks));
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
            ),
            index = 0
    )
    private InteractionHand mobscombat$useActiveAttackHand(InteractionHand hand) {
        return DualWieldSystem.getAttackHandOverride((Player) (Object) this, hand);
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack mobscombat$getActiveAttackItem(Player player) {
        return DualWieldSystem.getMainHandItemOverride(player);
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private void mobscombat$setActiveAttackItem(Player player, InteractionHand hand, ItemStack stack) {
        DualWieldSystem.setItemInAttackHand(player, hand, stack);
    }
}
