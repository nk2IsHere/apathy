package eu.nk2.apathy.mixin;

import eu.nk2.apathy.context.OnBlockBrokenEventRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class ApathyMixinBlockInjector {

    @Inject(method = "onBreak", at = @At("TAIL"))
    public void onBreakInjection(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        OnBlockBrokenEventRegistry.INSTANCE.publishOnBlockBrokenEvent(world, pos, state, player);
    }
}
