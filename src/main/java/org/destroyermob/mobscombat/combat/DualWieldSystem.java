package org.destroyermob.mobscombat.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.destroyermob.mobscombat.MobsCombat;
import org.destroyermob.mobscombat.config.CombatConfig;

public final class DualWieldSystem {
    private static final ResourceLocation ATTACK_SPEED_MODIFIER_ID = MobsCombat.id("dual_wield_attack_speed");
    private static final int ACTIVE_ATTACK_TICKS = 3;
    private static final float MIN_DUAL_WIELD_ATTACK_STRENGTH = 0.9F;
    private static final Map<UUID, Boolean> NEXT_OFFHAND_ATTACK = new HashMap<>();
    private static final Map<UUID, ActiveAttack> ACTIVE_ATTACKS = new HashMap<>();
    private static final Map<UUID, QueuedAttackHand> QUEUED_ATTACK_HANDS = new HashMap<>();

    private DualWieldSystem() {
    }

    public static void prepareAttack(ServerPlayer player, LivingEntity target) {
        updateAttackSpeedModifier(player);
        if (!CombatConfig.dualWieldEnabled() || target == player || isAttackSuppressed(player)) {
            ACTIVE_ATTACKS.remove(player.getUUID());
            return;
        }

        QueuedAttackHand queued = consumeQueuedAttackHand(player, target.getId());
        if (queued == null) {
            ACTIVE_ATTACKS.remove(player.getUUID());
            return;
        }

        if (queued.hand() == InteractionHand.OFF_HAND) {
            if (!armOffhandAttack(player, target, queued.finisher())) {
                ACTIVE_ATTACKS.remove(player.getUUID());
            }
            return;
        }

        if (isDualWielding(player)) {
            armMainHandAttack(player, target, queued.finisher());
        } else {
            ACTIVE_ATTACKS.remove(player.getUUID());
        }
    }

    public static InteractionHand claimClientAttackHand(Player player) {
        return selectAttackHand(player, true);
    }

    public static boolean hasUsableOffhandWeapon(Player player) {
        return CombatConfig.dualWieldEnabled()
                && isWeapon(player.getOffhandItem())
                && !isTwoHanded(player.getMainHandItem())
                && !isTwoHanded(player.getOffhandItem());
    }

    public static boolean hasUsableMainHandWeapon(Player player) {
        return CombatConfig.dualWieldEnabled()
                && isWeapon(player.getMainHandItem())
                && !isTwoHanded(player.getMainHandItem())
                && !isTwoHanded(player.getOffhandItem());
    }

    public static boolean shouldUseCustomAttack(Player player) {
        return isDualWielding(player);
    }

    public static boolean shouldWaitForDualWieldCooldown(Player player) {
        return isDualWielding(player) && player.getAttackStrengthScale(0.0F) < MIN_DUAL_WIELD_ATTACK_STRENGTH;
    }

    public static void handleAttack(ServerPlayer player, int targetId, boolean usingSecondaryAction, InteractionHand hand, boolean finisher) {
        updateAttackSpeedModifier(player);
        if (player.isSpectator() || isAttackSuppressed(player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(targetId);
        if (!(entity instanceof LivingEntity target)
                || target == player
                || !level.getWorldBorder().isWithinBounds(target.blockPosition())
                || !player.canInteractWithEntity(target.getBoundingBox(), 1.0D)) {
            return;
        }

        ItemStack weapon = hand == InteractionHand.OFF_HAND ? player.getOffhandItem() : player.getMainHandItem();
        if (!weapon.isItemEnabled(level.enabledFeatures())) {
            return;
        }

        boolean dualWielding = isDualWielding(player);
        if (!dualWielding) {
            if (hand != InteractionHand.MAIN_HAND || !(player.getControlledVehicle() instanceof Boat)) {
                return;
            }
            player.setShiftKeyDown(usingSecondaryAction);
            player.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(target, usingSecondaryAction));
            return;
        }

        if (!isWeapon(weapon)) {
            return;
        }

        boolean validFinisher = finisher && dualWielding;
        QUEUED_ATTACK_HANDS.put(player.getUUID(), new QueuedAttackHand(targetId, player.tickCount, hand, validFinisher));
        player.setShiftKeyDown(usingSecondaryAction);
        if (hand == InteractionHand.OFF_HAND) {
            if (!armOffhandAttack(player, target, validFinisher)) {
                QUEUED_ATTACK_HANDS.remove(player.getUUID());
                return;
            }
        } else if (dualWielding) {
            armMainHandAttack(player, target, validFinisher);
        }
        player.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(target, usingSecondaryAction));
    }

    public static ItemStack getWeaponItemOverride(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return ItemStack.EMPTY;
        }
        if (getActiveAttackHand(player) != InteractionHand.OFF_HAND) {
            return ItemStack.EMPTY;
        }
        ItemStack offhand = player.getOffhandItem();
        return isWeapon(offhand) ? offhand : ItemStack.EMPTY;
    }

    public static InteractionHand getAttackHandOverride(Player player, InteractionHand originalHand) {
        return getActiveAttackHand(player) == InteractionHand.OFF_HAND ? InteractionHand.OFF_HAND : originalHand;
    }

    public static ItemStack getMainHandItemOverride(Player player) {
        if (getActiveAttackHand(player) != InteractionHand.OFF_HAND) {
            return player.getMainHandItem();
        }

        ItemStack offhand = player.getOffhandItem();
        return isWeapon(offhand) ? offhand : player.getMainHandItem();
    }

    public static void setItemInAttackHand(Player player, InteractionHand originalHand, ItemStack stack) {
        player.setItemInHand(getAttackHandOverride(player, originalHand), stack);
    }

    public static void adjustIncomingDamage(LivingIncomingDamageEvent event) {
        if (!CombatConfig.dualWieldEnabled()
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }

        ActiveAttack attack = getActiveAttack(player, event.getEntity().getId());
        if (attack == null || event.getAmount() <= 0.0F) {
            return;
        }

        float correctedAmount = event.getAmount();
        if (attack.dualWielding()) {
            correctedAmount *= CombatConfig.dualWieldDamageMultiplier();
        }
        if (attack.finisher()) {
            correctedAmount *= CombatConfig.dualWieldFinisherDamageMultiplier();
        }
        event.setAmount(Math.max(0.0F, correctedAmount));
    }

    public static double getAttackDamageAttributeOverride(Player player, Holder<Attribute> attribute, double originalValue) {
        if (!CombatConfig.dualWieldEnabled() || !Attributes.ATTACK_DAMAGE.equals(attribute)) {
            return originalValue;
        }

        ActiveAttack attack = getActiveAttack(player);
        return attack == null ? originalValue : attack.weaponAttributeDamage();
    }

    public static float getAttackStrengthScaleOverride(Player player, float originalValue) {
        ActiveAttack attack = getActiveAttack(player);
        return attack != null && attack.dualWielding() ? 1.0F : originalValue;
    }

    public static float postureMultiplier(ServerPlayer player, int targetId) {
        ActiveAttack attack = getActiveAttack(player, targetId);
        return attack != null && attack.finisher() ? CombatConfig.dualWieldFinisherPostureMultiplier() : 1.0F;
    }

    public static void tick(ServerPlayer player) {
        updateAttackSpeedModifier(player);
        ActiveAttack attack = ACTIVE_ATTACKS.get(player.getUUID());
        if (attack != null && player.tickCount - attack.tick() > ACTIVE_ATTACK_TICKS) {
            ACTIVE_ATTACKS.remove(player.getUUID());
        }
        QueuedAttackHand queued = QUEUED_ATTACK_HANDS.get(player.getUUID());
        if (queued != null && player.tickCount - queued.tick() > ACTIVE_ATTACK_TICKS) {
            QUEUED_ATTACK_HANDS.remove(player.getUUID());
        }
    }

    public static void clear(LivingEntity entity) {
        UUID id = entity.getUUID();
        NEXT_OFFHAND_ATTACK.remove(id);
        ACTIVE_ATTACKS.remove(id);
        QUEUED_ATTACK_HANDS.remove(id);
        if (entity instanceof ServerPlayer player) {
            removeAttackSpeedModifier(player);
        }
    }

    private static InteractionHand selectAttackHand(Player player, boolean advance) {
        if (!CombatConfig.dualWieldEnabled()) {
            return InteractionHand.MAIN_HAND;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean mainWeapon = isWeapon(mainHand);
        boolean offhandWeapon = isWeapon(offHand);
        if (!offhandWeapon) {
            if (advance) {
                NEXT_OFFHAND_ATTACK.put(player.getUUID(), false);
            }
            return InteractionHand.MAIN_HAND;
        }
        if (!mainWeapon) {
            return InteractionHand.OFF_HAND;
        }

        boolean useOffhand = NEXT_OFFHAND_ATTACK.getOrDefault(player.getUUID(), false);
        if (advance) {
            NEXT_OFFHAND_ATTACK.put(player.getUUID(), !useOffhand);
        }
        return useOffhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void updateNextAttackHand(Player player, boolean mainWeapon, boolean usedOffhand) {
        if (mainWeapon) {
            NEXT_OFFHAND_ATTACK.put(player.getUUID(), !usedOffhand);
        }
    }

    private static QueuedAttackHand consumeQueuedAttackHand(ServerPlayer player, int targetId) {
        QueuedAttackHand queued = QUEUED_ATTACK_HANDS.remove(player.getUUID());
        if (queued == null || queued.targetId() != targetId || player.tickCount - queued.tick() > ACTIVE_ATTACK_TICKS) {
            return null;
        }
        return queued;
    }

    private static boolean armOffhandAttack(ServerPlayer player, LivingEntity target, boolean finisher) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean mainWeapon = isWeapon(mainHand);
        if (!isWeapon(offHand)) {
            NEXT_OFFHAND_ATTACK.put(player.getUUID(), false);
            ACTIVE_ATTACKS.remove(player.getUUID());
            QUEUED_ATTACK_HANDS.remove(player.getUUID());
            return false;
        }

        updateNextAttackHand(player, mainWeapon, true);
        ACTIVE_ATTACKS.put(player.getUUID(), ActiveAttack.capture(player, target, mainHand, offHand, InteractionHand.OFF_HAND, mainWeapon, finisher));
        return true;
    }

    private static boolean armMainHandAttack(ServerPlayer player, LivingEntity target, boolean finisher) {
        ItemStack mainHand = player.getMainHandItem();
        if (!isWeapon(mainHand)) {
            ACTIVE_ATTACKS.remove(player.getUUID());
            QUEUED_ATTACK_HANDS.remove(player.getUUID());
            return false;
        }

        ACTIVE_ATTACKS.put(player.getUUID(), ActiveAttack.capture(player, target, mainHand, mainHand, InteractionHand.MAIN_HAND, isWeapon(player.getOffhandItem()), finisher));
        return true;
    }

    private static ActiveAttack getActiveAttack(Player player) {
        ActiveAttack attack = ACTIVE_ATTACKS.get(player.getUUID());
        if (attack == null) {
            return null;
        }
        if (player.tickCount - attack.tick() > ACTIVE_ATTACK_TICKS) {
            ACTIVE_ATTACKS.remove(player.getUUID());
            return null;
        }
        return attack;
    }

    private static InteractionHand getActiveAttackHand(Player player) {
        ActiveAttack attack = getActiveAttack(player);
        return attack == null ? InteractionHand.MAIN_HAND : attack.hand();
    }

    private static ActiveAttack getActiveAttack(ServerPlayer player, int targetId) {
        ActiveAttack attack = getActiveAttack(player);
        if (attack == null) {
            return null;
        }
        return attack.targetId() == targetId ? attack : null;
    }

    private static float selectedWeaponAttributeDamage(ServerPlayer player, ItemStack currentMainHand, ItemStack selectedWeapon) {
        float currentAttributeDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float currentMainHandDamage = stackAttackDamage(player, currentMainHand);
        float selectedWeaponDamage = stackAttackDamage(player, selectedWeapon);
        return Math.max(0.0F, selectedWeaponDamage + currentAttributeDamage - currentMainHandDamage);
    }

    private static float stackAttackDamage(ServerPlayer player, ItemStack stack) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        double baseDamage = attackDamage == null ? Attributes.ATTACK_DAMAGE.value().getDefaultValue() : attackDamage.getBaseValue();
        if (stack == null || stack.isEmpty()) {
            return (float) baseDamage;
        }
        return (float) computeStackAttackDamage(stack, baseDamage);
    }

    private static double computeStackAttackDamage(ItemStack stack, double baseDamage) {
        double[] addValue = {0.0D};
        double[] addMultipliedBase = {0.0D};
        double[] multipliedTotal = {1.0D};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (!Attributes.ATTACK_DAMAGE.equals(attribute)) {
                return;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> addValue[0] += modifier.amount();
                case ADD_MULTIPLIED_BASE -> addMultipliedBase[0] += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal[0] *= 1.0D + modifier.amount();
            }
        });

        double damageWithAdds = baseDamage + addValue[0];
        double damageWithBaseMultipliers = damageWithAdds + damageWithAdds * addMultipliedBase[0];
        return Math.max(0.0D, damageWithBaseMultipliers * multipliedTotal[0]);
    }

    private static void updateAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed == null) {
            return;
        }
        if (!isDualWielding(player)) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            return;
        }

        float cooldownMultiplier = Math.max(0.05F, CombatConfig.dualWieldCooldownMultiplier());
        double speedBonus = (1.0D / cooldownMultiplier) - 1.0D;
        attackSpeed.addOrUpdateTransientModifier(new AttributeModifier(
                ATTACK_SPEED_MODIFIER_ID,
                speedBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void removeAttackSpeedModifier(ServerPlayer player) {
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
    }

    private static boolean isDualWielding(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return CombatConfig.dualWieldEnabled()
                && isWeapon(mainHand)
                && isWeapon(offHand)
                && !isTwoHanded(mainHand)
                && !isTwoHanded(offHand);
    }

    private static boolean isAttackSuppressed(ServerPlayer player) {
        CombatState state = CombatStateManager.get(player);
        return state != null && state.isStaggered();
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty() || WeaponProfileResolver.isShieldLike(stack)) {
            return false;
        }
        return WeaponProfileResolver.resolve(stack).recognizedWeapon();
    }

    private static boolean isTwoHanded(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(CombatTags.Items.TWO_HANDED);
    }

    private record ActiveAttack(
            int targetId,
            int tick,
            InteractionHand hand,
            float weaponAttributeDamage,
            boolean dualWielding,
            boolean finisher
    ) {
        static ActiveAttack capture(ServerPlayer player, LivingEntity target, ItemStack currentMainHand, ItemStack selectedWeapon, InteractionHand hand, boolean dualWielding, boolean finisher) {
            return new ActiveAttack(
                    target.getId(),
                    player.tickCount,
                    hand,
                    selectedWeaponAttributeDamage(player, currentMainHand, selectedWeapon),
                    dualWielding,
                    finisher
            );
        }
    }

    private record QueuedAttackHand(int targetId, int tick, InteractionHand hand, boolean finisher) {
    }
}
