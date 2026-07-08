package org.destroyermob.mobscombat.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperFuseAccessor {
    @Accessor("DATA_IS_IGNITED")
    static EntityDataAccessor<Boolean> mobscombat$ignitedDataAccessor() {
        throw new AssertionError();
    }

    @Accessor("swell")
    void mobscombat$setSwell(int swell);

    @Accessor("oldSwell")
    void mobscombat$setOldSwell(int oldSwell);
}
