package org.destroyermob.mobscombat.combat;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public record WeaponCombatProfile(
        CombatDamageKind damageKind,
        float postureDamage,
        float guardDamage,
        float quickStrikePostureMultiplier,
        float fullStrikePostureMultiplier,
        float counterStrikePostureMultiplier,
        float recoveryPunishMultiplier,
        float knockbackMultiplier,
        boolean canHardStagger,
        boolean canSoftStagger
) {
    public static final WeaponCombatProfile GENERIC = new WeaponCombatProfile(
            CombatDamageKind.GENERIC,
            4.0F,
            5.0F,
            0.45F,
            1.0F,
            1.3F,
            1.15F,
            1.0F,
            false,
            true
    );

    public static final WeaponCombatProfile SWORD = new WeaponCombatProfile(
            CombatDamageKind.SLASH,
            8.0F,
            8.0F,
            0.45F,
            1.0F,
            1.35F,
            1.2F,
            1.0F,
            true,
            true
    );

    public static final WeaponCombatProfile AXE = new WeaponCombatProfile(
            CombatDamageKind.CHOP,
            10.0F,
            12.0F,
            0.4F,
            1.0F,
            1.35F,
            1.2F,
            1.05F,
            true,
            true
    );

    public static final WeaponCombatProfile MACE = new WeaponCombatProfile(
            CombatDamageKind.BLUNT,
            14.0F,
            16.0F,
            0.35F,
            1.0F,
            1.5F,
            1.25F,
            1.1F,
            true,
            true
    );

    public static WeaponCombatProfile fromJson(JsonObject object) {
        WeaponCombatProfile base = GENERIC;
        return new WeaponCombatProfile(
                CombatDamageKind.byName(GsonHelper.getAsString(object, "damage_kind", base.damageKind().name()), base.damageKind()),
                GsonHelper.getAsFloat(object, "posture_damage", base.postureDamage()),
                GsonHelper.getAsFloat(object, "guard_damage", base.guardDamage()),
                GsonHelper.getAsFloat(object, "quick_strike_posture_multiplier", base.quickStrikePostureMultiplier()),
                GsonHelper.getAsFloat(object, "full_strike_posture_multiplier", base.fullStrikePostureMultiplier()),
                GsonHelper.getAsFloat(object, "counter_strike_posture_multiplier", base.counterStrikePostureMultiplier()),
                GsonHelper.getAsFloat(object, "recovery_punish_multiplier", base.recoveryPunishMultiplier()),
                GsonHelper.getAsFloat(object, "knockback_multiplier", base.knockbackMultiplier()),
                GsonHelper.getAsBoolean(object, "can_hard_stagger", base.canHardStagger()),
                GsonHelper.getAsBoolean(object, "can_soft_stagger", base.canSoftStagger())
        );
    }
}
