package org.destroyermob.mobscombat.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

public final class CombatStateManager {
    private static final Map<UUID, CombatState> STATES = new HashMap<>();

    private CombatStateManager() {
    }

    public static CombatState getOrCreate(LivingEntity entity) {
        return STATES.computeIfAbsent(entity.getUUID(), ignored -> new CombatState());
    }

    public static CombatState get(LivingEntity entity) {
        return STATES.get(entity.getUUID());
    }

    public static void tick(LivingEntity entity, EntityCombatProfile profile) {
        CombatState state = get(entity);
        if (state != null) {
            state.tick(entity, profile);
        }
    }

    public static void remove(LivingEntity entity) {
        STATES.remove(entity.getUUID());
    }
}
