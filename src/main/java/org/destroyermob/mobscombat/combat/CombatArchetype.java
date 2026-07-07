package org.destroyermob.mobscombat.combat;

import java.util.Locale;

public enum CombatArchetype {
    NO_OP,
    BASIC_MELEE,
    RANGED,
    SWARM,
    ARMORED,
    BRUTE,
    CASTER,
    FLYING,
    BOSS,
    GENERIC_HOSTILE;

    public static CombatArchetype byName(String value, CombatArchetype fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return CombatArchetype.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
