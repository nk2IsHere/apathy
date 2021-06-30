package eu.nk2.apathy.mixin;

import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface ApathyMixinEntityFactoryAccessor {
    @Accessor("factory") EntityType.EntityFactory getFactory();
    @Accessor("factory") void setFactory(EntityType.EntityFactory entityFactory);
}
