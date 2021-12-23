package eu.nk2.apathy.context;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public interface ApathyMixinEntityTypeAccessor<T extends Entity> {

    EntityType.EntityFactory<T> getFactory();
    void setCustomFactory(EntityType.EntityFactory<T> entityFactory);
}
