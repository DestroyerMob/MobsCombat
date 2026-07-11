package org.destroyermob.mobscombat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.destroyermob.mobscombat.MobsCombat;

public record ParryStancePayload() implements CustomPacketPayload {
    public static final ParryStancePayload INSTANCE = new ParryStancePayload();
    public static final Type<ParryStancePayload> TYPE = new Type<>(MobsCombat.id("parry_stance"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParryStancePayload> STREAM_CODEC =
            StreamCodec.ofMember(ParryStancePayload::write, ParryStancePayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
    }

    private static ParryStancePayload read(RegistryFriendlyByteBuf buffer) {
        return INSTANCE;
    }

    @Override
    public Type<ParryStancePayload> type() {
        return TYPE;
    }
}
