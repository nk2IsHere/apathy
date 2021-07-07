package eu.nk2.apathy.goal;

import eu.nk2.apathy.context.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ApathyIfItemSelectedFollowTargetGoal extends FollowTargetGoal<PlayerEntity> {
    private final Logger logger = LogManager.getLogger("Apathy");

    private final float maximalFollowDistance;
    private final ItemStack reactionItemStack;

    private UUID onHandStackChangedHandlerId;
    private UUID onLivingEntityDeadHandlerId;

    private final Map<UUID, ItemStack> playerMemory = new HashMap<>();

    public ApathyIfItemSelectedFollowTargetGoal(
        MobEntity mob,
        int reciprocalChance,
        boolean checkVisibility,
        boolean checkCanNavigate,
        TargetPredicate targetPredicate,
        float maximalFollowDistance,
        ItemStack reactionItemStack
    ) {
        super(mob, PlayerEntity.class, reciprocalChance, checkVisibility, checkCanNavigate, null);
        this.targetPredicate = targetPredicate;
        this.maximalFollowDistance = maximalFollowDistance;
        this.reactionItemStack = reactionItemStack;

        this.onHandStackChangedHandlerId = OnHandStackChangedEventRegistry.INSTANCE.registerOnHandStackChangedHandler((hand, playerUuid, previousStack, currentStack) -> {
            logger.info("[" + this.mob + "] " + hand.name() + " hand stack changed: " + playerUuid + ", from: " + previousStack + ", to: " + currentStack);

            if(currentStack != null && currentStack.isItemEqual(this.reactionItemStack)) {
                logger.info("[" + this.mob + "] Perform follow on: " + playerUuid);
                playerMemory.put(playerUuid, currentStack);
            }

            if(previousStack != null && previousStack.isItemEqual(this.reactionItemStack)
                && currentStack != null && !currentStack.isItemEqual(this.reactionItemStack)) {
                logger.info("[" + this.mob + "] Stop follow on: " + playerUuid);
                playerMemory.remove(playerUuid);
            }
        });

        this.onLivingEntityDeadHandlerId = OnLivingEntityDeadEventRegistry.INSTANCE.registerOnLivingEntityDeadHandler((world, livingEntity, damageSource) -> {
            if(this.mob.getEntityId() == livingEntity.getEntityId()) {
                logger.info("[" + this.mob + "] Unregister goal from events");
                OnHandStackChangedEventRegistry.INSTANCE.unregisterOnHandStackChangedHandler(onHandStackChangedHandlerId);
                OnLivingEntityDeadEventRegistry.INSTANCE.unregisterOnLivingEntityDeadHandler(onLivingEntityDeadHandlerId);
                playerMemory.clear();
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
            .filter((playerDistancePair) -> playerDistancePair.getRight() <= maximalFollowDistance)
            .min(Comparator.comparing(Pair::getRight))
            .ifPresent((playerDistancePair) -> this.targetEntity = playerDistancePair.getLeft());
    }
}
