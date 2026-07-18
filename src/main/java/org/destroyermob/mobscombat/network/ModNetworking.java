package org.destroyermob.mobscombat.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.destroyermob.mobscombat.client.MobsCombatClient;
import org.destroyermob.mobscombat.combat.DualWieldSystem;

public final class ModNetworking {
    private static final String NETWORK_VERSION = "5";

    private ModNetworking() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloadHandlers);
    }

    public static void sendPlayerPosture(ServerPlayer player, float current, float max) {
        PacketDistributor.sendToPlayer(player, new PlayerPosturePayload(current, max));
    }

    public static void sendCombatFeedback(ServerPlayer player, CombatFeedbackType type) {
        PacketDistributor.sendToPlayer(player, new CombatFeedbackPayload(type));
    }

    public static void sendAggressionIndicator(ServerPlayer player, int mobEntityId) {
        PacketDistributor.sendToPlayer(player, new AggressionIndicatorPayload(mobEntityId));
    }

    public static void sendCombatAttack(int targetId, boolean usingSecondaryAction, InteractionHand hand, boolean finisher) {
        PacketDistributor.sendToServer(new DualWieldAttackPayload(targetId, usingSecondaryAction, hand == InteractionHand.OFF_HAND, finisher));
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToClient(PlayerPosturePayload.TYPE, PlayerPosturePayload.STREAM_CODEC, ModNetworking::handlePlayerPosture);
        registrar.playToClient(CombatFeedbackPayload.TYPE, CombatFeedbackPayload.STREAM_CODEC, ModNetworking::handleCombatFeedback);
        registrar.playToClient(AggressionIndicatorPayload.TYPE, AggressionIndicatorPayload.STREAM_CODEC, ModNetworking::handleAggressionIndicator);
        registrar.playToServer(DualWieldAttackPayload.TYPE, DualWieldAttackPayload.STREAM_CODEC, ModNetworking::handleDualWieldAttack);
    }

    private static void handlePlayerPosture(PlayerPosturePayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> MobsCombatClient.updatePlayerPosture(payload));
        }
    }

    private static void handleCombatFeedback(CombatFeedbackPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> MobsCombatClient.showCombatFeedback(payload));
        }
    }

    private static void handleAggressionIndicator(AggressionIndicatorPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> MobsCombatClient.showAggressionIndicator(payload));
        }
    }

    private static void handleDualWieldAttack(DualWieldAttackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DualWieldSystem.handleAttack(
                        player,
                        payload.targetId(),
                        payload.usingSecondaryAction(),
                        payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                        payload.finisher()
                );
            }
        });
    }

}
