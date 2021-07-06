package eu.nk2.apathy.context;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface OnBlockBrokenEventHandler {
    void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player);
}
