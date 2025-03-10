package eu.nk2.apathy.mixin;

import eu.nk2.apathy.context.OnBlockBrokenEventRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class ApathyMixinBlockInjector {

    @Inject(method = "onBreak", at = @At("TAIL"))
    public void onBreakInjection(
        World world,
        BlockPos pos,
        BlockState state,
        PlayerEntity player,
        CallbackInfoReturnable<BlockState> cir
    ) {
        if(player instanceof ServerPlayerEntity) {
            OnBlockBrokenEventRegistry.INSTANCE.publishOnBlockBrokenEvent(pos, state, player);
        }
    }
}
