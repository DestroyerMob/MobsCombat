package org.destroyermob.mobscombat.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class CombatEvents {
    private CombatEvents() {
    }

    public static void recordAttackIntent(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        if (isStaggered(player)) {
            event.setCanceled(true);
            return;
        }
        DualWieldSystem.prepareAttack(player, target);
        CombatStateManager.getOrCreate(player).recordAttackIntent(target.getId(), player.getAttackStrengthScale(0.0F), player.tickCount);
    }

    public static void adjustDualWieldIncomingDamage(LivingIncomingDamageEvent event) {
        DualWieldSystem.adjustIncomingDamage(event);
    }

    public static void reduceStaggeredOutgoingDamage(LivingDamageEvent.Pre event) {
        if (!CombatConfig.combatOverhaulEnabled()) {
            return;
        }
        if (ParrySystem.tryParry(event)) {
            return;
        }
        StealthSystem.tryApplyStealthStrike(event);
        Entity source = event.getSource().getEntity();
        if (!(source instanceof LivingEntity attacker) || attacker.level().isClientSide()) {
            return;
        }
        CombatState state = CombatStateManager.get(attacker);
        if (state == null || !state.isStaggered()) {
            return;
        }
        EntityCombatProfile profile = CombatProfileResolver.resolve(attacker).profile();
        if (state.isHardStaggered()) {
            event.setNewDamage(0.0F);
            return;
        }
        event.setNewDamage(event.getNewDamage() * profile.outgoingDamageMultiplierWhileStaggered());
    }

    public static void afterLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        if (!CombatConfig.combatOverhaulEnabled() || target.level().isClientSide() || event.getNewDamage() <= 0.0F) {
            return;
        }

        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity attacker && attacker != target && CombatConfig.recoveryWindowsEnabled()) {
            EntityCombatProfile attackerProfile = CombatProfileResolver.resolve(attacker).profile();
            if (attackerProfile.enabled() && attackerProfile.recoveryWindowTicks() > 0) {
                CombatStateManager.getOrCreate(attacker).markRecovery(attackerProfile.recoveryWindowTicks());
            }
        }

        if (target instanceof ServerPlayer playerTarget && source instanceof LivingEntity attacker && !(attacker instanceof ServerPlayer)) {
            ItemStack attackerWeapon = event.getSource().getWeaponItem();
            if (attackerWeapon == null || attackerWeapon.isEmpty()) {
                attackerWeapon = attacker.getMainHandItem();
            }
            PostureSystem.applyIncomingPostureDamage(attacker, playerTarget, attackerWeapon, event.getNewDamage());
        }

        if (!(source instanceof ServerPlayer player) || event.getSource().getDirectEntity() != player) {
            return;
        }
        ItemStack weapon = event.getSource().getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            weapon = player.getMainHandItem();
        }
        CombatState playerState = CombatStateManager.getOrCreate(player);
        float attackStrength = playerState.consumeAttackStrengthFor(target.getId(), player.tickCount);
        StrikeTiming timing = StrikeTiming.fromAttackStrength(attackStrength, playerState.counterWindowTicks() > 0);
        float postureMultiplier = playerState.consumeStealthStrikeFor(target.getId(), player.tickCount)
                ? CombatConfig.stealthStrikePostureMultiplier()
                : 1.0F;
        postureMultiplier *= DualWieldSystem.postureMultiplier(player, target.getId());
        PostureSystem.applyPlayerMeleePosture(player, target, weapon, timing, postureMultiplier);
    }

    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        StealthSystem.onLivingChangeTarget(event);
    }

    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        StealthSystem.onLivingVisibility(event);
    }

    public static void onShieldBlock(LivingShieldBlockEvent event) {
        GuardSystem.handleShieldBlock(event);
    }

    public static void onUseItemStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && isStaggered(entity)) {
            event.setCanceled(true);
            event.setDuration(0);
            return;
        }
        if (!entity.level().isClientSide() && WeaponProfileResolver.isShieldLike(event.getItem())) {
            CombatStateManager.getOrCreate(entity).markShieldRaised(entity.tickCount);
        }
    }

    public static void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && isStaggered(entity)) {
            event.setCanceled(true);
            event.setDuration(0);
            entity.stopUsingItem();
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        CombatState state = CombatStateManager.get(entity);
        if (state == null) {
            return;
        }
        EntityCombatProfile profile = CombatProfileResolver.resolve(entity).profile();
        state.tick(entity, profile);
        PostureSystem.applyHardStaggerMotionSafety(entity, profile, state);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()) {
            return;
        }
        DualWieldSystem.tick(player);
        if (player.tickCount % 5 != 0) {
            return;
        }
        CombatState state = CombatStateManager.get(player);
        if (state != null && state.maxPosture() > 0.0F) {
            ModNetworking.sendPlayerPosture(player, state.currentPosture(), state.maxPosture());
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            DualWieldSystem.clear(event.getEntity());
            CombatStateManager.remove(event.getEntity());
        }
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living) {
            DualWieldSystem.clear(living);
            CombatStateManager.remove(living);
        }
    }

    private static boolean isStaggered(LivingEntity entity) {
        CombatState state = CombatStateManager.get(entity);
        return state != null && state.isStaggered();
    }
}
