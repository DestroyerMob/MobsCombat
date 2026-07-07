package org.destroyermob.mobscombat.network;

import java.util.Locale;

public enum CombatFeedbackType {
    PERFECT_BLOCK,
    GUARD_BREAK,
    PARRY,
    STEALTH_STRIKE;

    public String translationKey() {
        return "mobscombat.feedback." + this.name().toLowerCase(Locale.ROOT);
    }
}
