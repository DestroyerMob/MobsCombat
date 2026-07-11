package org.destroyermob.mobscombat.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public final class MobsCombatKeyMappings {
    public static final String CATEGORY = "key.categories.mobscombat";
    public static final KeyMapping PARRY = new KeyMapping(
            "key.mobscombat.parry",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            CATEGORY);

    private MobsCombatKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(PARRY);
    }
}
