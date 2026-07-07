package org.destroyermob.mobscombat.combat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import org.destroyermob.mobscombat.config.CombatConfig;
import org.slf4j.Logger;

public final class CombatProfileResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static volatile ProfileTables loadedTables = ProfileTables.empty();

    private CombatProfileResolver() {
    }

    public static ResolvedEntityProfile resolve(LivingEntity entity) {
        if (!CombatConfig.combatOverhaulEnabled()) {
            return new ResolvedEntityProfile(EntityCombatProfile.DISABLED, "config_disabled");
        }
        EntityType<?> type = entity.getType();
        if (type.is(CombatTags.EntityTypes.NO_COMBAT_PROFILE) || type.is(CombatTags.EntityTypes.NO_POSTURE)) {
            return new ResolvedEntityProfile(EntityCombatProfile.DISABLED, "disabled_tag");
        }
        if (entity instanceof Player && !CombatConfig.playerPostureEnabled()) {
            return new ResolvedEntityProfile(EntityCombatProfile.DISABLED, "player_posture_disabled");
        }
        if (entity instanceof ArmorStand) {
            return new ResolvedEntityProfile(EntityCombatProfile.DISABLED, "armor_stand");
        }

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        ProfileTables tables = loadedTables;
        EntityCombatProfile exact = tables.exact().get(typeId);
        if (exact != null) {
            return new ResolvedEntityProfile(adjustForSafetyTags(entity, exact), "json:" + typeId);
        }
        for (Map.Entry<TagKey<EntityType<?>>, EntityCombatProfile> entry : tables.tags().entrySet()) {
            if (type.is(entry.getKey())) {
                return new ResolvedEntityProfile(adjustForSafetyTags(entity, entry.getValue()), "json:#" + entry.getKey().location());
            }
        }
        return infer(entity);
    }

    private static ResolvedEntityProfile infer(LivingEntity entity) {
        if (entity instanceof Player) {
            return new ResolvedEntityProfile(playerProfile(), "inferred:player");
        }
        if (isBossLike(entity)) {
            return new ResolvedEntityProfile(EntityCombatProfile.BOSS_LIKE, "inferred:boss_like");
        }
        if (entity.getType().is(CombatTags.EntityTypes.SWARM) || (entity.getBbWidth() <= 0.7F && maxHealth(entity) <= 12.0F)) {
            return new ResolvedEntityProfile(profile(CombatArchetype.SWARM, 0.7F, 0.0F, 1.4F, 8, 40, 0.35F, 0.55F, true, 1.0F, 1.25F, 8), "inferred:swarm");
        }
        if (entity.getType().is(CombatTags.EntityTypes.BRUTE) || armor(entity) >= 8.0F || knockbackResistance(entity) >= 0.35F) {
            return new ResolvedEntityProfile(profile(CombatArchetype.BRUTE, 1.35F, 8.0F, 1.5F, 10, 70, 0.55F, 0.6F, true, 1.25F, 1.15F, 8), "inferred:brute");
        }
        if (entity.getType().is(CombatTags.EntityTypes.CASTER)) {
            return new ResolvedEntityProfile(profile(CombatArchetype.CASTER, 0.85F, 0.0F, 1.2F, 8, 55, 0.45F, 0.6F, true, 0.85F, 1.3F, 10), "inferred:caster");
        }
        if (entity instanceof FlyingMob || entity.getType().is(CombatTags.EntityTypes.FLYING_HOSTILE)) {
            return new ResolvedEntityProfile(profile(CombatArchetype.FLYING, 0.8F, 0.0F, 1.4F, 7, 55, 0.5F, 0.7F, false, 0.9F, 1.2F, 8), "inferred:flying");
        }
        if (isHostile(entity)) {
            return new ResolvedEntityProfile(EntityCombatProfile.GENERIC_HOSTILE, "inferred:generic_hostile");
        }
        return new ResolvedEntityProfile(EntityCombatProfile.DISABLED, "inferred:non_hostile");
    }

    private static EntityCombatProfile adjustForSafetyTags(LivingEntity entity, EntityCombatProfile profile) {
        EntityCombatProfile adjusted = profile;
        if (isBossLike(entity) && !CombatConfig.bossHardStaggerEnabled()) {
            adjusted = adjusted.asBossLike();
        }
        if (entity.getType().is(CombatTags.EntityTypes.NO_HARD_STAGGER)) {
            adjusted = new EntityCombatProfile(
                    adjusted.enabled(),
                    adjusted.archetype(),
                    adjusted.postureMultiplier(),
                    adjusted.flatPostureBonus(),
                    adjusted.postureRecoveryPerSecond(),
                    adjusted.staggerDurationTicks(),
                    adjusted.staggerCooldownTicks(),
                    adjusted.outgoingDamageMultiplierWhileStaggered(),
                    adjusted.movementSpeedMultiplierWhileStaggered(),
                    false,
                    false,
                    adjusted.knockbackOverrideAllowed(),
                    adjusted.bossLike(),
                    adjusted.guardDamageMultiplier(),
                    adjusted.postureDamageTakenMultipliers(),
                    adjusted.healthDamageTakenMultipliers(),
                    adjusted.recoveryWindowPostureMultiplier(),
                    adjusted.recoveryWindowTicks()
            );
        }
        return adjusted;
    }

    private static boolean isHostile(LivingEntity entity) {
        if (entity.getType().getCategory() == MobCategory.MONSTER || entity instanceof Enemy) {
            return true;
        }
        if (entity instanceof Animal || entity instanceof AbstractVillager) {
            return false;
        }
        if (entity instanceof Mob mob && mob.getTarget() != null) {
            return true;
        }
        return attackDamage(entity) > 0.0F && !(entity instanceof Player);
    }

    private static boolean isBossLike(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type.is(CombatTags.EntityTypes.BOSS_LIKE)
                || type.is(CombatTags.EntityTypes.COMMON_BOSSES)
                || maxHealth(entity) >= CombatConfig.bossHealthThreshold()
                || knockbackResistance(entity) >= 0.75F;
    }

    private static EntityCombatProfile playerProfile() {
        return new EntityCombatProfile(
                CombatConfig.playerPostureEnabled(),
                CombatArchetype.BASIC_MELEE,
                1.0F,
                0.0F,
                2.0F,
                4,
                80,
                0.55F,
                0.65F,
                false,
                false,
                false,
                false,
                1.0F,
                Map.of(),
                Map.of(),
                1.05F,
                4
        );
    }

    private static EntityCombatProfile profile(
            CombatArchetype archetype,
            float postureMultiplier,
            float flatPostureBonus,
            float postureRecoveryPerSecond,
            int staggerDurationTicks,
            int staggerCooldownTicks,
            float outgoingDamageMultiplierWhileStaggered,
            float movementSpeedMultiplierWhileStaggered,
            boolean hardStaggerAllowed,
            float guardDamageMultiplier,
            float recoveryWindowPostureMultiplier,
            int recoveryWindowTicks
    ) {
        return new EntityCombatProfile(
                true,
                archetype,
                postureMultiplier,
                flatPostureBonus,
                postureRecoveryPerSecond,
                staggerDurationTicks,
                staggerCooldownTicks,
                outgoingDamageMultiplierWhileStaggered,
                movementSpeedMultiplierWhileStaggered,
                hardStaggerAllowed,
                false,
                hardStaggerAllowed,
                false,
                guardDamageMultiplier,
                Map.of(),
                Map.of(),
                recoveryWindowPostureMultiplier,
                recoveryWindowTicks
        );
    }

    private static float maxHealth(LivingEntity entity) {
        return attributeValue(entity, Attributes.MAX_HEALTH, entity.getMaxHealth());
    }

    private static float armor(LivingEntity entity) {
        return attributeValue(entity, Attributes.ARMOR, 0.0F);
    }

    private static float knockbackResistance(LivingEntity entity) {
        return attributeValue(entity, Attributes.KNOCKBACK_RESISTANCE, 0.0F);
    }

    private static float attackDamage(LivingEntity entity) {
        return attributeValue(entity, Attributes.ATTACK_DAMAGE, 0.0F);
    }

    private static float attributeValue(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, float fallback) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? fallback : (float) instance.getValue();
    }

    public record ProfileTables(Map<ResourceLocation, EntityCombatProfile> exact, Map<TagKey<EntityType<?>>, EntityCombatProfile> tags) {
        static ProfileTables empty() {
            return new ProfileTables(Map.of(), Map.of());
        }
    }

    public static final class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "mobscombat/entity_combat_profiles");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, EntityCombatProfile> exact = new LinkedHashMap<>();
            Map<TagKey<EntityType<?>>, EntityCombatProfile> tags = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
                try {
                    JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), "entity combat profiles");
                    JsonObject values = GsonHelper.getAsJsonObject(root, "values");
                    for (Map.Entry<String, JsonElement> valueEntry : values.entrySet()) {
                        EntityCombatProfile profile = EntityCombatProfile.fromJson(GsonHelper.convertToJsonObject(valueEntry.getValue(), "entity combat profile"));
                        String key = valueEntry.getKey();
                        if (key.startsWith("#")) {
                            tags.put(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(key.substring(1))), profile);
                        } else {
                            exact.put(ResourceLocation.parse(key), profile);
                        }
                    }
                } catch (RuntimeException exception) {
                    LOGGER.warn("Skipping invalid entity combat profile file {}", entry.getKey(), exception);
                }
            }
            loadedTables = new ProfileTables(
                    Collections.unmodifiableMap(new LinkedHashMap<>(exact)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(tags))
            );
            LOGGER.info("Loaded {} exact and {} tagged Mobs Combat entity profile(s) from {} file(s)", exact.size(), tags.size(), resources.size());
        }
    }
}
