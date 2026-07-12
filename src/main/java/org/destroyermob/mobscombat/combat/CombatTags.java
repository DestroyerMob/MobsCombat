package org.destroyermob.mobscombat.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.destroyermob.mobscombat.MobsCombat;

public final class CombatTags {
    private CombatTags() {
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> NO_COMBAT_PROFILE = entityType("combat/no_combat_profile");
        public static final TagKey<EntityType<?>> NO_POSTURE = entityType("combat/no_posture");
        public static final TagKey<EntityType<?>> NO_HARD_STAGGER = entityType("combat/no_hard_stagger");
        public static final TagKey<EntityType<?>> NO_KNOCKBACK_OVERRIDE = entityType("combat/no_knockback_override");
        public static final TagKey<EntityType<?>> BOSS_LIKE = entityType("combat/boss_like");
        public static final TagKey<EntityType<?>> COMMON_BOSSES = commonEntityType("bosses");
        public static final TagKey<EntityType<?>> REDUCED_POSTURE_EFFECTS = entityType("combat/reduced_posture_effects");
        public static final TagKey<EntityType<?>> SWARM = entityType("combat/swarm");
        public static final TagKey<EntityType<?>> ARMORED = entityType("combat/armored");
        public static final TagKey<EntityType<?>> BRUTE = entityType("combat/brute");
        public static final TagKey<EntityType<?>> CASTER = entityType("combat/caster");
        public static final TagKey<EntityType<?>> FLYING_HOSTILE = entityType("combat/flying_hostile");
        public static final TagKey<EntityType<?>> CONSTRUCT = entityType("combat/construct");
        public static final TagKey<EntityType<?>> BEAST = entityType("combat/beast");
        public static final TagKey<EntityType<?>> UNDEAD = entityType("combat/undead");
        public static final TagKey<EntityType<?>> ARTHROPOD = entityType("combat/arthropod");

        private EntityTypes() {
        }
    }

    public static final class Items {
        public static final TagKey<Item> SLASHING = item("weapons/slashing");
        public static final TagKey<Item> PIERCING = item("weapons/piercing");
        public static final TagKey<Item> BLUNT = item("weapons/blunt");
        public static final TagKey<Item> CHOPPING = item("weapons/chopping");
        public static final TagKey<Item> DAGGERS = item("weapons/daggers");
        public static final TagKey<Item> SPEARS = item("weapons/spears");
        public static final TagKey<Item> MACES = item("weapons/maces");
        public static final TagKey<Item> MACHETES = item("weapons/machetes");
        public static final TagKey<Item> GREATSWORDS = item("weapons/greatswords");
        public static final TagKey<Item> BATTLE_AXES = item("weapons/battle_axes");
        public static final TagKey<Item> TWO_HANDED = item("weapons/two_handed");
        public static final TagKey<Item> BUCKLERS = item("shields/bucklers");
        public static final TagKey<Item> TOWER_SHIELDS = item("shields/tower_shields");
        public static final TagKey<Item> SPELLWARD_SHIELDS = item("shields/spellward_shields");
        public static final TagKey<Item> SHIELDS = item("shields");
        public static final TagKey<Item> COMMON_SHIELD = commonItem("tools/shield");
        public static final TagKey<Item> COMMON_SHIELDS = commonItem("tools/shields");
        public static final TagKey<Item> BETTER_ENCHANTING_SWORDS = externalItem("betterenchanting", "weapons/swords");
        public static final TagKey<Item> BETTER_ENCHANTING_MACES = externalItem("betterenchanting", "weapons/maces");
        public static final TagKey<Item> BETTER_ENCHANTING_AXES = externalItem("betterenchanting", "tools/axes");
        public static final TagKey<Item> MINECRAFT_SWORDS = vanillaItem("swords");
        public static final TagKey<Item> MINECRAFT_AXES = vanillaItem("axes");

        private Items() {
        }
    }

    private static TagKey<EntityType<?>> entityType(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, MobsCombat.id(path));
    }

    private static TagKey<EntityType<?>> commonEntityType(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, MobsCombat.id(path));
    }

    private static TagKey<Item> commonItem(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> vanillaItem(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.withDefaultNamespace(path));
    }

    private static TagKey<Item> externalItem(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
