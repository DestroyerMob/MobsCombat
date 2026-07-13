package org.destroyermob.mobscombat.combat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;

public final class ParrySystem {
    private ParrySystem() {
    }

    public static boolean openCounterFromBlock(Player player, ItemStack blockingItem, boolean sendCounterFeedback) {
        if (!CombatConfig.parryEnabled()
                || player.isSpectator()
                || !player.isAlive()
                || blockingItem.isEmpty()
                || !WeaponProfileResolver.isShieldLike(blockingItem)) {
            return false;
        }

        CombatState state = CombatStateManager.getOrCreate(player);
        if (state.isStaggered()) {
            return false;
        }

        state.markParry();
        if (sendCounterFeedback && player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendCombatFeedback(serverPlayer, CombatFeedbackType.PARRY);
        }
        return true;
    }
}
