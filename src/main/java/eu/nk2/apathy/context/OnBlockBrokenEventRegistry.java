package eu.nk2.apathy.context;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public interface OnBlockBrokenEventRegistry {

    void registerOnBlockBrokenHandler(OnBlockBrokenEventHandler handler);
    void publishOnBlockBrokenEvent(World world, BlockPos pos, BlockState state, PlayerEntity player);

    OnBlockBrokenEventRegistry INSTANCE = new OnBlockBrokenEventRegistry() {

        private final ArrayList<OnBlockBrokenEventHandler> onBlockBrokenEventHandlers = new ArrayList<>();

        @Override
        public void registerOnBlockBrokenHandler(OnBlockBrokenEventHandler handler) {
            onBlockBrokenEventHandlers.add(handler);
        }

        @Override
        public void publishOnBlockBrokenEvent(World world, BlockPos pos, BlockState state, PlayerEntity player) {
            onBlockBrokenEventHandlers.forEach(handler -> handler.onBlockBroken(world, pos, state, player));
        }
    };
}
