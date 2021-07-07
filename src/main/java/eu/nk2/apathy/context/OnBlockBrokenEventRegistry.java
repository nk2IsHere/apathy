package eu.nk2.apathy.context;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface OnBlockBrokenEventRegistry {

    UUID registerOnBlockBrokenHandler(OnBlockBrokenEventHandler handler);
    void unregisterOnBlockBrokenHandler(UUID handlerId);
    void publishOnBlockBrokenEvent(BlockPos pos, BlockState state, PlayerEntity player);

    OnBlockBrokenEventRegistry INSTANCE = new OnBlockBrokenEventRegistry() {

        private final Map<UUID, OnBlockBrokenEventHandler> onBlockBrokenEventHandlers = new ConcurrentHashMap<>();

        @Override
        public UUID registerOnBlockBrokenHandler(OnBlockBrokenEventHandler handler) {
            UUID handlerId = UUID.randomUUID();
            onBlockBrokenEventHandlers.put(handlerId, handler);

            return handlerId;
        }

        @Override
        public void unregisterOnBlockBrokenHandler(UUID handlerId) {
            onBlockBrokenEventHandlers.remove(handlerId);
        }

        @Override
        public void publishOnBlockBrokenEvent(BlockPos pos, BlockState state, PlayerEntity player) {
            onBlockBrokenEventHandlers.values()
                .forEach(handler -> handler.onBlockBroken(pos, state, player.getUuid()));
        }
    };
}
