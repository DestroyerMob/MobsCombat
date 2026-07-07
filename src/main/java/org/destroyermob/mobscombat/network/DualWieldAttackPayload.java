package org.destroyermob.mobscombat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.destroyermob.mobscombat.MobsCombat;

public record DualWieldAttackPayload(int targetId, boolean usingSecondaryAction, boolean offhand, boolean finisher) implements CustomPacketPayload {
    public static final Type<DualWieldAttackPayload> TYPE = new Type<>(MobsCombat.id("dual_wield_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DualWieldAttackPayload> STREAM_CODEC =
            StreamCodec.ofMember(DualWieldAttackPayload::write, DualWieldAttackPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.targetId);
        buffer.writeBoolean(this.usingSecondaryAction);
        buffer.writeBoolean(this.offhand);
        buffer.writeBoolean(this.finisher);
    }

    private static DualWieldAttackPayload read(RegistryFriendlyByteBuf buffer) {
        return new DualWieldAttackPayload(buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public Type<DualWieldAttackPayload> type() {
        return TYPE;
    }
}
