package org.destroyermob.mobscombat.mixin.client;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes Punchy's cached-hand renderer treat More Weapons knives like consumed throwables. */
@Pseudo
@Mixin(targets = "punchy.client.state.HandEquipStateMachine", remap = false)
public abstract class PunchyKnifeThrowMixin {
    private static final TagKey<net.minecraft.world.item.Item> KNIVES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("mobsmoreweapons", "knives")
    );

    @Shadow(remap = false)
    private ItemStack renderedOff;

    @Inject(
            method = "isThrowableSkipEquipStack(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void mobscombat$treatKnifeAsConsumedThrowable(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (isKnife(stack)) {
            callback.setReturnValue(true);
        }
    }

    @Inject(
            method = "shouldHideOffhandTridentThrowCache(Lnet/minecraft/world/item/ItemStack;Z)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void mobscombat$hideConsumedOffhandKnife(
            ItemStack currentOffhand,
            boolean offhandChanged,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (offhandChanged
                && (currentOffhand == null || currentOffhand.isEmpty())
                && isKnife(renderedOff)) {
            renderedOff = ItemStack.EMPTY;
            callback.setReturnValue(true);
        }
    }

    @Inject(
            method = "shouldSkipHandEquipOnSwap(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;ZZ)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void mobscombat$skipConsumedOffhandKnifeEquipCycle(
            ItemStack currentMainhand,
            ItemStack currentOffhand,
            boolean mainhandChanged,
            boolean offhandChanged,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!mainhandChanged
                && offhandChanged
                && (currentOffhand == null || currentOffhand.isEmpty())
                && isKnife(renderedOff)) {
            // The server has moved the knife into its projectile. Commit the empty visual
            // immediately and do not let Punchy turn that consumption into a hand-out cycle.
            renderedOff = ItemStack.EMPTY;
            callback.setReturnValue(true);
        }
    }

    private static boolean isKnife(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(KNIVES);
    }
}
