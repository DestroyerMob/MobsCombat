package org.destroyermob.mobscombat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.destroyermob.mobscombat.MobsCombat;

public final class AggressionIndicatorRenderer {
    private static final ResourceLocation TEXTURE = MobsCombat.id("textures/entity/aggression_indicator.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final float ENTER_TICKS = 4.0F;
    private static final float FADE_START_TICK = 26.0F;
    private static final float DURATION_TICKS = 36.0F;
    private static final float INDICATOR_SIZE = 0.62F;
    private static final Map<Integer, Long> ALERT_START_TICKS = new HashMap<>();
    private static ClientLevel activeLevel;

    private AggressionIndicatorRenderer() {
    }

    public static void show(int mobEntityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        resetForLevel(minecraft.level);
        ALERT_START_TICKS.put(mobEntityId, minecraft.level.getGameTime());
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            activeLevel = null;
            ALERT_START_TICKS.clear();
            return;
        }
        resetForLevel(minecraft.level);
        if (minecraft.options.hideGui || ALERT_START_TICKS.isEmpty()) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float currentTick = minecraft.level.getGameTime() + partialTick;
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RENDER_TYPE);
        Iterator<Map.Entry<Integer, Long>> iterator = ALERT_START_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> alert = iterator.next();
            float age = currentTick - alert.getValue();
            if (age < 0.0F || age >= DURATION_TICKS) {
                iterator.remove();
                continue;
            }

            Entity entity = minecraft.level.getEntity(alert.getKey());
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                continue;
            }
            if (!mob.isInvisibleTo(minecraft.player)) {
                renderIndicator(event, consumer, mob, partialTick, age);
            }
        }
        buffers.endBatch(RENDER_TYPE);
    }

    private static void renderIndicator(
            RenderLevelStageEvent event,
            VertexConsumer consumer,
            Mob mob,
            float partialTick,
            float age
    ) {
        Camera camera = event.getCamera();
        double x = Mth.lerp(partialTick, mob.xOld, mob.getX());
        double y = Mth.lerp(partialTick, mob.yOld, mob.getY()) + mob.getBbHeight() + 0.42D;
        double z = Mth.lerp(partialTick, mob.zOld, mob.getZ());
        double bob = Math.sin(age * Math.PI / 10.0D) * 0.035D;
        Vec3 relative = new Vec3(x, y + bob, z).subtract(camera.getPosition());

        float visibility = visibility(age);
        float scale = INDICATOR_SIZE * (0.72F + visibility * 0.28F);
        int color = Mth.clamp(Math.round(visibility * 255.0F), 0, 255) << 24 | 0xFFFFFF;
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(relative.x, relative.y, relative.z);
        poses.mulPose(camera.rotation());
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        poses.scale(scale, scale, scale);

        vertex(poses, consumer, -0.5F, -0.5F, 0.0F, 1.0F, color);
        vertex(poses, consumer, 0.5F, -0.5F, 1.0F, 1.0F, color);
        vertex(poses, consumer, 0.5F, 0.5F, 1.0F, 0.0F, color);
        vertex(poses, consumer, -0.5F, 0.5F, 0.0F, 0.0F, color);
        poses.popPose();
    }

    private static void vertex(
            PoseStack poses,
            VertexConsumer consumer,
            float x,
            float y,
            float u,
            float v,
            int color
    ) {
        consumer.addVertex(poses.last(), x, y, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(poses.last(), 0.0F, 0.0F, 1.0F);
    }

    private static float visibility(float age) {
        if (age < ENTER_TICKS) {
            return smoothstep(age / ENTER_TICKS);
        }
        if (age > FADE_START_TICK) {
            return smoothstep((DURATION_TICKS - age) / (DURATION_TICKS - FADE_START_TICK));
        }
        return 1.0F;
    }

    private static float smoothstep(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static void resetForLevel(ClientLevel level) {
        if (activeLevel != level) {
            activeLevel = level;
            ALERT_START_TICKS.clear();
        }
    }
}
