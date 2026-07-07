package org.destroyermob.mobscombat.combat;

import com.google.gson.JsonObject;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.GsonHelper;

public record EntityCombatProfile(
        boolean enabled,
        CombatArchetype archetype,
        float postureMultiplier,
        float flatPostureBonus,
        float postureRecoveryPerSecond,
        int staggerDurationTicks,
        int staggerCooldownTicks,
        float outgoingDamageMultiplierWhileStaggered,
        float movementSpeedMultiplierWhileStaggered,
        boolean hardStaggerAllowed,
        boolean trueInterruptAllowed,
        boolean knockbackOverrideAllowed,
        boolean bossLike,
        float guardDamageMultiplier,
        Map<CombatDamageKind, Float> postureDamageTakenMultipliers,
        Map<CombatDamageKind, Float> healthDamageTakenMultipliers,
        float recoveryWindowPostureMultiplier,
        int recoveryWindowTicks
) {
    public static final EntityCombatProfile DISABLED = new EntityCombatProfile(
            false,
            CombatArchetype.NO_OP,
            0.0F,
            0.0F,
            0.0F,
            0,
            0,
            1.0F,
            1.0F,
            false,
            false,
            false,
            false,
            1.0F,
            Map.of(),
            Map.of(),
            1.0F,
            0
    );

    public static final EntityCombatProfile GENERIC_HOSTILE = new EntityCombatProfile(
            true,
            CombatArchetype.GENERIC_HOSTILE,
            1.0F,
            0.0F,
            1.0F,
            10,
            50,
            0.45F,
            0.55F,
            true,
            false,
            true,
            false,
            1.0F,
            Map.of(),
            Map.of(),
            1.2F,
            8
    );

    public static final EntityCombatProfile BOSS_LIKE = new EntityCombatProfile(
            true,
            CombatArchetype.BOSS,
            2.0F,
            40.0F,
            4.0F,
            4,
            120,
            0.85F,
            0.9F,
            false,
            false,
            false,
            true,
            1.5F,
            Map.of(),
            Map.of(),
            1.1F,
            6
    );

    public static EntityCombatProfile fromJson(JsonObject object) {
        EntityCombatProfile base = GENERIC_HOSTILE;
        String archetypeName = GsonHelper.getAsString(object, "archetype", base.archetype().name());
        CombatArchetype archetype = CombatArchetype.byName(archetypeName, base.archetype());
        boolean bossLike = GsonHelper.getAsBoolean(object, "boss_like", archetype == CombatArchetype.BOSS || base.bossLike());

        return new EntityCombatProfile(
                GsonHelper.getAsBoolean(object, "enabled", base.enabled()),
                archetype,
                GsonHelper.getAsFloat(object, "posture_multiplier", base.postureMultiplier()),
                GsonHelper.getAsFloat(object, "flat_posture_bonus", base.flatPostureBonus()),
                GsonHelper.getAsFloat(object, "posture_recovery_per_second", base.postureRecoveryPerSecond()),
                GsonHelper.getAsInt(object, "stagger_duration_ticks", base.staggerDurationTicks()),
                GsonHelper.getAsInt(object, "stagger_cooldown_ticks", base.staggerCooldownTicks()),
                GsonHelper.getAsFloat(object, "outgoing_damage_multiplier_while_staggered", base.outgoingDamageMultiplierWhileStaggered()),
                GsonHelper.getAsFloat(object, "movement_speed_multiplier_while_staggered", base.movementSpeedMultiplierWhileStaggered()),
                GsonHelper.getAsBoolean(object, "hard_stagger_allowed", base.hardStaggerAllowed() && !bossLike),
                GsonHelper.getAsBoolean(object, "true_interrupt_allowed", base.trueInterruptAllowed()),
                GsonHelper.getAsBoolean(object, "knockback_override_allowed", base.knockbackOverrideAllowed() && !bossLike),
                bossLike,
                GsonHelper.getAsFloat(object, "guard_damage_multiplier", base.guardDamageMultiplier()),
                readKindMap(object, "posture_damage_taken_multipliers"),
                readKindMap(object, "health_damage_taken_multipliers"),
                GsonHelper.getAsFloat(object, "recovery_window_posture_multiplier", base.recoveryWindowPostureMultiplier()),
                GsonHelper.getAsInt(object, "recovery_window_ticks", base.recoveryWindowTicks())
        );
    }

    public EntityCombatProfile asBossLike() {
        return new EntityCombatProfile(
                this.enabled,
                CombatArchetype.BOSS,
                Math.max(this.postureMultiplier, BOSS_LIKE.postureMultiplier),
                Math.max(this.flatPostureBonus, BOSS_LIKE.flatPostureBonus),
                Math.max(this.postureRecoveryPerSecond, BOSS_LIKE.postureRecoveryPerSecond),
                Math.min(this.staggerDurationTicks, BOSS_LIKE.staggerDurationTicks),
                Math.max(this.staggerCooldownTicks, BOSS_LIKE.staggerCooldownTicks),
                Math.max(this.outgoingDamageMultiplierWhileStaggered, BOSS_LIKE.outgoingDamageMultiplierWhileStaggered),
                Math.max(this.movementSpeedMultiplierWhileStaggered, BOSS_LIKE.movementSpeedMultiplierWhileStaggered),
                false,
                false,
                false,
                true,
                Math.max(this.guardDamageMultiplier, BOSS_LIKE.guardDamageMultiplier),
                this.postureDamageTakenMultipliers,
                this.healthDamageTakenMultipliers,
                Math.min(this.recoveryWindowPostureMultiplier, BOSS_LIKE.recoveryWindowPostureMultiplier),
                Math.min(this.recoveryWindowTicks, BOSS_LIKE.recoveryWindowTicks)
        );
    }

    public float postureMultiplierFor(CombatDamageKind kind) {
        return this.postureDamageTakenMultipliers.getOrDefault(kind, 1.0F);
    }

    public boolean allowsHardStagger() {
        return this.hardStaggerAllowed && !this.bossLike;
    }

    private static Map<CombatDamageKind, Float> readKindMap(JsonObject object, String memberName) {
        if (!object.has(memberName) || !object.get(memberName).isJsonObject()) {
            return Map.of();
        }
        EnumMap<CombatDamageKind, Float> values = new EnumMap<>(CombatDamageKind.class);
        JsonObject mapObject = GsonHelper.getAsJsonObject(object, memberName);
        for (Map.Entry<String, com.google.gson.JsonElement> entry : mapObject.entrySet()) {
            CombatDamageKind kind = CombatDamageKind.byName(entry.getKey(), CombatDamageKind.GENERIC);
            values.put(kind, GsonHelper.convertToFloat(entry.getValue(), memberName + "." + entry.getKey()));
        }
        return Map.copyOf(values);
    }
}
