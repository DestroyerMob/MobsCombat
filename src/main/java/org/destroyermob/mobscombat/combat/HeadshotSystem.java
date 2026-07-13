package org.destroyermob.mobscombat.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.destroyermob.mobscombat.config.CombatConfig;

public final class HeadshotSystem {
    private static final long IMPACT_TTL_TICKS = 5L;
    private static final Map<ImpactKey, ImpactRecord> PROJECTILE_IMPACTS = new HashMap<>();

    private HeadshotSystem() {
    }

    public static void recordProjectileImpact(ProjectileImpactEvent event) {
        if (!CombatConfig.projectileHeadshotsEnabled()
                || !(event.getProjectile().level() instanceof ServerLevel level)
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target)
                || !(event.getProjectile().getOwner() instanceof Player)) {
            return;
        }

        pruneOldImpacts(level);
        PROJECTILE_IMPACTS.put(
                new ImpactKey(event.getProjectile().getUUID(), target.getUUID()),
                new ImpactRecord(hit.getLocation(), level.getGameTime())
        );
    }

    public static void increaseProjectileHeadshotDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (!CombatConfig.projectileHeadshotsEnabled()
                || !(target.level() instanceof ServerLevel level)
                || event.getNewDamage() <= 0.0F
                || !(event.getSource().getEntity() instanceof Player)
                || !(event.getSource().getDirectEntity() instanceof Projectile projectile)) {
            return;
        }

        Vec3 impact = consumeImpact(level, projectile, target);
        boolean headshot = impact == null
                ? isFallbackHeadshot(projectile, target)
                : isHeadshot(target, impact);
        if (headshot) {
            event.setNewDamage(event.getNewDamage() * CombatConfig.projectileHeadshotDamageMultiplier());
        }
    }

    private static Vec3 consumeImpact(ServerLevel level, Projectile projectile, LivingEntity target) {
        ImpactRecord record = PROJECTILE_IMPACTS.remove(new ImpactKey(projectile.getUUID(), target.getUUID()));
        if (record == null || level.getGameTime() - record.gameTime() > IMPACT_TTL_TICKS) {
            return null;
        }
        return record.location();
    }

    private static boolean isFallbackHeadshot(Projectile projectile, LivingEntity target) {
        Vec3 start = projectile.position();
        Vec3 end = start.add(projectile.getDeltaMovement());
        return target.getBoundingBox().inflate(0.1D).clip(start, end)
                .map(hit -> isHeadshot(target, hit))
                .orElseGet(() -> isHeadshot(target, projectile.position()));
    }

    private static boolean isHeadshot(LivingEntity target, Vec3 impact) {
        double lower = Math.min(
                CombatConfig.projectileHeadshotLowerEyeBand(),
                CombatConfig.projectileHeadshotUpperEyeBand()
        );
        double upper = Math.max(
                CombatConfig.projectileHeadshotLowerEyeBand(),
                CombatConfig.projectileHeadshotUpperEyeBand()
        );
        double relativeToEyes = (impact.y() - target.getEyeY()) / Math.max(target.getBbHeight(), 0.001F);
        return relativeToEyes >= lower && relativeToEyes <= upper;
    }

    private static void pruneOldImpacts(ServerLevel level) {
        long cutoff = level.getGameTime() - IMPACT_TTL_TICKS;
        PROJECTILE_IMPACTS.entrySet().removeIf(entry -> entry.getValue().gameTime() < cutoff);
    }

    private record ImpactKey(UUID projectile, UUID target) {
    }

    private record ImpactRecord(Vec3 location, long gameTime) {
    }
}
