package org.destroyermob.mobscombat.integration.bettercombat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.destroyermob.mobscombat.combat.ResolvedWeaponProfile;
import org.slf4j.Logger;

/** Optional entry point which never resolves Better Combat classes unless the mod is loaded. */
public final class BetterCombatCompat {
    public static final String MOD_ID = "bettercombat";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean bridgeFailed;

    private BetterCombatCompat() {
    }

    public static boolean isLoaded() {
        try {
            return ModList.get().isLoaded(MOD_ID);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static ResolvedWeaponProfile resolveWeaponProfile(ItemStack stack) {
        if (!isLoaded() || bridgeFailed || stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            return BetterCombatBridge.resolveWeaponProfile(stack);
        } catch (LinkageError | RuntimeException exception) {
            bridgeFailed = true;
            LOGGER.warn("Better Combat weapon-profile integration failed; Mobs Combat will use tags and vanilla fallbacks.", exception);
            return null;
        }
    }
}
