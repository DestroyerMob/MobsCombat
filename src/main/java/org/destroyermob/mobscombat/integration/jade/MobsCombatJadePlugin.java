package org.destroyermob.mobscombat.integration.jade;

import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.destroyermob.mobscombat.MobsCombat;
import org.destroyermob.mobscombat.combat.CombatProfileResolver;
import org.destroyermob.mobscombat.combat.CombatState;
import org.destroyermob.mobscombat.combat.CombatStateManager;
import org.destroyermob.mobscombat.combat.EntityCombatProfile;
import org.destroyermob.mobscombat.combat.PostureSystem;
import org.destroyermob.mobscombat.combat.ResolvedEntityProfile;
import org.destroyermob.mobscombat.combat.WeaponProfileResolver;
import org.destroyermob.mobscombat.config.CombatConfig;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(MobsCombat.MOD_ID)
public final class MobsCombatJadePlugin implements IWailaPlugin {
    private static final CombatEntityProvider COMBAT_ENTITY_PROVIDER = new CombatEntityProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(COMBAT_ENTITY_PROVIDER, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(COMBAT_ENTITY_PROVIDER, LivingEntity.class);
    }

    private static final class CombatEntityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
        private static final ResourceLocation UID = MobsCombat.id("entity_combat");
        private static final String DATA_KEY = MobsCombat.MOD_ID;

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof LivingEntity living)) {
                return;
            }

            ResolvedEntityProfile resolved = CombatProfileResolver.resolve(living);
            EntityCombatProfile profile = resolved.profile();
            CombatState state = CombatStateManager.get(living);
            CompoundTag combat = new CompoundTag();

            if (CombatConfig.postureEnabled() && profile.enabled()) {
                float maxPosture = state == null || state.maxPosture() <= 0.0F ? PostureSystem.maxPosture(living, profile) : state.maxPosture();
                combat.putBoolean("has_posture", true);
                combat.putFloat("posture_current", state == null ? maxPosture : state.currentPosture());
                combat.putFloat("posture_max", maxPosture);
                combat.putString("archetype", profile.archetype().name().toLowerCase(Locale.ROOT));
                combat.putString("profile_source", resolved.source());
            }

            if (CombatConfig.shieldGuardEnabled() && shouldShowGuard(living, state)) {
                combat.putBoolean("has_guard", true);
                combat.putFloat("guard_current", state.currentGuard());
                combat.putFloat("guard_max", state.maxGuard());
            }

            if (state != null) {
                combat.putInt("stagger_ticks", state.staggerTicks());
                combat.putBoolean("hard_stagger", state.isHardStaggered());
                combat.putInt("stagger_cooldown_ticks", state.staggerCooldownTicks());
                combat.putInt("recovery_ticks", state.recoveryTicks());
                combat.putInt("counter_window_ticks", state.counterWindowTicks());
                combat.putInt("parry_ticks", state.recentParryTicks());
                combat.putInt("stealth_strike_ticks", state.recentStealthStrikeTicks());
                combat.putBoolean("guard_broken", state.isGuardBroken());
            }

            double armor = living.getAttributeValue(Attributes.ARMOR);
            double toughness = living.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            if (armor > 0.0D || toughness > 0.0D) {
                combat.putBoolean("has_defense", true);
                combat.putFloat("armor", (float) armor);
                combat.putFloat("armor_toughness", (float) toughness);
            }

            ApotheosisCompat.worldTierDefense(living).ifPresent(worldTier -> {
                combat.putBoolean("has_world_tier_defense", true);
                combat.putString("world_tier", worldTier.tier());
                combat.putFloat("world_tier_armor", worldTier.armor());
                combat.putFloat("world_tier_toughness", worldTier.toughness());
            });

            if (!combat.isEmpty()) {
                tag.put(DATA_KEY, combat);
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag serverData = accessor.getServerData();
            if (!serverData.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
                return;
            }
            CompoundTag combat = serverData.getCompound(DATA_KEY);

            if (combat.getBoolean("has_posture")) {
                float current = combat.getFloat("posture_current");
                float max = combat.getFloat("posture_max");
                tooltip.add(Component.translatable(
                        "jade.mobscombat.posture",
                        Component.literal(format(current)),
                        Component.literal(format(max)),
                        Math.round(percent(current, max))
                ).withStyle(ratioColor(current, max)));
            }

            if (combat.getBoolean("has_guard")) {
                float current = combat.getFloat("guard_current");
                float max = combat.getFloat("guard_max");
                tooltip.add(Component.translatable(
                        "jade.mobscombat.guard",
                        Component.literal(format(current)),
                        Component.literal(format(max)),
                        Math.round(percent(current, max))
                ).withStyle(ratioColor(current, max)));
            }

            if (combat.getBoolean("has_defense")) {
                float armor = combat.getFloat("armor");
                float toughness = combat.getFloat("armor_toughness");
                if (toughness > 0.0F) {
                    tooltip.add(Component.translatable(
                            "jade.mobscombat.defense_with_toughness",
                            Component.literal(formatCompact(armor)),
                            Component.literal(formatCompact(toughness))
                    ).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable(
                            "jade.mobscombat.defense",
                            Component.literal(formatCompact(armor))
                    ).withStyle(ChatFormatting.GRAY));
                }
            }

            if (combat.getBoolean("has_world_tier_defense")) {
                float armor = combat.getFloat("world_tier_armor");
                float toughness = combat.getFloat("world_tier_toughness");
                Component tier = Component.literal(displayTier(combat.getString("world_tier")));
                if (toughness > 0.0F) {
                    tooltip.add(Component.translatable(
                            "jade.mobscombat.world_tier_defense_with_toughness",
                            tier,
                            Component.literal(formatSignedCompact(armor)),
                            Component.literal(formatSignedCompact(toughness))
                    ).withStyle(ChatFormatting.LIGHT_PURPLE));
                } else {
                    tooltip.add(Component.translatable(
                            "jade.mobscombat.world_tier_defense",
                            tier,
                            Component.literal(formatSignedCompact(armor))
                    ).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }

            addStatusLines(tooltip, combat);

            if (accessor.showDetails() && combat.contains("archetype")) {
                tooltip.add(Component.translatable(
                        "jade.mobscombat.archetype",
                        Component.translatable("mobscombat.archetype." + combat.getString("archetype"))
                ).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable(
                        "jade.mobscombat.profile_source",
                        Component.literal(combat.getString("profile_source"))
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        private static boolean shouldShowGuard(LivingEntity living, CombatState state) {
            if (state == null || !(living instanceof Player)) {
                return false;
            }
            return state.currentGuard() < state.maxGuard()
                    || state.isGuardBroken()
                    || state.counterWindowTicks() > 0
                    || WeaponProfileResolver.isShieldLike(living.getUseItem());
        }

        private static void addStatusLines(ITooltip tooltip, CompoundTag combat) {
            int staggerTicks = combat.getInt("stagger_ticks");
            if (staggerTicks > 0) {
                String key = combat.getBoolean("hard_stagger") ? "jade.mobscombat.status.hard_stagger" : "jade.mobscombat.status.stagger";
                tooltip.add(Component.translatable(key, seconds(staggerTicks)).withStyle(combat.getBoolean("hard_stagger") ? ChatFormatting.RED : ChatFormatting.GOLD));
            }

            if (combat.getBoolean("guard_broken")) {
                tooltip.add(Component.translatable("jade.mobscombat.status.guard_broken").withStyle(ChatFormatting.RED));
            }

            int recoveryTicks = combat.getInt("recovery_ticks");
            if (recoveryTicks > 0) {
                tooltip.add(Component.translatable("jade.mobscombat.status.recovery", seconds(recoveryTicks)).withStyle(ChatFormatting.DARK_GRAY));
            }

            int counterTicks = combat.getInt("counter_window_ticks");
            if (counterTicks > 0) {
                tooltip.add(Component.translatable("jade.mobscombat.status.counter", seconds(counterTicks)).withStyle(ChatFormatting.AQUA));
            }

            int parryTicks = combat.getInt("parry_ticks");
            if (parryTicks > 0) {
                tooltip.add(Component.translatable("jade.mobscombat.status.parry", seconds(parryTicks)).withStyle(ChatFormatting.GOLD));
            }

            int stealthStrikeTicks = combat.getInt("stealth_strike_ticks");
            if (stealthStrikeTicks > 0) {
                tooltip.add(Component.translatable("jade.mobscombat.status.stealth_strike", seconds(stealthStrikeTicks)).withStyle(ChatFormatting.DARK_GREEN));
            }

            int cooldownTicks = combat.getInt("stagger_cooldown_ticks");
            if (cooldownTicks > 0) {
                tooltip.add(Component.translatable("jade.mobscombat.status.stagger_cooldown", seconds(cooldownTicks)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        private static ChatFormatting ratioColor(float current, float max) {
            float ratio = max <= 0.0F ? 1.0F : current / max;
            if (ratio <= 0.25F) {
                return ChatFormatting.RED;
            }
            if (ratio <= 0.55F) {
                return ChatFormatting.YELLOW;
            }
            return ChatFormatting.GREEN;
        }

        private static float percent(float current, float max) {
            return max <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(100.0F, current * 100.0F / max));
        }

        private static Component seconds(int ticks) {
            return Component.translatable("jade.mobscombat.seconds", Component.literal(format(ticks / 20.0F)));
        }

        private static String format(float value) {
            return String.format(Locale.ROOT, "%.1f", value);
        }

        private static String formatCompact(float value) {
            if (Math.abs(value - Math.round(value)) < 0.001F) {
                return Integer.toString(Math.round(value));
            }
            return format(value);
        }

        private static String formatSignedCompact(float value) {
            if (Math.abs(value) < 0.001F) {
                return "+0";
            }
            return (value > 0.0F ? "+" : "-") + formatCompact(Math.abs(value));
        }

        private static String displayTier(String tier) {
            if (tier == null || tier.isBlank()) {
                return "Unknown";
            }

            String normalized = tier.replace('_', ' ').replace('-', ' ');
            StringBuilder builder = new StringBuilder(normalized.length());
            boolean uppercaseNext = true;
            for (int i = 0; i < normalized.length(); i++) {
                char current = normalized.charAt(i);
                if (current == ' ') {
                    builder.append(current);
                    uppercaseNext = true;
                } else if (uppercaseNext) {
                    builder.append(Character.toUpperCase(current));
                    uppercaseNext = false;
                } else {
                    builder.append(Character.toLowerCase(current));
                }
            }
            return builder.toString();
        }
    }
}
