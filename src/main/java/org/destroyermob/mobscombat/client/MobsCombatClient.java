package org.destroyermob.mobscombat.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.destroyermob.mobscombat.network.PlayerPosturePayload;

public final class MobsCombatClient {
    private static final int SYNC_TIMEOUT_TICKS = 20;
    private static float playerPosture = 0.0F;
    private static float playerMaxPosture = 0.0F;
    private static long lastPostureSyncTick = Long.MIN_VALUE;

    private MobsCombatClient() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(MobsCombatClient::registerGuiLayers);
    }

    public static void updatePlayerPosture(PlayerPosturePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        playerPosture = payload.current();
        playerMaxPosture = payload.max();
        lastPostureSyncTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_BAR, original -> MobsCombatClient.wrapExperienceBar(original));
    }

    private static LayeredDraw.Layer wrapExperienceBar(LayeredDraw.Layer original) {
        return (GuiGraphics graphics, DeltaTracker deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !shouldRenderPostureBar()) {
                original.render(graphics, deltaTracker);
                return;
            }

            float experienceProgress = minecraft.player.experienceProgress;
            minecraft.player.experienceProgress = Mth.clamp(playerPosture / playerMaxPosture, 0.0F, 1.0F);
            try {
                original.render(graphics, deltaTracker);
            } finally {
                minecraft.player.experienceProgress = experienceProgress;
            }
        };
    }

    private static boolean shouldRenderPostureBar() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.options.hideGui || minecraft.player.isCreative() || minecraft.player.isSpectator()) {
            return false;
        }
        if (playerMaxPosture <= 0.0F || minecraft.level.getGameTime() - lastPostureSyncTick > SYNC_TIMEOUT_TICKS) {
            return false;
        }
        return playerPosture < playerMaxPosture - 0.05F;
    }
}
