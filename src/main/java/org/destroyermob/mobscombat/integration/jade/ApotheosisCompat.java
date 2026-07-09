package org.destroyermob.mobscombat.integration.jade;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.ModList;

final class ApotheosisCompat {
    private static final String MOD_ID = "apotheosis";
    private static final String ARMOR_SUFFIX = "/armor";
    private static final String ARMOR_TOUGHNESS_SUFFIX = "/armor_toughness";

    private ApotheosisCompat() {
    }

    static Optional<WorldTierDefense> worldTierDefense(LivingEntity entity) {
        if (!ModList.get().isLoaded(MOD_ID)) {
            return Optional.empty();
        }

        TierValue armor = findTierValue(entity.getAttribute(Attributes.ARMOR), ARMOR_SUFFIX);
        TierValue toughness = findTierValue(entity.getAttribute(Attributes.ARMOR_TOUGHNESS), ARMOR_TOUGHNESS_SUFFIX);
        if (isZero(armor.amount()) && isZero(toughness.amount())) {
            return Optional.empty();
        }

        String tier = armor.tier().isEmpty() ? toughness.tier() : armor.tier();
        return Optional.of(new WorldTierDefense(tier, armor.amount(), toughness.amount()));
    }

    private static TierValue findTierValue(AttributeInstance instance, String suffix) {
        if (instance == null) {
            return TierValue.NONE;
        }

        String tier = "";
        float amount = 0.0F;
        for (AttributeModifier modifier : instance.getModifiers()) {
            ResourceLocation id = modifier.id();
            if (!MOD_ID.equals(id.getNamespace()) || !id.getPath().endsWith(suffix)) {
                continue;
            }

            String modifierTier = id.getPath().substring(0, id.getPath().length() - suffix.length());
            if (tier.isEmpty()) {
                tier = modifierTier;
            }
            amount += (float) modifier.amount();
        }

        return new TierValue(tier, amount);
    }

    private static boolean isZero(float value) {
        return Math.abs(value) < 0.001F;
    }

    record WorldTierDefense(String tier, float armor, float toughness) {
    }

    private record TierValue(String tier, float amount) {
        private static final TierValue NONE = new TierValue("", 0.0F);
    }
}
