package org.destroyermob.mobscombat.combat;

public enum StrikeTiming {
    QUICK,
    FULL,
    COUNTER;

    public static StrikeTiming fromAttackStrength(float attackStrength, boolean counterWindow) {
        if (counterWindow) {
            return COUNTER;
        }
        return attackStrength >= 0.75F ? FULL : QUICK;
    }

    public float postureMultiplier(WeaponCombatProfile profile) {
        return switch (this) {
            case QUICK -> profile.quickStrikePostureMultiplier();
            case FULL -> profile.fullStrikePostureMultiplier();
            case COUNTER -> profile.counterStrikePostureMultiplier();
        };
    }
}
