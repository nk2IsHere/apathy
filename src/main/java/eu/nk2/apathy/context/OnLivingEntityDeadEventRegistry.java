package eu.nk2.apathy.context;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface OnLivingEntityDeadEventRegistry {

    UUID registerOnLivingEntityDeadHandler(OnLivingEntityDeadEventHandler handler);
    void unregisterOnLivingEntityDeadHandler(UUID handlerId);
    void publishOnLivingEntityDeadEvent(World world, LivingEntity livingEntity, DamageSource damageSource);

    OnLivingEntityDeadEventRegistry INSTANCE = new OnLivingEntityDeadEventRegistry() {

        private final Map<UUID, OnLivingEntityDeadEventHandler> onLivingEntityDeadHandlers = new ConcurrentHashMap();

        @Override
        public UUID  registerOnLivingEntityDeadHandler(OnLivingEntityDeadEventHandler handler) {
            UUID handlerId = UUID.randomUUID();
            onLivingEntityDeadHandlers.put(handlerId, handler);

            return handlerId;
        }

        @Override
        public void unregisterOnLivingEntityDeadHandler(UUID handlerId) {
            onLivingEntityDeadHandlers.remove(handlerId);
        }

        @Override
        public void publishOnLivingEntityDeadEvent(World world, LivingEntity livingEntity, DamageSource damageSource) {
            onLivingEntityDeadHandlers.values()
                .forEach(handler -> handler.onLivingEntityDead(world, livingEntity, damageSource));
        }
    };
}
