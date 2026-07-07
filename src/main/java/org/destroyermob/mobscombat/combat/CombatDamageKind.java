package org.destroyermob.mobscombat.combat;

import java.util.Locale;

public enum CombatDamageKind {
    SLASH,
    PIERCE,
    BLUNT,
    CHOP,
    MAGIC,
    FIRE,
    FROST,
    LIGHTNING,
    POISON,
    BLEED,
    HOLY,
    VOID,
    GENERIC;

    public static CombatDamageKind byName(String value, CombatDamageKind fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return CombatDamageKind.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
