package org.destroyermob.mobscombat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CombatConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_COMBAT_OVERHAUL;
    private static final ModConfigSpec.BooleanValue ENABLE_POSTURE;
    private static final ModConfigSpec.BooleanValue ENABLE_SHIELD_GUARD;
    private static final ModConfigSpec.BooleanValue ENABLE_TIMED_BLOCKS;
    private static final ModConfigSpec.BooleanValue ENABLE_PARRY;
    private static final ModConfigSpec.BooleanValue ENABLE_STEALTH;
    private static final ModConfigSpec.BooleanValue ENABLE_DUAL_WIELD;
    private static final ModConfigSpec.BooleanValue ENABLE_RECOVERY_WINDOWS;
    private static final ModConfigSpec.BooleanValue ENABLE_PLAYER_POSTURE;
    private static final ModConfigSpec.BooleanValue ENABLE_PVP_POSTURE;
    private static final ModConfigSpec.BooleanValue ENABLE_HARD_STAGGER;
    private static final ModConfigSpec.BooleanValue ENABLE_BOSS_HARD_STAGGER;
    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG_MESSAGES;
    private static final ModConfigSpec.DoubleValue GENERIC_POSTURE_BASE_MULTIPLIER;
    private static final ModConfigSpec.IntValue POSTURE_RECOVERY_DELAY_TICKS;
    private static final ModConfigSpec.IntValue DEFAULT_STAGGER_DURATION_TICKS;
    private static final ModConfigSpec.IntValue DEFAULT_STAGGER_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue POSTURE_AFTER_BREAK_RATIO;
    private static final ModConfigSpec.DoubleValue BOSS_HEALTH_THRESHOLD;
    private static final ModConfigSpec.IntValue PERFECT_BLOCK_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue COUNTER_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue PARRY_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue DAGGER_PARRY_WINDOW_BONUS_TICKS;
    private static final ModConfigSpec.DoubleValue PARRY_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue PARRY_POSTURE_DAMAGE;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_COOLDOWN_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_FINISHER_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_FINISHER_POSTURE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue GUARD_RECOVERY_PER_SECOND;
    private static final ModConfigSpec.DoubleValue DEFAULT_GUARD_MAX;
    private static final ModConfigSpec.IntValue GUARD_RECOVERY_DELAY_TICKS;
    private static final ModConfigSpec.IntValue GUARD_BREAK_TICKS;
    private static final ModConfigSpec.DoubleValue STEALTH_SNEAKING_VISIBILITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue STEALTH_CONE_DEGREES;
    private static final ModConfigSpec.DoubleValue STEALTH_CLOSE_RANGE_BLOCKS;
    private static final ModConfigSpec.DoubleValue STEALTH_STRIKE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue STEALTH_STRIKE_POSTURE_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_COMBAT_OVERHAUL = builder.define("enable_combat_overhaul", true);
        ENABLE_DEBUG_MESSAGES = builder.define("enable_debug_messages", false);
        builder.pop();

        builder.push("posture");
        ENABLE_POSTURE = builder.define("enable_posture", true);
        ENABLE_RECOVERY_WINDOWS = builder.define("enable_recovery_windows", true);
        ENABLE_PLAYER_POSTURE = builder.define("enable_player_posture", true);
        ENABLE_PVP_POSTURE = builder.define("enable_pvp_posture", false);
        ENABLE_HARD_STAGGER = builder.define("enable_hard_stagger", true);
        ENABLE_BOSS_HARD_STAGGER = builder.define("enable_boss_hard_stagger", false);
        GENERIC_POSTURE_BASE_MULTIPLIER = builder.defineInRange("generic_posture_base_multiplier", 1.0D, 0.0D, 100.0D);
        POSTURE_RECOVERY_DELAY_TICKS = builder.defineInRange("posture_recovery_delay_ticks", 20, 0, 72000);
        DEFAULT_STAGGER_DURATION_TICKS = builder.defineInRange("default_stagger_duration_ticks", 24, 0, 72000);
        DEFAULT_STAGGER_COOLDOWN_TICKS = builder.defineInRange("default_stagger_cooldown_ticks", 50, 0, 72000);
        POSTURE_AFTER_BREAK_RATIO = builder.defineInRange("posture_after_break_ratio", 0.2D, 0.0D, 1.0D);
        BOSS_HEALTH_THRESHOLD = builder.defineInRange("boss_health_threshold", 100.0D, 1.0D, 100000.0D);
        builder.pop();

        builder.push("guard");
        ENABLE_SHIELD_GUARD = builder.define("enable_shield_guard", true);
        ENABLE_TIMED_BLOCKS = builder.define("enable_timed_blocks", true);
        ENABLE_PARRY = builder.define("enable_parry", true);
        PERFECT_BLOCK_WINDOW_TICKS = builder.defineInRange("perfect_block_window_ticks", 6, 0, 200);
        COUNTER_WINDOW_TICKS = builder.defineInRange("counter_window_ticks", 20, 0, 72000);
        PARRY_WINDOW_TICKS = builder.defineInRange("parry_window_ticks", 5, 0, 200);
        DAGGER_PARRY_WINDOW_BONUS_TICKS = builder.defineInRange("dagger_parry_window_bonus_ticks", 3, 0, 200);
        PARRY_DAMAGE_MULTIPLIER = builder.defineInRange("parry_damage_multiplier", 0.0D, 0.0D, 1.0D);
        PARRY_POSTURE_DAMAGE = builder.defineInRange("parry_posture_damage", 8.0D, 0.0D, 10000.0D);
        ENABLE_DUAL_WIELD = builder.define("enable_dual_wield", true);
        DUAL_WIELD_COOLDOWN_MULTIPLIER = builder.defineInRange("dual_wield_cooldown_multiplier", 0.65D, 0.05D, 1.0D);
        DUAL_WIELD_FINISHER_DAMAGE_MULTIPLIER = builder.defineInRange("dual_wield_finisher_damage_multiplier", 1.35D, 1.0D, 100.0D);
        DUAL_WIELD_FINISHER_POSTURE_MULTIPLIER = builder.defineInRange("dual_wield_finisher_posture_multiplier", 1.5D, 1.0D, 100.0D);
        GUARD_RECOVERY_PER_SECOND = builder.defineInRange("guard_recovery_per_second", 20.0D, 0.0D, 10000.0D);
        DEFAULT_GUARD_MAX = builder.defineInRange("default_guard_max", 100.0D, 1.0D, 10000.0D);
        GUARD_RECOVERY_DELAY_TICKS = builder.defineInRange("guard_recovery_delay_ticks", 20, 0, 72000);
        GUARD_BREAK_TICKS = builder.defineInRange("guard_break_ticks", 35, 0, 72000);
        builder.pop();

        builder.push("stealth");
        ENABLE_STEALTH = builder.define("enable_stealth", true);
        STEALTH_SNEAKING_VISIBILITY_MULTIPLIER = builder.defineInRange("sneaking_visibility_multiplier", 0.35D, 0.0D, 1.0D);
        STEALTH_CONE_DEGREES = builder.defineInRange("hostile_vision_cone_degrees", 130.0D, 1.0D, 360.0D);
        STEALTH_CLOSE_RANGE_BLOCKS = builder.defineInRange("close_range_awareness_blocks", 3.0D, 0.0D, 64.0D);
        STEALTH_STRIKE_DAMAGE_MULTIPLIER = builder.defineInRange("stealth_strike_damage_multiplier", 1.35D, 1.0D, 100.0D);
        DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER = builder.defineInRange("dagger_stealth_strike_damage_multiplier", 1.75D, 1.0D, 100.0D);
        STEALTH_STRIKE_POSTURE_MULTIPLIER = builder.defineInRange("stealth_strike_posture_multiplier", 1.5D, 1.0D, 100.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private CombatConfig() {
    }

    public static boolean combatOverhaulEnabled() {
        return ENABLE_COMBAT_OVERHAUL.get();
    }

    public static boolean postureEnabled() {
        return combatOverhaulEnabled() && ENABLE_POSTURE.get();
    }

    public static boolean shieldGuardEnabled() {
        return combatOverhaulEnabled() && ENABLE_SHIELD_GUARD.get();
    }

    public static boolean timedBlocksEnabled() {
        return shieldGuardEnabled() && ENABLE_TIMED_BLOCKS.get();
    }

    public static boolean parryEnabled() {
        return combatOverhaulEnabled() && ENABLE_PARRY.get();
    }

    public static boolean stealthEnabled() {
        return combatOverhaulEnabled() && ENABLE_STEALTH.get();
    }

    public static boolean dualWieldEnabled() {
        return combatOverhaulEnabled() && ENABLE_DUAL_WIELD.get();
    }

    public static boolean recoveryWindowsEnabled() {
        return combatOverhaulEnabled() && ENABLE_RECOVERY_WINDOWS.get();
    }

    public static boolean pvpPostureEnabled() {
        return ENABLE_PVP_POSTURE.get();
    }

    public static boolean playerPostureEnabled() {
        return postureEnabled() && ENABLE_PLAYER_POSTURE.get();
    }

    public static boolean hardStaggerEnabled() {
        return postureEnabled() && ENABLE_HARD_STAGGER.get();
    }

    public static boolean bossHardStaggerEnabled() {
        return hardStaggerEnabled() && ENABLE_BOSS_HARD_STAGGER.get();
    }

    public static boolean debugMessagesEnabled() {
        return ENABLE_DEBUG_MESSAGES.get();
    }

    public static float genericPostureBaseMultiplier() {
        return GENERIC_POSTURE_BASE_MULTIPLIER.get().floatValue();
    }

    public static int postureRecoveryDelayTicks() {
        return POSTURE_RECOVERY_DELAY_TICKS.get();
    }

    public static int defaultStaggerDurationTicks() {
        return DEFAULT_STAGGER_DURATION_TICKS.get();
    }

    public static int defaultStaggerCooldownTicks() {
        return DEFAULT_STAGGER_COOLDOWN_TICKS.get();
    }

    public static float postureAfterBreakRatio() {
        return POSTURE_AFTER_BREAK_RATIO.get().floatValue();
    }

    public static float bossHealthThreshold() {
        return BOSS_HEALTH_THRESHOLD.get().floatValue();
    }

    public static int perfectBlockWindowTicks() {
        return PERFECT_BLOCK_WINDOW_TICKS.get();
    }

    public static int counterWindowTicks() {
        return COUNTER_WINDOW_TICKS.get();
    }

    public static int parryWindowTicks() {
        return PARRY_WINDOW_TICKS.get();
    }

    public static int daggerParryWindowBonusTicks() {
        return DAGGER_PARRY_WINDOW_BONUS_TICKS.get();
    }

    public static float parryDamageMultiplier() {
        return PARRY_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float parryPostureDamage() {
        return PARRY_POSTURE_DAMAGE.get().floatValue();
    }

    public static float dualWieldCooldownMultiplier() {
        return DUAL_WIELD_COOLDOWN_MULTIPLIER.get().floatValue();
    }

    public static float dualWieldFinisherDamageMultiplier() {
        return DUAL_WIELD_FINISHER_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float dualWieldFinisherPostureMultiplier() {
        return DUAL_WIELD_FINISHER_POSTURE_MULTIPLIER.get().floatValue();
    }

    public static float guardRecoveryPerTick() {
        return GUARD_RECOVERY_PER_SECOND.get().floatValue() / 20.0F;
    }

    public static float defaultGuardMax() {
        return DEFAULT_GUARD_MAX.get().floatValue();
    }

    public static int guardRecoveryDelayTicks() {
        return GUARD_RECOVERY_DELAY_TICKS.get();
    }

    public static int guardBreakTicks() {
        return GUARD_BREAK_TICKS.get();
    }

    public static float sneakingVisibilityMultiplier() {
        return STEALTH_SNEAKING_VISIBILITY_MULTIPLIER.get().floatValue();
    }

    public static float hostileVisionConeDegrees() {
        return STEALTH_CONE_DEGREES.get().floatValue();
    }

    public static float closeRangeAwarenessBlocks() {
        return STEALTH_CLOSE_RANGE_BLOCKS.get().floatValue();
    }

    public static float stealthStrikeDamageMultiplier() {
        return STEALTH_STRIKE_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float daggerStealthStrikeDamageMultiplier() {
        return DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float stealthStrikePostureMultiplier() {
        return STEALTH_STRIKE_POSTURE_MULTIPLIER.get().floatValue();
    }
}
