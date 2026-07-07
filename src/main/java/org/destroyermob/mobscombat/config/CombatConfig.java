package org.destroyermob.mobscombat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CombatConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_COMBAT_OVERHAUL;
    private static final ModConfigSpec.BooleanValue ENABLE_POSTURE;
    private static final ModConfigSpec.BooleanValue ENABLE_SHIELD_GUARD;
    private static final ModConfigSpec.BooleanValue ENABLE_TIMED_BLOCKS;
    private static final ModConfigSpec.BooleanValue ENABLE_RECOVERY_WINDOWS;
    private static final ModConfigSpec.BooleanValue ENABLE_PVP_POSTURE;
    private static final ModConfigSpec.BooleanValue ENABLE_HARD_STAGGER;
    private static final ModConfigSpec.BooleanValue ENABLE_BOSS_HARD_STAGGER;
    private static final ModConfigSpec.BooleanValue ENABLE_DEBUG_MESSAGES;
    private static final ModConfigSpec.DoubleValue GENERIC_POSTURE_BASE_MULTIPLIER;
    private static final ModConfigSpec.IntValue POSTURE_RECOVERY_DELAY_TICKS;
    private static final ModConfigSpec.IntValue DEFAULT_STAGGER_DURATION_TICKS;
    private static final ModConfigSpec.IntValue DEFAULT_STAGGER_COOLDOWN_TICKS;
    private static final ModConfigSpec.DoubleValue BOSS_HEALTH_THRESHOLD;
    private static final ModConfigSpec.IntValue PERFECT_BLOCK_WINDOW_TICKS;
    private static final ModConfigSpec.IntValue COUNTER_WINDOW_TICKS;
    private static final ModConfigSpec.DoubleValue GUARD_RECOVERY_PER_SECOND;
    private static final ModConfigSpec.DoubleValue DEFAULT_GUARD_MAX;
    private static final ModConfigSpec.IntValue GUARD_RECOVERY_DELAY_TICKS;
    private static final ModConfigSpec.IntValue GUARD_BREAK_TICKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_COMBAT_OVERHAUL = builder.define("enable_combat_overhaul", true);
        ENABLE_DEBUG_MESSAGES = builder.define("enable_debug_messages", false);
        builder.pop();

        builder.push("posture");
        ENABLE_POSTURE = builder.define("enable_posture", true);
        ENABLE_RECOVERY_WINDOWS = builder.define("enable_recovery_windows", true);
        ENABLE_PVP_POSTURE = builder.define("enable_pvp_posture", false);
        ENABLE_HARD_STAGGER = builder.define("enable_hard_stagger", true);
        ENABLE_BOSS_HARD_STAGGER = builder.define("enable_boss_hard_stagger", false);
        GENERIC_POSTURE_BASE_MULTIPLIER = builder.defineInRange("generic_posture_base_multiplier", 1.0D, 0.0D, 100.0D);
        POSTURE_RECOVERY_DELAY_TICKS = builder.defineInRange("posture_recovery_delay_ticks", 20, 0, 72000);
        DEFAULT_STAGGER_DURATION_TICKS = builder.defineInRange("default_stagger_duration_ticks", 10, 0, 72000);
        DEFAULT_STAGGER_COOLDOWN_TICKS = builder.defineInRange("default_stagger_cooldown_ticks", 50, 0, 72000);
        BOSS_HEALTH_THRESHOLD = builder.defineInRange("boss_health_threshold", 100.0D, 1.0D, 100000.0D);
        builder.pop();

        builder.push("guard");
        ENABLE_SHIELD_GUARD = builder.define("enable_shield_guard", true);
        ENABLE_TIMED_BLOCKS = builder.define("enable_timed_blocks", true);
        PERFECT_BLOCK_WINDOW_TICKS = builder.defineInRange("perfect_block_window_ticks", 6, 0, 200);
        COUNTER_WINDOW_TICKS = builder.defineInRange("counter_window_ticks", 20, 0, 72000);
        GUARD_RECOVERY_PER_SECOND = builder.defineInRange("guard_recovery_per_second", 20.0D, 0.0D, 10000.0D);
        DEFAULT_GUARD_MAX = builder.defineInRange("default_guard_max", 100.0D, 1.0D, 10000.0D);
        GUARD_RECOVERY_DELAY_TICKS = builder.defineInRange("guard_recovery_delay_ticks", 20, 0, 72000);
        GUARD_BREAK_TICKS = builder.defineInRange("guard_break_ticks", 35, 0, 72000);
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

    public static boolean recoveryWindowsEnabled() {
        return combatOverhaulEnabled() && ENABLE_RECOVERY_WINDOWS.get();
    }

    public static boolean pvpPostureEnabled() {
        return ENABLE_PVP_POSTURE.get();
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

    public static float bossHealthThreshold() {
        return BOSS_HEALTH_THRESHOLD.get().floatValue();
    }

    public static int perfectBlockWindowTicks() {
        return PERFECT_BLOCK_WINDOW_TICKS.get();
    }

    public static int counterWindowTicks() {
        return COUNTER_WINDOW_TICKS.get();
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
}
