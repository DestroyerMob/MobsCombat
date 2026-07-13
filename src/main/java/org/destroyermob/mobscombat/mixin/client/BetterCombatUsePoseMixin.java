package org.destroyermob.mobscombat.mixin.client;

import net.bettercombat.Platform;
import net.bettercombat.client.animation.PoseSubStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps Better Combat's configured weapon pose while a More Weapons secondary is being prepared. */
@Mixin(value = PoseSubStack.class, remap = false)
public abstract class BetterCombatUsePoseMixin {
    @Inject(
            method = "setPose",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/kosmx/playerAnim/api/layered/modifier/AbstractFadeModifier;standardFadeIn(ILdev/kosmx/playerAnim/core/util/Ease;)Ldev/kosmx/playerAnim/api/layered/modifier/AbstractFadeModifier;",
                    ordinal = 0
            ),
            cancellable = true,
            require = 0
    )
    private void mobscombat$keepBetterCombatPoseDuringSecondaryUse(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null
                || !player.isUsingItem()
                || !isMoreWeaponsSecondary(player.getUseItem())
                || player.swinging
                || player.isSwimming()
                || player.onClimbable()
                || player.isFallFlying()
                || Platform.isCastingSpell(player)
                || CrossbowItem.isCharged(player.getMainHandItem())) {
            return;
        }

        ci.cancel();
    }

    private static boolean isMoreWeaponsSecondary(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"mobsmoreweapons".equals(id.getNamespace())) {
            return false;
        }
        String path = id.getPath();
        return path.endsWith("great_sword") || path.endsWith("battle_axe");
    }
}
