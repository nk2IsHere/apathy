package eu.nk2.apathy.mixin;

import eu.nk2.apathy.context.ApathyMixinEntityTypeAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class ApathyMixinEntityTypeInjector<T extends Entity> implements ApathyMixinEntityTypeAccessor<T> {

    @Unique
    private EntityType.EntityFactory<T> customFactory;

    @Accessor("factory")
    public abstract EntityType.EntityFactory<T> getFactory();

    public void apathy$setCustomFactory(EntityType.EntityFactory<T> customFactory) {
        this.customFactory = customFactory;
    }

    //public T create(World world)
    @Inject(method = "create(Lnet/minecraft/world/World;Lnet/minecraft/entity/SpawnReason;)Lnet/minecraft/entity/Entity;", at = @At("HEAD"), cancellable = true)
    public void createInjection(
        World world,
        SpawnReason reason,
        CallbackInfoReturnable<T> cir
    ) {
        var entityType = (EntityType<T>) (Object) this;
        var factory = customFactory != null ? customFactory : getFactory();

        cir.setReturnValue(
            entityType.isEnabled(world.getEnabledFeatures())
                ? factory.create(entityType, world)
                : null
        );
    }
}
