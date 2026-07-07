package org.destroyermob.mobscombat;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.destroyermob.mobscombat.combat.CombatDebugCommand;
import org.destroyermob.mobscombat.combat.CombatEvents;
import org.destroyermob.mobscombat.combat.CombatProfileResolver;
import org.destroyermob.mobscombat.combat.WeaponProfileResolver;
import org.destroyermob.mobscombat.config.CombatConfig;

@Mod(MobsCombat.MOD_ID)
public final class MobsCombat {
    public static final String MOD_ID = "mobscombat";

    public MobsCombat(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CombatConfig.SPEC);

        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(CombatEvents::recordAttackIntent);
        NeoForge.EVENT_BUS.addListener(CombatEvents::reduceStaggeredOutgoingDamage);
        NeoForge.EVENT_BUS.addListener(CombatEvents::afterLivingDamage);
        NeoForge.EVENT_BUS.addListener(CombatEvents::onShieldBlock);
        NeoForge.EVENT_BUS.addListener(CombatEvents::onUseItemStart);
        NeoForge.EVENT_BUS.addListener(CombatEvents::onEntityTick);
        NeoForge.EVENT_BUS.addListener(CombatEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(CombatEvents::onEntityLeaveLevel);
        NeoForge.EVENT_BUS.addListener(CombatDebugCommand::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CombatProfileResolver.ReloadListener());
        event.addListener(new WeaponProfileResolver.ReloadListener());
    }
}
