package eu.nk2.apathy.context;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface OnHandStackChangedEventRegistry {

    UUID registerOnHandStackChangedHandler(OnHandStackChangedEventHandler handler);
    void unregisterOnHandStackChangedHandler(UUID handlerId);
    void publishOnHandStackChangedEvent(OnHandStackChangedEventHandler.Hand hand, PlayerEntity playerEntity, ItemStack previousStack, ItemStack currentStack);

    OnHandStackChangedEventRegistry INSTANCE = new OnHandStackChangedEventRegistry() {

        private final Map<UUID, OnHandStackChangedEventHandler> onHandStackChangedHandlers = new ConcurrentHashMap<>();
        private final Map<UUID, Pair<OnHandStackChangedEventHandler.Hand, ItemStack>> handStateByPlayerCache = new ConcurrentHashMap<>();

        @Override
        public UUID registerOnHandStackChangedHandler(OnHandStackChangedEventHandler handler) {
            UUID handlerId = UUID.randomUUID();
            onHandStackChangedHandlers.put(handlerId, handler);
            handStateByPlayerCache.forEach((playerUuid, handItemStack) -> handler.onHandStackChanged(
                handItemStack.getLeft(),
                playerUuid,
                null,
                handItemStack.getRight()
            ));

            return handlerId;
        }

        @Override
        public void unregisterOnHandStackChangedHandler(UUID handlerId) {
            onHandStackChangedHandlers.remove(handlerId);
        }

        @Override
        public void publishOnHandStackChangedEvent(OnHandStackChangedEventHandler.Hand hand, PlayerEntity playerEntity, ItemStack previousStack, ItemStack currentStack) {
            onHandStackChangedHandlers.values().forEach(handler -> handler.onHandStackChanged(hand, playerEntity.getUuid(), previousStack, currentStack));
            handStateByPlayerCache.put(playerEntity.getUuid(), new Pair<>(hand, currentStack));
        }
    };
}
