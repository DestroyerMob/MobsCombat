package org.destroyermob.mobscombat.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class GuardSystem {
    private static final float PERFECT_BLOCK_POSTURE_DAMAGE = 6.0F;

    private GuardSystem() {
    }

    public static void handleShieldBlock(LivingShieldBlockEvent event) {
        if (!CombatConfig.shieldGuardEnabled() || !(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (!event.getOriginalBlock() || !event.getBlocked()) {
            return;
        }

        ItemStack shield = player.getUseItem();
        if (!WeaponProfileResolver.isShieldLike(shield)) {
            return;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        state.syncGuardMax(guardMax(shield));
        boolean perfectBlock = CombatConfig.timedBlocksEnabled() && state.isWithinPerfectBlockWindow(player.tickCount) && !state.isGuardBroken();
        float guardCost = guardCost(event);

        if (perfectBlock) {
            state.spendGuard(guardCost * 0.15F);
            state.markPerfectBlock();
            event.setShieldDamage(0.0F);
            applyPerfectBlockFeedback(player, event.getDamageSource().getEntity());
            sendFeedback(player, CombatFeedbackType.PERFECT_BLOCK);
            return;
        }

        state.spendGuard(guardCost);
        if (state.currentGuard() <= 0.0F) {
            state.setGuardBroken();
            event.setBlockedDamage(0.0F);
            event.setShieldDamage(0.0F);
            player.getCooldowns().addCooldown(shield.getItem(), CombatConfig.guardBreakTicks());
            playGuardBreak(player);
            sendFeedback(player, CombatFeedbackType.GUARD_BREAK);
        } else {
            event.setShieldDamage(Math.max(1.0F, guardCost * 0.08F));
        }
    }

    private static float guardMax(ItemStack shield) {
        float base = CombatConfig.defaultGuardMax();
        if (shield.is(CombatTags.Items.BUCKLERS)) {
            return base * 0.75F;
        }
        if (shield.is(CombatTags.Items.TOWER_SHIELDS)) {
            return base * 1.5F;
        }
        return base;
    }

    private static float guardCost(LivingShieldBlockEvent event) {
        float incoming = Math.max(event.getOriginalBlockedDamage(), 0.0F);
        float multiplier = 1.0F;
        if (event.getDamageSource().getEntity() instanceof LivingEntity attacker) {
            multiplier *= CombatProfileResolver.resolve(attacker).profile().guardDamageMultiplier();
            ItemStack weapon = event.getDamageSource().getWeaponItem();
            if (weapon != null && !weapon.isEmpty()) {
                multiplier *= Math.max(0.2F, WeaponProfileResolver.resolve(weapon).profile().guardDamage() / 8.0F);
            }
        }
        return Math.max(1.0F, incoming * 8.0F * multiplier);
    }

    private static void applyPerfectBlockFeedback(Player player, Entity attacker) {
        if (attacker instanceof LivingEntity livingAttacker) {
            PostureSystem.applyFlatPostureDamage(player, livingAttacker, PERFECT_BLOCK_POSTURE_DAMAGE, CombatDamageKind.BLUNT, false);
        }
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    player.getX(),
                    player.getY() + player.getBbHeight() * 0.6D,
                    player.getZ(),
                    8,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.05D
            );
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 1.6F);
        }
    }

    private static void playGuardBreak(Player player) {
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8F, 0.9F);
            level.sendParticles(
                    ParticleTypes.CRIT,
                    player.getX(),
                    player.getY() + player.getBbHeight() * 0.55D,
                    player.getZ(),
                    12,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.08D
            );
        }
    }

    private static void sendFeedback(Player player, CombatFeedbackType type) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendCombatFeedback(serverPlayer, type);
        }
    }
}
