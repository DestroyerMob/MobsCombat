package org.destroyermob.mobscombat.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.destroyermob.mobscombat.MobsCombat;
import org.destroyermob.mobscombat.client.compat.PunchyCompat;
import org.destroyermob.mobscombat.combat.DualWieldSystem;
import org.destroyermob.mobscombat.network.CombatFeedbackPayload;
import org.destroyermob.mobscombat.network.CombatFeedbackType;
import org.destroyermob.mobscombat.network.ModNetworking;
import org.destroyermob.mobscombat.network.PlayerPosturePayload;

public final class MobsCombatClient {
    private static final int SYNC_TIMEOUT_TICKS = 20;
    private static final int POSTURE_BAR_WIDTH = 182;
    private static final int POSTURE_BAR_HEIGHT = 5;
    private static final int POSTURE_TEXT_COLOR = 0xF4E38A;
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation POSTURE_BAR_PROGRESS_SPRITE = MobsCombat.id("hud/posture_bar_progress");
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

    public static void showCombatFeedback(CombatFeedbackPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.displayClientMessage(Component.translatable(payload.feedbackType().translationKey()).withStyle(feedbackColor(payload.feedbackType())), true);
    }

    public static boolean tryStartDualWieldAttack() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.player.isSpectator()
                || !DualWieldSystem.shouldUseCustomAttack(minecraft.player)) {
            return false;
        }
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            return false;
        }
        if (minecraft.hitResult instanceof EntityHitResult hitResult && !(hitResult.getEntity() instanceof LivingEntity)) {
            return false;
        }
        if (DualWieldSystem.shouldWaitForDualWieldCooldown(minecraft.player)) {
            return true;
        }

        InteractionHand attackHand = DualWieldSystem.claimClientAttackHand(minecraft.player);
        minecraft.player.swing(attackHand);
        boolean finisher = PunchyCompat.recordAttack(minecraft, attackHand);
        if (minecraft.hitResult instanceof EntityHitResult hitResult) {
            ModNetworking.sendDualWieldAttack(hitResult.getEntity().getId(), minecraft.player.isShiftKeyDown(), attackHand, finisher);
        }
        minecraft.player.resetAttackStrengthTicker();
        return true;
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_BAR, original -> MobsCombatClient.wrapExperienceBar(original));
        event.wrapLayer(VanillaGuiLayers.EXPERIENCE_LEVEL, original -> MobsCombatClient.wrapExperienceLevel(original));
    }

    private static LayeredDraw.Layer wrapExperienceBar(LayeredDraw.Layer original) {
        return (GuiGraphics graphics, DeltaTracker deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !shouldRenderPostureBar()) {
                original.render(graphics, deltaTracker);
                return;
            }

            int x = graphics.guiWidth() / 2 - POSTURE_BAR_WIDTH / 2;
            int y = graphics.guiHeight() - 32 + 3;
            int progressWidth = (int) (Mth.clamp(playerPosture / playerMaxPosture, 0.0F, 1.0F) * (POSTURE_BAR_WIDTH + 1));
            graphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, x, y, POSTURE_BAR_WIDTH, POSTURE_BAR_HEIGHT);
            if (progressWidth > 0) {
                graphics.blitSprite(
                        POSTURE_BAR_PROGRESS_SPRITE,
                        POSTURE_BAR_WIDTH,
                        POSTURE_BAR_HEIGHT,
                        0,
                        0,
                        x,
                        y,
                        progressWidth,
                        POSTURE_BAR_HEIGHT
                );
            }
        };
    }

    private static LayeredDraw.Layer wrapExperienceLevel(LayeredDraw.Layer original) {
        return (GuiGraphics graphics, DeltaTracker deltaTracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || !shouldRenderPostureBar()) {
                original.render(graphics, deltaTracker);
                return;
            }

            String text = Math.round(playerPosture) + "/" + Math.round(playerMaxPosture);
            Font font = minecraft.font;
            int x = (graphics.guiWidth() - font.width(text)) / 2;
            int y = graphics.guiHeight() - 31 - 4;
            drawOutlinedString(graphics, font, text, x, y, POSTURE_TEXT_COLOR);
        };
    }

    private static void drawOutlinedString(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x + 1, y, 0, false);
        graphics.drawString(font, text, x - 1, y, 0, false);
        graphics.drawString(font, text, x, y + 1, 0, false);
        graphics.drawString(font, text, x, y - 1, 0, false);
        graphics.drawString(font, text, x, y, color, false);
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

    private static ChatFormatting feedbackColor(CombatFeedbackType type) {
        return switch (type) {
            case PERFECT_BLOCK -> ChatFormatting.AQUA;
            case GUARD_BREAK -> ChatFormatting.RED;
            case PARRY -> ChatFormatting.GOLD;
            case STEALTH_STRIKE -> ChatFormatting.DARK_GREEN;
        };
    }
}
