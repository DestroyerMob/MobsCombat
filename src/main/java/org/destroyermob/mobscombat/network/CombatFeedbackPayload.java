package org.destroyermob.mobscombat.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.destroyermob.mobscombat.MobsCombat;

public record CombatFeedbackPayload(CombatFeedbackType feedbackType) implements CustomPacketPayload {
    public static final Type<CombatFeedbackPayload> TYPE = new Type<>(MobsCombat.id("combat_feedback"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CombatFeedbackPayload> STREAM_CODEC =
            StreamCodec.ofMember(CombatFeedbackPayload::write, CombatFeedbackPayload::read);

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(this.feedbackType);
    }

    private static CombatFeedbackPayload read(RegistryFriendlyByteBuf buffer) {
        return new CombatFeedbackPayload(buffer.readEnum(CombatFeedbackType.class));
    }

    @Override
    public Type<CombatFeedbackPayload> type() {
        return TYPE;
    }
}
