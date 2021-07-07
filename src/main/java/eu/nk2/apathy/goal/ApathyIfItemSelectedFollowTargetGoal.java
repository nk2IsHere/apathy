package eu.nk2.apathy.goal;

import eu.nk2.apathy.context.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApathyIfItemSelectedFollowTargetGoal extends FollowTargetGoal<PlayerEntity> {

    private final float maximalReactionDistance;
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
        float maximalReactionDistance,
        ItemStack reactionItemStack
    ) {
        super(mob, PlayerEntity.class, reciprocalChance, checkVisibility, checkCanNavigate, null);
        this.targetPredicate = targetPredicate;
        this.maximalReactionDistance = maximalReactionDistance;
        this.reactionItemStack = reactionItemStack;

        this.onHandStackChangedHandlerId = OnHandStackChangedEventRegistry.INSTANCE.registerOnHandStackChangedHandler((hand, playerUuid, previousStack, currentStack) -> {
            System.out.println("[" + mob + "] " + hand.name() + " hand stack changed: " + playerUuid + ", from: " + previousStack + ", to: " + currentStack);

            if(currentStack != null && currentStack.isItemEqual(this.reactionItemStack)) {
                System.out.println("[" + mob + "] Perform follow on: " + playerUuid);
                playerMemory.put(playerUuid, currentStack);
            }

            if(previousStack != null && previousStack.isItemEqual(this.reactionItemStack)
                && currentStack != null && !currentStack.isItemEqual(this.reactionItemStack)) {
                System.out.println("[" + mob + "] Stop follow on: " + playerUuid);
                playerMemory.remove(playerUuid);
            }
        });

        this.onLivingEntityDeadHandlerId = OnLivingEntityDeadEventRegistry.INSTANCE.registerOnLivingEntityDeadHandler((world, livingEntity, damageSource) -> {
            if(mob.getEntityId() == livingEntity.getEntityId()) {
                System.out.println("[" + mob + "] Unregister goal from events");
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
            .map((player) -> new Pair<>(player, mob.distanceTo(player)))
            .filter((playerDistancePair) -> playerDistancePair.getRight() <= maximalReactionDistance)
            .min(Comparator.comparing(Pair::getRight))
            .ifPresent((playerDistancePair) -> this.targetEntity = playerDistancePair.getLeft());
    }
}
