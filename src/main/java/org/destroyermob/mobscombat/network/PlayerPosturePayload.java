package org.destroyermob.mobscombat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.destroyermob.mobscombat.MobsCombat;

public record PlayerPosturePayload(float current, float max) implements CustomPacketPayload {
    public static final Type<PlayerPosturePayload> TYPE = new Type<>(MobsCombat.id("player_posture"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPosturePayload> STREAM_CODEC =
            StreamCodec.ofMember(PlayerPosturePayload::write, PlayerPosturePayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(this.current);
        buffer.writeFloat(this.max);
    }

    private static PlayerPosturePayload read(RegistryFriendlyByteBuf buffer) {
        return new PlayerPosturePayload(buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public Type<PlayerPosturePayload> type() {
        return TYPE;
    }
}
