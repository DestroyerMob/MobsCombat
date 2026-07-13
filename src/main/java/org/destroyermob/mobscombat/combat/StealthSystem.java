package org.destroyermob.mobscombat.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class StealthSystem {
    private static final double VECTOR_EPSILON = 1.0E-5D;
    private static final ResourceKey<Enchantment> BACKSTAB = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath("betterenchanting", "backstab")
    );

    private StealthSystem() {
    }

    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (!CombatConfig.stealthEnabled() || !(event.getEntity() instanceof ServerPlayer player) || !isTryingToSneak(player)) {
            return;
        }
        Entity lookingEntity = event.getLookingEntity();
        if (lookingEntity instanceof Mob mob && isHostileMob(mob)) {
            event.modifyVisibility(CombatConfig.sneakingVisibilityMultiplier());
        }
    }

    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!CombatConfig.stealthEnabled() || event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof Mob mob) || !(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) {
            return;
        }
        if (isHiddenFrom(mob, player)) {
            event.setCanceled(true);
        }
    }

    public static boolean tryApplyStealthStrike(LivingDamageEvent.Pre event) {
        if (!CombatConfig.stealthEnabled() || event.getNewDamage() <= 0.0F) {
            return false;
        }

        LivingEntity target = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player) || event.getSource().getDirectEntity() != player || target instanceof Player) {
            return false;
        }
        if (!(target instanceof Mob mob) || !isHiddenFrom(mob, player)) {
            return false;
        }

        ItemStack weapon = event.getSource().getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            weapon = player.getMainHandItem();
        }

        boolean dagger = weapon.is(CombatTags.Items.DAGGERS);
        float multiplier = dagger
                ? CombatConfig.daggerStealthStrikeDamageMultiplier()
                    + backstabLevel(player, weapon) * CombatConfig.backstabDamageBonusPerLevel()
                : CombatConfig.stealthStrikeDamageMultiplier();
        event.setNewDamage(event.getNewDamage() * multiplier);
        CombatStateManager.getOrCreate(player).markStealthStrike(target.getId(), player.tickCount);
        playStealthStrikeFeedback(player, target);
        ModNetworking.sendCombatFeedback(player, CombatFeedbackType.STEALTH_STRIKE);
        return true;
    }

    public static boolean isHiddenFrom(Mob observer, ServerPlayer player) {
        if (!isHostileMob(observer) || isAlertedTo(observer, player)) {
            return false;
        }

        float closeRange = isTryingToSneak(player)
                ? CombatConfig.sneakingCloseRangeAwarenessBlocks()
                : CombatConfig.closeRangeAwarenessBlocks();
        if (closeRange > 0.0F && observer.distanceToSqr(player) <= closeRange * closeRange) {
            return false;
        }

        float coneDegrees = isTryingToSneak(player)
                ? CombatConfig.sneakingVisionConeDegrees()
                : CombatConfig.hostileVisionConeDegrees();
        return !observer.getSensing().hasLineOfSight(player)
                || !isInsideVisionCone(observer, player, coneDegrees);
    }

    private static boolean isAlertedTo(Mob observer, ServerPlayer player) {
        return observer.getTarget() == player || observer.getLastHurtByMob() == player;
    }

    private static boolean isHostileMob(Mob mob) {
        return mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    private static boolean isTryingToSneak(ServerPlayer player) {
        return player.isShiftKeyDown() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean isInsideVisionCone(LivingEntity viewer, LivingEntity target, float coneDegrees) {
        double clampedCone = Math.max(1.0D, Math.min(360.0D, coneDegrees));
        double headYaw = Math.toRadians(viewer.getYHeadRot());
        Vec3 forward = new Vec3(-Math.sin(headYaw), 0.0D, Math.cos(headYaw));
        Vec3 toTarget = target.position().subtract(viewer.position());
        Vec3 direction = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (forward.lengthSqr() < VECTOR_EPSILON || direction.lengthSqr() < VECTOR_EPSILON) {
            return true;
        }
        double threshold = Math.cos(Math.toRadians(clampedCone * 0.5D));
        return forward.normalize().dot(direction.normalize()) >= threshold;
    }

    private static int backstabLevel(ServerPlayer player, ItemStack weapon) {
        return player.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(BACKSTAB)
                .map(weapon::getEnchantmentLevel)
                .orElse(0);
    }

    private static void playStealthStrikeFeedback(ServerPlayer player, LivingEntity target) {
        if (target.level() instanceof ServerLevel level) {
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.75F, 0.85F);
            level.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.65D,
                    target.getZ(),
                    12,
                    Math.max(target.getBbWidth() * 0.3D, 0.12D),
                    Math.max(target.getBbHeight() * 0.25D, 0.12D),
                    Math.max(target.getBbWidth() * 0.3D, 0.12D),
                    0.08D
            );
        }
    }
}
