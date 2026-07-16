package org.destroyermob.mobscombat.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class ParrySystem {
    private ParrySystem() {
    }

    public static void armWeaponParry(ServerPlayer player, ItemStack weapon) {
        if (!CombatConfig.parryEnabled()
                || player.isSpectator()
                || !player.isAlive()
                || !WeaponProfileResolver.isParryWeapon(weapon)) {
            return;
        }
        CombatState state = CombatStateManager.getOrCreate(player);
        if (!state.isStaggered()) {
            state.armWeaponParry(CombatConfig.parryWindowTicks(), CombatConfig.parryCooldownTicks());
        }
    }

    public static boolean tryWeaponParry(LivingDamageEvent.Pre event) {
        if (!CombatConfig.parryEnabled()
                || event.getNewDamage() <= 0.0F
                || !(event.getEntity() instanceof ServerPlayer player)
                || !player.isUsingItem()
                || player.getUsedItemHand() != InteractionHand.MAIN_HAND) {
            return false;
        }

        ItemStack weapon = player.getUseItem();
        Entity source = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();
        if (!WeaponProfileResolver.isParryWeapon(weapon)
                || !(source instanceof LivingEntity attacker)
                || source != direct
                || attacker == player
                || !isFacing(player, attacker)) {
            return false;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        if (state.isStaggered() || !state.consumeWeaponParry()) {
            return false;
        }

        ResolvedWeaponProfile resolvedWeapon = WeaponProfileResolver.resolve(weapon);
        event.setNewDamage(event.getNewDamage() * CombatConfig.parryDamageMultiplier());
        state.markParry();
        PostureSystem.applyFlatPostureDamage(
                player,
                attacker,
                CombatConfig.parryPostureDamage(),
                resolvedWeapon.profile().damageKind(),
                false
        );
        player.stopUsingItem();
        playWeaponParryFeedback(player, attacker);
        ModNetworking.sendCombatFeedback(player, CombatFeedbackType.PARRY);
        return true;
    }

    public static boolean openCounterFromBlock(Player player, ItemStack blockingItem, boolean sendCounterFeedback) {
        if (!CombatConfig.parryEnabled()
                || player.isSpectator()
                || !player.isAlive()
                || blockingItem.isEmpty()
                || !WeaponProfileResolver.isShieldLike(blockingItem)) {
            return false;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        if (state.isStaggered()) {
            return false;
        }

        state.markParry();
        if (sendCounterFeedback && player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendCombatFeedback(serverPlayer, CombatFeedbackType.PARRY);
        }
        return true;
    }

    private static boolean isFacing(Player player, LivingEntity attacker) {
        Vec3 towardAttacker = attacker.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (towardAttacker.lengthSqr() < 1.0E-6D) {
            return true;
        }
        Vec3 facing = player.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        return facing.lengthSqr() > 1.0E-6D && facing.normalize().dot(towardAttacker.normalize()) > 0.0D;
    }

    private static void playWeaponParryFeedback(ServerPlayer player, LivingEntity attacker) {
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.75F, 1.9F);
            level.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    attacker.getX(),
                    attacker.getY() + attacker.getBbHeight() * 0.55D,
                    attacker.getZ(),
                    10,
                    Math.max(attacker.getBbWidth() * 0.35D, 0.15D),
                    Math.max(attacker.getBbHeight() * 0.25D, 0.15D),
                    Math.max(attacker.getBbWidth() * 0.35D, 0.15D),
                    0.08D
            );
        }
    }
}
