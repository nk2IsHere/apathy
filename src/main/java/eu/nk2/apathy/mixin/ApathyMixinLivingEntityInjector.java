package eu.nk2.apathy.mixin;

import eu.nk2.apathy.context.OnLivingEntityDeadEventRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class ApathyMixinLivingEntityInjector {

    @Inject(method = "onDeath", at=@At("TAIL"))
    void onDeathInjection(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        OnLivingEntityDeadEventRegistry.INSTANCE.publishOnLivingEntityDeadEvent(livingEntity.getWorld(), livingEntity, damageSource);
    }
}
