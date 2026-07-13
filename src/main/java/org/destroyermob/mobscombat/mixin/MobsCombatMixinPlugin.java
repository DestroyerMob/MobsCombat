package org.destroyermob.mobscombat.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class MobsCombatMixinPlugin implements IMixinConfigPlugin {
    private static final String PLAYER_ATTACK_HAND_MIXIN =
            "org.destroyermob.mobscombat.mixin.PlayerAttackHandMixin";
    private static final String LIVING_ENTITY_WEAPON_ITEM_MIXIN =
            "org.destroyermob.mobscombat.mixin.LivingEntityWeaponItemMixin";
    private static final String BETTER_COMBAT_MOD_ID = "bettercombat";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (PLAYER_ATTACK_HAND_MIXIN.equals(mixinClassName)
                || LIVING_ENTITY_WEAPON_ITEM_MIXIN.equals(mixinClassName)) {
            return !isModPresent(BETTER_COMBAT_MOD_ID);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean isModPresent(String modId) {
        try {
            LoadingModList modList = LoadingModList.get();
            return modList != null && modList.getModFileById(modId) != null;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
