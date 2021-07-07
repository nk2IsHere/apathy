package eu.nk2.apathy.context;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

@FunctionalInterface
public interface OnBlockBrokenEventHandler {
    void onBlockBroken(BlockPos pos, BlockState state, UUID playerUuid);
}
