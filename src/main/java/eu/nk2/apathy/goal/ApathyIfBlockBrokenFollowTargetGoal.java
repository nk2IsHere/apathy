package eu.nk2.apathy.goal;

import eu.nk2.apathy.context.OnBlockBrokenEventHandler;
import eu.nk2.apathy.context.OnBlockBrokenEventRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ApathyIfBlockBrokenFollowTargetGoal extends FollowTargetGoal<PlayerEntity> implements OnBlockBrokenEventHandler {
    private final float maximalReactionDistance;
    private final Block reactionBlock;

    public ApathyIfBlockBrokenFollowTargetGoal(
        MobEntity mob,
        int reciprocalChance,
        boolean checkVisibility,
        boolean checkCanNavigate,
        TargetPredicate targetPredicate,
        float maximalReactionDistance,
        Block reactionBlock
    ) {
        super(mob, PlayerEntity.class, reciprocalChance, checkVisibility, checkCanNavigate, null);
        this.targetPredicate = targetPredicate;
        this.maximalReactionDistance = maximalReactionDistance;
        this.reactionBlock = reactionBlock;

        OnBlockBrokenEventRegistry.INSTANCE.registerOnBlockBrokenHandler(this);
    }

    @Override
    public void onBlockBroken(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if(player instanceof ServerPlayerEntity) {
            System.out.println("[" + mob + "] Block broken: " + player + " " + state);
            if(mob.distanceTo(player) <= this.maximalReactionDistance && state.getBlock().is(reactionBlock)) {
                System.out.println("[" + mob + "] Perform follow on: " + player);
                this.targetEntity = player;
            }
        }
    }

    @Override
    public boolean canStart() {
        return targetEntity != null;
    }
}
