package org.destroyermob.mobscombat.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.destroyermob.mobscombat.client.MobsCombatClient;

public final class ModNetworking {
    private static final String NETWORK_VERSION = "1";

    private ModNetworking() {
    }

    public static void register(IEventBus eventBus) {
        eventBus.addListener(ModNetworking::registerPayloadHandlers);
    }

    public static void sendPlayerPosture(ServerPlayer player, float current, float max) {
        PacketDistributor.sendToPlayer(player, new PlayerPosturePayload(current, max));
    }

    private static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToClient(PlayerPosturePayload.TYPE, PlayerPosturePayload.STREAM_CODEC, ModNetworking::handlePlayerPosture);
    }

    private static void handlePlayerPosture(PlayerPosturePayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist.isClient()) {
            context.enqueueWork(() -> MobsCombatClient.updatePlayerPosture(payload));
        }
    }
}
