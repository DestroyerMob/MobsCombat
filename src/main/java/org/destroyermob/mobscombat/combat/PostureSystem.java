package org.destroyermob.mobscombat.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.mixin.CreeperFuseAccessor;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class PostureSystem {
    private PostureSystem() {
    }

    public static void applyPlayerMeleePosture(ServerPlayer attacker, LivingEntity target, ItemStack weapon, StrikeTiming timing) {
        applyPlayerMeleePosture(attacker, target, weapon, timing, 1.0F);
    }

    public static void applyPlayerMeleePosture(ServerPlayer attacker, LivingEntity target, ItemStack weapon, StrikeTiming timing, float postureMultiplier) {
        if (!CombatConfig.postureEnabled() || target.level().isClientSide() || target == attacker) {
            return;
        }

        ResolvedEntityProfile resolvedTarget = CombatProfileResolver.resolve(target);
        EntityCombatProfile targetProfile = resolvedTarget.profile();
        if (!targetProfile.enabled()) {
            return;
        }
        if (target instanceof Player && !CombatConfig.pvpPostureEnabled()) {
            return;
        }

        ResolvedWeaponProfile resolvedWeapon = WeaponProfileResolver.resolve(weapon);
        if (!resolvedWeapon.recognizedWeapon()) {
            return;
        }

        CombatState targetState = CombatStateManager.getOrCreate(target);
        targetState.syncPostureMax(maxPosture(target, targetProfile));

        WeaponCombatProfile weaponProfile = resolvedWeapon.profile();

        float postureDamage = weaponProfile.postureDamage() * timing.postureMultiplier(weaponProfile);
        postureDamage *= Math.max(0.0F, postureMultiplier);
        postureDamage *= targetProfile.postureMultiplierFor(weaponProfile.damageKind());
        if (targetState.recoveryTicks() > 0 && CombatConfig.recoveryWindowsEnabled()) {
            postureDamage *= targetProfile.recoveryWindowPostureMultiplier();
            postureDamage *= weaponProfile.recoveryPunishMultiplier();
        }

        applyPostureDamage(attacker, target, targetProfile, weaponProfile, targetState, postureDamage, resolvedTarget.source(), resolvedWeapon.source());
    }

    public static void applyFlatPostureDamage(LivingEntity source, LivingEntity target, float amount, CombatDamageKind kind, boolean canHardStagger) {
        if (!CombatConfig.postureEnabled() || target.level().isClientSide() || source == target) {
            return;
        }
        ResolvedEntityProfile resolvedTarget = CombatProfileResolver.resolve(target);
        EntityCombatProfile targetProfile = resolvedTarget.profile();
        if (!targetProfile.enabled()) {
            return;
        }
        CombatState targetState = CombatStateManager.getOrCreate(target);
        targetState.syncPostureMax(maxPosture(target, targetProfile));
        WeaponCombatProfile syntheticProfile = new WeaponCombatProfile(kind, amount, amount, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, canHardStagger, true);
        applyPostureDamage(source, target, targetProfile, syntheticProfile, targetState, amount * targetProfile.postureMultiplierFor(kind), resolvedTarget.source(), "synthetic");
    }

    public static void applyIncomingPostureDamage(LivingEntity source, LivingEntity target, ItemStack weapon, float healthDamage) {
        if (!CombatConfig.postureEnabled() || target.level().isClientSide() || source == target || healthDamage <= 0.0F) {
            return;
        }
        if (target instanceof Player && !CombatConfig.playerPostureEnabled()) {
            return;
        }

        ResolvedEntityProfile resolvedTarget = CombatProfileResolver.resolve(target);
        EntityCombatProfile targetProfile = resolvedTarget.profile();
        if (!targetProfile.enabled()) {
            return;
        }

        CombatState targetState = CombatStateManager.getOrCreate(target);
        targetState.syncPostureMax(maxPosture(target, targetProfile));
        ResolvedWeaponProfile resolvedWeapon = WeaponProfileResolver.resolve(weapon);
        WeaponCombatProfile weaponProfile = resolvedWeapon.recognizedWeapon()
                ? resolvedWeapon.profile()
                : new WeaponCombatProfile(
                        CombatDamageKind.GENERIC,
                        Math.max(2.0F, healthDamage * 1.5F),
                        Math.max(2.0F, healthDamage * 1.5F),
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        false,
                        true
                );
        float postureDamage = weaponProfile.postureDamage() * targetProfile.postureMultiplierFor(weaponProfile.damageKind());
        applyPostureDamage(source, target, targetProfile, weaponProfile, targetState, postureDamage, resolvedTarget.source(), resolvedWeapon.recognizedWeapon() ? resolvedWeapon.source() : "incoming_damage");
        if (target instanceof ServerPlayer player) {
            ModNetworking.sendPlayerPosture(player, targetState.currentPosture(), targetState.maxPosture());
        }
    }

    public static float maxPosture(LivingEntity entity, EntityCombatProfile profile) {
        float health = maxHealth(entity);
        float armorBonus = armor(entity) * 1.5F + knockbackResistance(entity) * 20.0F;
        float base = health * profile.postureMultiplier() + profile.flatPostureBonus() + armorBonus;
        return Math.max(4.0F, base * CombatConfig.genericPostureBaseMultiplier());
    }

    public static void applyHardStaggerMotionSafety(LivingEntity entity, EntityCombatProfile profile, CombatState state) {
        if (!state.isStaggered()) {
            return;
        }
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setAggressive(false);
            if (state.isHardStaggered()) {
                mob.setTarget(null);
            }
        }
        cancelCreeperFuse(entity);
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }
        double multiplier = Mth.clamp(profile.movementSpeedMultiplierWhileStaggered(), 0.0F, 1.0F);
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(multiplier, 1.0D, multiplier));
    }

    private static void applyPostureDamage(
            LivingEntity source,
            LivingEntity target,
            EntityCombatProfile targetProfile,
            WeaponCombatProfile weaponProfile,
            CombatState targetState,
            float postureDamage,
            String targetProfileSource,
            String weaponProfileSource
    ) {
        if (postureDamage <= 0.0F) {
            return;
        }
        if (targetState.staggerCooldownTicks() > 0) {
            postureDamage *= CombatConfig.staggerCooldownPostureDamageMultiplier();
            if (postureDamage <= 0.0F) {
                return;
            }
            targetState.damagePosture(postureDamage, 1.0F);
        } else {
            targetState.damagePosture(postureDamage);
        }
        if (CombatConfig.debugMessagesEnabled() && source instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal("Posture " + shortId(target) + ": -" + format(postureDamage) + " -> " + format(targetState.currentPosture()) + "/" + format(targetState.maxPosture()) + " (" + targetProfileSource + ", " + weaponProfileSource + ")"));
        }
        if (targetState.currentPosture() <= 0.0F) {
            breakPosture(source, target, targetProfile, weaponProfile, targetState);
        }
    }

    private static void breakPosture(LivingEntity source, LivingEntity target, EntityCombatProfile targetProfile, WeaponCombatProfile weaponProfile, CombatState targetState) {
        if (targetState.staggerCooldownTicks() > 0) {
            targetState.restorePostureAfterBreak();
            return;
        }

        boolean hard = canHardStagger(target, targetProfile, weaponProfile);
        int duration = staggerDuration(targetProfile, hard);
        int cooldown = Math.max(targetProfile.staggerCooldownTicks(), CombatConfig.defaultStaggerCooldownTicks());
        targetState.stagger(Math.max(1, duration), cooldown, hard);
        targetState.restorePostureAfterBreak();
        cancelCreeperFuse(target);

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(
                    hard ? ParticleTypes.CRIT : ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.6D,
                    target.getZ(),
                    hard ? 10 : 5,
                    Math.max(target.getBbWidth() * 0.35D, 0.15D),
                    Math.max(target.getBbHeight() * 0.25D, 0.15D),
                    Math.max(target.getBbWidth() * 0.35D, 0.15D),
                    0.08D
            );
            level.playSound(null, target.blockPosition(), hard ? SoundEvents.SHIELD_BREAK : SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.6F, hard ? 0.9F : 1.25F);
        }
        if (CombatConfig.debugMessagesEnabled() && source instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal((hard ? "Hard" : "Soft") + " posture break: " + shortId(target)));
        }
    }

    private static boolean canHardStagger(LivingEntity target, EntityCombatProfile profile, WeaponCombatProfile weaponProfile) {
        if (!CombatConfig.hardStaggerEnabled() || !weaponProfile.canHardStagger() || !profile.allowsHardStagger()) {
            return false;
        }
        if (target instanceof Player) {
            return false;
        }
        if (profile.bossLike() && !CombatConfig.bossHardStaggerEnabled()) {
            return false;
        }
        return !target.getType().is(CombatTags.EntityTypes.NO_HARD_STAGGER);
    }

    private static int staggerDuration(EntityCombatProfile profile, boolean hard) {
        int profileDuration = hard ? profile.staggerDurationTicks() : Math.min(profile.staggerDurationTicks(), profile.bossLike() ? 4 : 8);
        if (profile.bossLike()) {
            return profileDuration;
        }
        int duration = Math.max(profileDuration, CombatConfig.defaultStaggerDurationTicks());
        return hard ? duration + 6 : duration;
    }

    private static void cancelCreeperFuse(LivingEntity entity) {
        if (!(entity instanceof Creeper creeper)) {
            return;
        }
        creeper.setSwellDir(-1);
        creeper.getEntityData().set(CreeperFuseAccessor.mobscombat$ignitedDataAccessor(), false);
        CreeperFuseAccessor accessor = (CreeperFuseAccessor) creeper;
        accessor.mobscombat$setSwell(0);
        accessor.mobscombat$setOldSwell(0);
    }

    private static float maxHealth(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        return instance == null ? entity.getMaxHealth() : (float) instance.getValue();
    }

    private static float armor(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ARMOR);
        return instance == null ? 0.0F : (float) instance.getValue();
    }

    private static float knockbackResistance(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        return instance == null ? 0.0F : (float) instance.getValue();
    }

    private static String shortId(LivingEntity entity) {
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static String format(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
