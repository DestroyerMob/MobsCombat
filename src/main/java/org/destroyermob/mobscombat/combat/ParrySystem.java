package org.destroyermob.mobscombat.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class ParrySystem {
    private ParrySystem() {
    }

    public static boolean tryArmParry(ServerPlayer player) {
        if (!CombatConfig.parryEnabled() || player.isSpectator() || !player.isAlive()) {
            return false;
        }

        ItemStack weapon = player.getMainHandItem();
        ResolvedWeaponProfile resolvedWeapon = WeaponProfileResolver.resolve(weapon);
        if (!resolvedWeapon.recognizedWeapon() || WeaponProfileResolver.isShieldLike(weapon)) {
            return false;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        int windowTicks = CombatConfig.parryWindowTicks();
        if (weapon.is(CombatTags.Items.DAGGERS)) {
            windowTicks += CombatConfig.daggerParryWindowBonusTicks();
        }
        if (state.isStaggered() || !state.armParry(windowTicks, CombatConfig.parryCooldownTicks())) {
            return false;
        }

        ModNetworking.sendCombatFeedback(player, CombatFeedbackType.PARRY_READY);
        return true;
    }

    public static boolean tryParry(LivingDamageEvent.Pre event) {
        if (!CombatConfig.parryEnabled() || event.getNewDamage() <= 0.0F || !(event.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        Entity source = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();
        if (!(source instanceof LivingEntity attacker) || source != direct || attacker == player || attacker instanceof ServerPlayer) {
            return false;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        if (!state.consumeParryReadiness()) {
            return false;
        }

        ItemStack weapon = player.getMainHandItem();
        ResolvedWeaponProfile resolvedWeapon = WeaponProfileResolver.resolve(weapon);
        if (!resolvedWeapon.recognizedWeapon() || WeaponProfileResolver.isShieldLike(weapon)) {
            return false;
        }

        event.setNewDamage(event.getNewDamage() * CombatConfig.parryDamageMultiplier());
        state.markParry();
        PostureSystem.applyFlatPostureDamage(player, attacker, CombatConfig.parryPostureDamage(), resolvedWeapon.profile().damageKind(), false);
        playParryFeedback(player, attacker);
        ModNetworking.sendCombatFeedback(player, CombatFeedbackType.PARRY);
        return true;
    }

    private static void playParryFeedback(ServerPlayer player, LivingEntity attacker) {
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
