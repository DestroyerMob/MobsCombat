package org.destroyermob.mobscombat.client.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.vehicle.Boat;
import org.destroyermob.mobscombat.combat.DualWieldSystem;

public final class PunchyCompat {
    private static final int COMBO_RESET_TICKS = 30;
    private static final int DUAL_THRUST_WINDOW_TICKS = 20;
    private static final int VISUAL_COMBO_SIZE = 3;
    private static final int SLASH_COMBO_INDEX = 0;
    private static final int THRUST_COMBO_INDEX = 2;
    private static final String THRUST_CLIP_NAME = "attack_3";
    private static boolean lookedUp;
    private static Method recordCriticalCandidate;
    private static Method recordAttack;
    private static boolean comboLookedUp;
    private static Field attackState;
    private static Field comboIndex;
    private static int nextVisualComboStep;
    private static long lastVisualComboTick = Long.MIN_VALUE;
    private static long forceDualThrustUntilTick = Long.MIN_VALUE;
    private static Method clipName;

    private PunchyCompat() {
    }

    public static boolean recordAttack(Minecraft minecraft, InteractionHand hand) {
        if (minecraft == null || hand == null || !lookup()) {
            return false;
        }

        boolean controllingBoat = isControllingBoat(minecraft);
        boolean finisher = controllingBoat ? false : primeVisualCombo(minecraft);
        try {
            recordCriticalCandidate.invoke(null, minecraft);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }
        try {
            recordAttack.invoke(null, hand);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }
        return finisher;
    }

    public static boolean shouldForceDualThrust(Minecraft minecraft, Object clip) {
        if (minecraft == null || clip == null || forceDualThrustUntilTick == Long.MIN_VALUE || isControllingBoat(minecraft)) {
            return false;
        }

        long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        if (gameTime > forceDualThrustUntilTick) {
            forceDualThrustUntilTick = Long.MIN_VALUE;
            return false;
        }
        return THRUST_CLIP_NAME.equals(getClipName(clip)) && hasDualWeapons(minecraft);
    }

    private static boolean lookup() {
        if (lookedUp) {
            return recordAttack != null;
        }
        lookedUp = true;
        try {
            Class<?> tracker = Class.forName("punchy.client.state.AttackActionTracker");
            recordCriticalCandidate = tracker.getMethod("recordCriticalCandidate", Minecraft.class);
            recordAttack = tracker.getMethod("recordAttack", InteractionHand.class);
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            recordCriticalCandidate = null;
            recordAttack = null;
            return false;
        }
    }

    private static boolean primeVisualCombo(Minecraft minecraft) {
        if (!lookupCombo()) {
            return false;
        }

        try {
            long gameTime = minecraft.level == null ? 0L : minecraft.level.getGameTime();
            if (lastVisualComboTick == Long.MIN_VALUE || gameTime - lastVisualComboTick > COMBO_RESET_TICKS) {
                nextVisualComboStep = 0;
                forceDualThrustUntilTick = Long.MIN_VALUE;
            }

            boolean thrustBeat = nextVisualComboStep == VISUAL_COMBO_SIZE - 1;
            boolean finisher = thrustBeat && hasDualWeapons(minecraft);
            // Main slash, mirrored offhand slash, then Punchy's attack_3 as a simultaneous thrust.
            comboIndex.setInt(attackState.get(null), thrustBeat ? THRUST_COMBO_INDEX : SLASH_COMBO_INDEX);
            forceDualThrustUntilTick = finisher ? gameTime + DUAL_THRUST_WINDOW_TICKS : Long.MIN_VALUE;
            nextVisualComboStep = (nextVisualComboStep + 1) % VISUAL_COMBO_SIZE;
            lastVisualComboTick = gameTime;
            return finisher;
        } catch (IllegalAccessException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean lookupCombo() {
        if (comboLookedUp) {
            return attackState != null && comboIndex != null;
        }
        comboLookedUp = true;
        try {
            Class<?> clientClass = Class.forName("punchy.client.PunchyClient");
            Class<?> attackStateClass = Class.forName("punchy.client.state.AttackStateMachine");
            attackState = clientClass.getDeclaredField("ATTACK_STATE");
            comboIndex = attackStateClass.getDeclaredField("comboIndex");
            attackState.setAccessible(true);
            comboIndex.setAccessible(true);
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            attackState = null;
            comboIndex = null;
            return false;
        }
    }

    private static String getClipName(Object clip) {
        try {
            if (clipName == null) {
                clipName = clip.getClass().getMethod("getName");
            }
            Object name = clipName.invoke(clip);
            return name instanceof String string ? string : "";
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return "";
        }
    }

    private static boolean hasDualWeapons(Minecraft minecraft) {
        if (minecraft.player == null) {
            return false;
        }
        return DualWieldSystem.hasUsableMainHandWeapon(minecraft.player) && DualWieldSystem.hasUsableOffhandWeapon(minecraft.player);
    }

    private static boolean isControllingBoat(Minecraft minecraft) {
        return minecraft != null && minecraft.player != null && minecraft.player.getControlledVehicle() instanceof Boat;
    }
}
