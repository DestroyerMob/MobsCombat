package org.destroyermob.mobscombat.combat;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.destroyermob.mobscombat.config.CombatConfig;

public final class CombatState {
    private float currentPosture;
    private float maxPosture;
    private float currentGuard;
    private float maxGuard;
    private int staggerTicks;
    private int staggerCooldownTicks;
    private int recoveryTicks;
    private int counterWindowTicks;
    private int recentPerfectBlockTicks;
    private int recentParryTicks;
    private int recentGuardBreakTicks;
    private int recentStealthStrikeTicks;
    private int ticksSinceLastDamaged = 72000;
    private int ticksSinceLastAttack = 72000;
    private int ticksSinceGuardPressure = 72000;
    private boolean softStaggered;
    private boolean hardStaggered;
    private int lastAttackTargetId = -1;
    private float lastAttackStrength = 1.0F;
    private int lastAttackTick = -72000;
    private int lastStealthStrikeTargetId = -1;
    private int lastStealthStrikeTick = -72000;
    private int shieldRaisedAtTick = -72000;

    public CombatState() {
        this.maxGuard = CombatConfig.defaultGuardMax();
        this.currentGuard = this.maxGuard;
    }

    public void tick(LivingEntity entity, EntityCombatProfile profile) {
        this.ticksSinceLastDamaged = incrementTimer(this.ticksSinceLastDamaged);
        this.ticksSinceLastAttack = incrementTimer(this.ticksSinceLastAttack);
        this.ticksSinceGuardPressure = incrementTimer(this.ticksSinceGuardPressure);
        this.staggerTicks = decrementTimer(this.staggerTicks);
        this.staggerCooldownTicks = decrementTimer(this.staggerCooldownTicks);
        this.recoveryTicks = decrementTimer(this.recoveryTicks);
        this.counterWindowTicks = decrementTimer(this.counterWindowTicks);
        this.recentPerfectBlockTicks = decrementTimer(this.recentPerfectBlockTicks);
        this.recentParryTicks = decrementTimer(this.recentParryTicks);
        this.recentGuardBreakTicks = decrementTimer(this.recentGuardBreakTicks);
        this.recentStealthStrikeTicks = decrementTimer(this.recentStealthStrikeTicks);

        if (this.staggerTicks <= 0) {
            this.softStaggered = false;
            this.hardStaggered = false;
        }
        if (this.ticksSinceLastDamaged > CombatConfig.postureRecoveryDelayTicks() && this.currentPosture < this.maxPosture) {
            this.currentPosture = Math.min(this.maxPosture, this.currentPosture + profile.postureRecoveryPerSecond() / 20.0F);
        }
        if (this.ticksSinceGuardPressure > CombatConfig.guardRecoveryDelayTicks() && this.currentGuard < this.maxGuard && this.recentGuardBreakTicks <= 0) {
            this.currentGuard = Math.min(this.maxGuard, this.currentGuard + CombatConfig.guardRecoveryPerTick());
        }
        if (entity.tickCount - this.lastAttackTick > 10) {
            clearAttackIntent();
        }
        if (entity.tickCount - this.lastStealthStrikeTick > 10) {
            clearStealthStrike();
        }
    }

    public void syncPostureMax(float newMaxPosture) {
        newMaxPosture = Math.max(1.0F, newMaxPosture);
        if (this.maxPosture <= 0.0F) {
            this.maxPosture = newMaxPosture;
            this.currentPosture = newMaxPosture;
            return;
        }
        if (Math.abs(this.maxPosture - newMaxPosture) < 0.01F) {
            return;
        }
        float postureRatio = this.currentPosture / this.maxPosture;
        this.maxPosture = newMaxPosture;
        this.currentPosture = Mth.clamp(newMaxPosture * postureRatio, 0.0F, newMaxPosture);
    }

    public void syncGuardMax(float newMaxGuard) {
        newMaxGuard = Math.max(1.0F, newMaxGuard);
        if (Math.abs(this.maxGuard - newMaxGuard) < 0.01F) {
            return;
        }
        float guardRatio = this.maxGuard <= 0.0F ? 1.0F : this.currentGuard / this.maxGuard;
        this.maxGuard = newMaxGuard;
        this.currentGuard = Mth.clamp(newMaxGuard * guardRatio, 0.0F, newMaxGuard);
    }

    public float currentPosture() {
        return this.currentPosture;
    }

    public float maxPosture() {
        return this.maxPosture;
    }

    public void damagePosture(float amount) {
        damagePosture(amount, 0.0F);
    }

    public void damagePosture(float amount, float minimumPosture) {
        float floor = Mth.clamp(minimumPosture, 0.0F, this.maxPosture);
        this.currentPosture = Math.max(floor, this.currentPosture - Math.max(0.0F, amount));
        this.ticksSinceLastDamaged = 0;
    }

    public void restorePostureAfterBreak() {
        this.currentPosture = Math.max(1.0F, this.maxPosture * CombatConfig.postureAfterBreakRatio());
    }

    public float currentGuard() {
        return this.currentGuard;
    }

    public float maxGuard() {
        return this.maxGuard;
    }

    public void spendGuard(float amount) {
        this.currentGuard = Math.max(0.0F, this.currentGuard - Math.max(0.0F, amount));
        this.ticksSinceGuardPressure = 0;
    }

    public void setGuardBroken() {
        this.currentGuard = 0.0F;
        this.recentGuardBreakTicks = CombatConfig.guardBreakTicks();
        this.ticksSinceGuardPressure = 0;
    }

    public boolean isGuardBroken() {
        return this.recentGuardBreakTicks > 0;
    }

    public int staggerTicks() {
        return this.staggerTicks;
    }

    public int staggerCooldownTicks() {
        return this.staggerCooldownTicks;
    }

    public int recoveryTicks() {
        return this.recoveryTicks;
    }

    public int counterWindowTicks() {
        return this.counterWindowTicks;
    }

    public int recentParryTicks() {
        return this.recentParryTicks;
    }

    public int recentStealthStrikeTicks() {
        return this.recentStealthStrikeTicks;
    }

    public boolean isStaggered() {
        return this.staggerTicks > 0 && (this.softStaggered || this.hardStaggered);
    }

    public boolean isHardStaggered() {
        return this.staggerTicks > 0 && this.hardStaggered;
    }

    public void stagger(int durationTicks, int cooldownTicks, boolean hard) {
        this.staggerTicks = Math.max(this.staggerTicks, durationTicks);
        this.staggerCooldownTicks = Math.max(this.staggerCooldownTicks, cooldownTicks);
        this.softStaggered = true;
        this.hardStaggered = hard;
    }

    public void markRecovery(int ticks) {
        this.recoveryTicks = Math.max(this.recoveryTicks, ticks);
        this.ticksSinceLastAttack = 0;
    }

    public void markPerfectBlock() {
        this.recentPerfectBlockTicks = 10;
        this.counterWindowTicks = Math.max(this.counterWindowTicks, CombatConfig.counterWindowTicks());
    }

    public void markParry() {
        this.recentParryTicks = 10;
        this.counterWindowTicks = Math.max(this.counterWindowTicks, CombatConfig.counterWindowTicks());
    }

    public void recordAttackIntent(int targetId, float attackStrength, int tick) {
        this.lastAttackTargetId = targetId;
        this.lastAttackStrength = Mth.clamp(attackStrength, 0.0F, 1.5F);
        this.lastAttackTick = tick;
    }

    public float consumeAttackStrengthFor(int targetId, int tick) {
        if (this.lastAttackTargetId == targetId && tick - this.lastAttackTick <= 10) {
            float strength = this.lastAttackStrength;
            clearAttackIntent();
            return strength;
        }
        return 1.0F;
    }

    public boolean consumeParryIntentFor(int targetId, int tick, int windowTicks) {
        if (this.lastAttackTargetId == targetId && tick - this.lastAttackTick <= windowTicks) {
            clearAttackIntent();
            return true;
        }
        return false;
    }

    public void markStealthStrike(int targetId, int tick) {
        this.lastStealthStrikeTargetId = targetId;
        this.lastStealthStrikeTick = tick;
        this.recentStealthStrikeTicks = 10;
    }

    public boolean consumeStealthStrikeFor(int targetId, int tick) {
        if (this.lastStealthStrikeTargetId == targetId && tick - this.lastStealthStrikeTick <= 10) {
            clearStealthStrike();
            return true;
        }
        return false;
    }

    public void markShieldRaised(int tick) {
        this.shieldRaisedAtTick = tick;
    }

    public boolean isWithinPerfectBlockWindow(int tick) {
        return tick - this.shieldRaisedAtTick <= CombatConfig.perfectBlockWindowTicks();
    }

    private void clearAttackIntent() {
        this.lastAttackTargetId = -1;
        this.lastAttackStrength = 1.0F;
    }

    private void clearStealthStrike() {
        this.lastStealthStrikeTargetId = -1;
        this.lastStealthStrikeTick = -72000;
    }

    private static int decrementTimer(int value) {
        return value > 0 ? value - 1 : 0;
    }

    private static int incrementTimer(int value) {
        return value >= 72000 ? 72000 : value + 1;
    }
}
