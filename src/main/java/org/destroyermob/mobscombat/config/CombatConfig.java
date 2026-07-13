package org.destroyermob.mobscombat.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.destroyermob.mobscombat.integration.bettercombat.BetterCombatCompat;

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
    private static final ModConfigSpec.DoubleValue STAGGER_COOLDOWN_POSTURE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue POSTURE_AFTER_BREAK_RATIO;
    private static final ModConfigSpec.DoubleValue BOSS_HEALTH_THRESHOLD;
    private static final ModConfigSpec.IntValue PERFECT_BLOCK_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue COUNTER_WINDOW_TICKS;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_COOLDOWN_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_FINISHER_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DUAL_WIELD_FINISHER_POSTURE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue GUARD_RECOVERY_PER_SECOND;
    private static final ModConfigSpec.DoubleValue DEFAULT_GUARD_MAX;
    private static final ModConfigSpec.IntValue GUARD_RECOVERY_DELAY_TICKS;
    private static final ModConfigSpec.IntValue GUARD_BREAK_TICKS;
    private static final ModConfigSpec.DoubleValue STEALTH_SNEAKING_VISIBILITY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue STEALTH_CONE_DEGREES;
    private static final ModConfigSpec.DoubleValue STEALTH_SNEAKING_CONE_DEGREES;
    private static final ModConfigSpec.DoubleValue STEALTH_CLOSE_RANGE_BLOCKS;
    private static final ModConfigSpec.DoubleValue STEALTH_SNEAKING_CLOSE_RANGE_BLOCKS;
    private static final ModConfigSpec.DoubleValue STEALTH_STRIKE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue BACKSTAB_DAMAGE_BONUS_PER_LEVEL;
    private static final ModConfigSpec.DoubleValue STEALTH_STRIKE_POSTURE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue ENABLE_PROJECTILE_HEADSHOTS;
    private static final ModConfigSpec.DoubleValue PROJECTILE_HEADSHOT_DAMAGE_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue PROJECTILE_HEADSHOT_UPPER_EYE_BAND;
    private static final ModConfigSpec.DoubleValue PROJECTILE_HEADSHOT_LOWER_EYE_BAND;

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
        STAGGER_COOLDOWN_POSTURE_DAMAGE_MULTIPLIER = builder.defineInRange("stagger_cooldown_posture_damage_multiplier", 0.15D, 0.0D, 1.0D);
        POSTURE_AFTER_BREAK_RATIO = builder.defineInRange("posture_after_break_ratio", 0.2D, 0.0D, 1.0D);
        BOSS_HEALTH_THRESHOLD = builder.defineInRange("boss_health_threshold", 100.0D, 1.0D, 100000.0D);
        builder.pop();

        builder.push("guard");
        ENABLE_SHIELD_GUARD = builder.define("enable_shield_guard", true);
        ENABLE_TIMED_BLOCKS = builder.define("enable_timed_blocks", true);
        ENABLE_PARRY = builder.define("enable_parry", true);
        PERFECT_BLOCK_WINDOW_TICKS = builder.defineInRange("perfect_block_window_ticks", 6, 0, 200);
        COUNTER_WINDOW_TICKS = builder.defineInRange("counter_window_ticks", 20, 0, 72000);
        ENABLE_DUAL_WIELD = builder.define("enable_dual_wield", true);
        DUAL_WIELD_COOLDOWN_MULTIPLIER = builder.defineInRange("dual_wield_cooldown_multiplier", 0.65D, 0.05D, 1.0D);
        DUAL_WIELD_DAMAGE_MULTIPLIER = builder.defineInRange("dual_wield_damage_multiplier", 0.67D, 0.0D, 100.0D);
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
        STEALTH_CONE_DEGREES = builder.defineInRange("hostile_vision_cone_degrees", 135.0D, 1.0D, 360.0D);
        STEALTH_SNEAKING_CONE_DEGREES = builder.defineInRange("sneaking_vision_cone_degrees", 90.0D, 1.0D, 360.0D);
        STEALTH_CLOSE_RANGE_BLOCKS = builder.defineInRange("close_range_awareness_blocks", 3.0D, 0.0D, 64.0D);
        STEALTH_SNEAKING_CLOSE_RANGE_BLOCKS = builder.defineInRange("sneaking_close_range_awareness_blocks", 0.0D, 0.0D, 64.0D);
        STEALTH_STRIKE_DAMAGE_MULTIPLIER = builder.defineInRange("stealth_strike_damage_multiplier", 1.35D, 1.0D, 100.0D);
        DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER = builder.defineInRange("dagger_stealth_strike_damage_multiplier", 1.75D, 1.0D, 100.0D);
        BACKSTAB_DAMAGE_BONUS_PER_LEVEL = builder.defineInRange("backstab_damage_bonus_per_level", 0.25D, 0.0D, 100.0D);
        STEALTH_STRIKE_POSTURE_MULTIPLIER = builder.defineInRange("stealth_strike_posture_multiplier", 1.5D, 1.0D, 100.0D);
        builder.pop();

        builder.push("headshots");
        ENABLE_PROJECTILE_HEADSHOTS = builder
                .comment("Whether player-owned projectiles deal bonus damage when they strike a living target's head.")
                .define("enable_projectile_headshots", true);
        PROJECTILE_HEADSHOT_DAMAGE_MULTIPLIER = builder
                .comment("Universal projectile headshot damage multiplier. Better Enchanting's Headshot bonus stacks multiplicatively with this value.")
                .defineInRange("projectile_headshot_damage_multiplier", 1.25D, 1.0D, 100.0D);
        PROJECTILE_HEADSHOT_UPPER_EYE_BAND = builder
                .comment("Highest valid projectile impact above eye height, measured as a fraction of the target's full hitbox height.")
                .defineInRange("projectile_headshot_upper_eye_band", 0.20D, -1.0D, 1.0D);
        PROJECTILE_HEADSHOT_LOWER_EYE_BAND = builder
                .comment("Lowest valid projectile impact below eye height, measured as a fraction of the target's full hitbox height.")
                .defineInRange("projectile_headshot_lower_eye_band", -0.10D, -1.0D, 1.0D);
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
        // Better Combat owns attack selection, hand swapping, cooldown and animation
        // whenever it is installed. The config remains effective in standalone mode.
        return combatOverhaulEnabled() && ENABLE_DUAL_WIELD.get() && !BetterCombatCompat.isLoaded();
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

    public static float staggerCooldownPostureDamageMultiplier() {
        return STAGGER_COOLDOWN_POSTURE_DAMAGE_MULTIPLIER.get().floatValue();
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

    public static float dualWieldCooldownMultiplier() {
        return DUAL_WIELD_COOLDOWN_MULTIPLIER.get().floatValue();
    }

    public static float dualWieldDamageMultiplier() {
        return DUAL_WIELD_DAMAGE_MULTIPLIER.get().floatValue();
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

    public static float sneakingVisionConeDegrees() {
        return STEALTH_SNEAKING_CONE_DEGREES.get().floatValue();
    }

    public static float closeRangeAwarenessBlocks() {
        return STEALTH_CLOSE_RANGE_BLOCKS.get().floatValue();
    }

    public static float sneakingCloseRangeAwarenessBlocks() {
        return STEALTH_SNEAKING_CLOSE_RANGE_BLOCKS.get().floatValue();
    }

    public static float stealthStrikeDamageMultiplier() {
        return STEALTH_STRIKE_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float daggerStealthStrikeDamageMultiplier() {
        return DAGGER_STEALTH_STRIKE_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static float backstabDamageBonusPerLevel() {
        return BACKSTAB_DAMAGE_BONUS_PER_LEVEL.get().floatValue();
    }

    public static float stealthStrikePostureMultiplier() {
        return STEALTH_STRIKE_POSTURE_MULTIPLIER.get().floatValue();
    }

    public static boolean projectileHeadshotsEnabled() {
        return combatOverhaulEnabled() && ENABLE_PROJECTILE_HEADSHOTS.get();
    }

    public static float projectileHeadshotDamageMultiplier() {
        return PROJECTILE_HEADSHOT_DAMAGE_MULTIPLIER.get().floatValue();
    }

    public static double projectileHeadshotUpperEyeBand() {
        return PROJECTILE_HEADSHOT_UPPER_EYE_BAND.get();
    }

    public static double projectileHeadshotLowerEyeBand() {
        return PROJECTILE_HEADSHOT_LOWER_EYE_BAND.get();
    }
}
