package org.destroyermob.mobscombat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.destroyermob.mobscombat.MobsCombat;

public record AggressionIndicatorPayload(int mobEntityId) implements CustomPacketPayload {
    public static final Type<AggressionIndicatorPayload> TYPE = new Type<>(MobsCombat.id("aggression_indicator"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AggressionIndicatorPayload> STREAM_CODEC =
            StreamCodec.ofMember(AggressionIndicatorPayload::write, AggressionIndicatorPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.mobEntityId);
    }

    private static AggressionIndicatorPayload read(RegistryFriendlyByteBuf buffer) {
        return new AggressionIndicatorPayload(buffer.readVarInt());
    }

    @Override
    public Type<AggressionIndicatorPayload> type() {
        return TYPE;
    }
}
