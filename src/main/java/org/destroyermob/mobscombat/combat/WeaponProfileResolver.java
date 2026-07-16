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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import org.destroyermob.mobscombat.integration.bettercombat.BetterCombatCompat;
import org.slf4j.Logger;

public final class WeaponProfileResolver {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static volatile ProfileTables loadedTables = ProfileTables.empty();

    private WeaponProfileResolver() {
    }

    public static ResolvedWeaponProfile resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new ResolvedWeaponProfile(WeaponCombatProfile.GENERIC, "empty", false);
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ProfileTables tables = loadedTables;
        WeaponCombatProfile exact = tables.exact().get(itemId);
        if (exact != null) {
            return new ResolvedWeaponProfile(exact, "json:" + itemId, true);
        }
        for (Map.Entry<TagKey<Item>, WeaponCombatProfile> entry : tables.tags().entrySet()) {
            if (stack.is(entry.getKey())) {
                return new ResolvedWeaponProfile(entry.getValue(), "json:#" + entry.getKey().location(), true);
            }
        }
        if (stack.is(CombatTags.Items.BLUNT) || stack.is(CombatTags.Items.MACES) || stack.is(CombatTags.Items.BETTER_ENCHANTING_MACES) || stack.is(Items.MACE)) {
            return new ResolvedWeaponProfile(WeaponCombatProfile.MACE, "fallback:mace", true);
        }
        if (stack.is(CombatTags.Items.CHOPPING) || stack.is(CombatTags.Items.BATTLE_AXES) || stack.is(CombatTags.Items.MINECRAFT_AXES) || stack.is(CombatTags.Items.BETTER_ENCHANTING_AXES) || stack.getItem() instanceof AxeItem) {
            return new ResolvedWeaponProfile(WeaponCombatProfile.AXE, "fallback:axe", true);
        }
        if (stack.is(CombatTags.Items.PIERCING) || stack.is(CombatTags.Items.DAGGERS) || stack.is(CombatTags.Items.SPEARS)) {
            return new ResolvedWeaponProfile(new WeaponCombatProfile(CombatDamageKind.PIERCE, 7.0F, 7.0F, 0.45F, 1.0F, 1.4F, 1.3F, 1.0F, true, true), "fallback:piercing", true);
        }
        if (stack.is(CombatTags.Items.SLASHING) || stack.is(CombatTags.Items.MINECRAFT_SWORDS) || stack.is(CombatTags.Items.BETTER_ENCHANTING_SWORDS) || stack.getItem() instanceof SwordItem) {
            return new ResolvedWeaponProfile(WeaponCombatProfile.SWORD, "fallback:sword", true);
        }
        ResolvedWeaponProfile betterCombatProfile = BetterCombatCompat.resolveWeaponProfile(stack);
        if (betterCombatProfile != null) {
            return betterCombatProfile;
        }
        return new ResolvedWeaponProfile(WeaponCombatProfile.GENERIC, "fallback:generic_item", false);
    }

    public static boolean isShieldLike(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.is(Items.SHIELD)
                || stack.is(CombatTags.Items.SHIELDS)
                || stack.is(CombatTags.Items.BUCKLERS)
                || stack.is(CombatTags.Items.TOWER_SHIELDS)
                || stack.is(CombatTags.Items.SPELLWARD_SHIELDS)
                || stack.is(CombatTags.Items.COMMON_SHIELD)
                || stack.is(CombatTags.Items.COMMON_SHIELDS)
                || stack.getUseAnimation() == UseAnim.BLOCK;
    }

    public static boolean isParryWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(CombatTags.Items.PARRY_WEAPONS);
    }

    public record ProfileTables(Map<ResourceLocation, WeaponCombatProfile> exact, Map<TagKey<Item>, WeaponCombatProfile> tags) {
        static ProfileTables empty() {
            return new ProfileTables(Map.of(), Map.of());
        }
    }

    public static final class ReloadListener extends SimpleJsonResourceReloadListener {
        public ReloadListener() {
            super(GSON, "mobscombat/weapon_combat_profiles");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
            Map<ResourceLocation, WeaponCombatProfile> exact = new LinkedHashMap<>();
            Map<TagKey<Item>, WeaponCombatProfile> tags = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
                try {
                    JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), "weapon combat profiles");
                    JsonObject values = GsonHelper.getAsJsonObject(root, "values");
                    for (Map.Entry<String, JsonElement> valueEntry : values.entrySet()) {
                        WeaponCombatProfile profile = WeaponCombatProfile.fromJson(GsonHelper.convertToJsonObject(valueEntry.getValue(), "weapon combat profile"));
                        String key = valueEntry.getKey();
                        if (key.startsWith("#")) {
                            tags.put(TagKey.create(Registries.ITEM, ResourceLocation.parse(key.substring(1))), profile);
                        } else {
                            exact.put(ResourceLocation.parse(key), profile);
                        }
                    }
                } catch (RuntimeException exception) {
                    LOGGER.warn("Skipping invalid weapon combat profile file {}", entry.getKey(), exception);
                }
            }
            loadedTables = new ProfileTables(
                    Collections.unmodifiableMap(new LinkedHashMap<>(exact)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(tags))
            );
            LOGGER.info("Loaded {} exact and {} tagged Mobs Combat weapon profile(s) from {} file(s)", exact.size(), tags.size(), resources.size());
        }
    }
}
