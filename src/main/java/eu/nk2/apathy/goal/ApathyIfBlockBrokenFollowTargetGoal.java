package eu.nk2.apathy.goal;

import eu.nk2.apathy.context.OnBlockBrokenEventRegistry;
import eu.nk2.apathy.context.OnLivingEntityDeadEventRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ApathyIfBlockBrokenFollowTargetGoal extends FollowTargetGoal<PlayerEntity> {
    private final Logger logger = LogManager.getLogger("Apathy");

    private final float maximalReactionDistance;
    private final Block reactionBlock;

    private UUID onBlockBrokenHandlerId;
    private UUID onLivingEntityDeadHandlerId;

    private final Map<UUID, BlockState> playerMemory = new HashMap<>();

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

        this.onBlockBrokenHandlerId = OnBlockBrokenEventRegistry.INSTANCE.registerOnBlockBrokenHandler((pos, state, playerUuid) -> {
            PlayerEntity player = this.mob.world.getPlayerByUuid(playerUuid);
            if(player == null) return;

            logger.info("[" + this.mob + "] Block broken: " + playerUuid + " " + state);
            if(state.getBlock().is(this.reactionBlock) && mob.distanceTo(player) <= this.maximalReactionDistance) {
                logger.info("[" + this.mob + "] Perform follow on: " + playerUuid);
                playerMemory.put(playerUuid, state);
            }
        });

        this.onLivingEntityDeadHandlerId = OnLivingEntityDeadEventRegistry.INSTANCE.registerOnLivingEntityDeadHandler((world, livingEntity, damageSource) -> {
            if(this.mob.getEntityId() == livingEntity.getEntityId()) {
                logger.info("[" + this.mob + "] Unregister goal from events");
                OnBlockBrokenEventRegistry.INSTANCE.unregisterOnBlockBrokenHandler(onBlockBrokenHandlerId);
                OnLivingEntityDeadEventRegistry.INSTANCE.unregisterOnLivingEntityDeadHandler(onLivingEntityDeadHandlerId);
            }
        });
    }

    @Override
    protected void findClosestTarget() {
        this.playerMemory.keySet()
            .stream()
            .map(playerUuid -> mob.world.getPlayerByUuid(playerUuid))
            .filter(Objects::nonNull)
            .map((player) -> new Pair<>(player, mob.distanceTo(player)))
            .min(Comparator.comparing(Pair::getRight))
            .ifPresent((playerDistancePair) -> {
                this.targetEntity = playerDistancePair.getLeft();
                this.playerMemory.clear();
            });
    }
}
