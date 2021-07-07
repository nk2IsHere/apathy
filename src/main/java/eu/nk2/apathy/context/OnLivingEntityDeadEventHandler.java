package eu.nk2.apathy.context;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

@FunctionalInterface
public interface OnLivingEntityDeadEventHandler {
    void onLivingEntityDead(World world, LivingEntity livingEntity, DamageSource damageSource);
}
