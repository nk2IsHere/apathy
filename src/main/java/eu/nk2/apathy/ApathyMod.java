package eu.nk2.apathy;

import eu.nk2.apathy.context.ApathyConfig;
import eu.nk2.apathy.context.ApathyConfigLoader;
import eu.nk2.apathy.context.ApathyMixinEntityTypeAccessor;
import eu.nk2.apathy.goal.ApathyDoNotActiveTargetGoal;
import eu.nk2.apathy.goal.ApathyIfBlockBrokenActiveTargetGoal;
import eu.nk2.apathy.goal.ApathyIfItemSelectedActiveTargetGoal;
import eu.nk2.apathy.mixin.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ApathyMod implements ModInitializer {

    private final Logger logger = LogManager.getLogger("Apathy");

    private EntityType.EntityFactory<Entity> getEntityFactory(EntityType<Entity> entityType) {
        try {
            return ((ApathyMixinEntityTypeAccessor) entityType).getFactory();
        } catch (Exception e) {
            logger.error(
                "Getting factory for {}: ",
                entityType,
                e
            );
            return null;
        }
    }

    private void setEntityFactory(
        EntityType<Entity> entityType,
        EntityType.EntityFactory<Entity> entityFactory
    ) {
        try {
            ((ApathyMixinEntityTypeAccessor) entityType).apathy$setCustomFactory(entityFactory);
        } catch (Exception e) {
            logger.error(
                "Setting factory for {}: ",
                entityType,
                e
            );
        }
    }

    private enum GoalSelectorFieldName {
        GOAL_SELECTOR,
        TARGET_SELECTOR
    }

    private Set<Goal> getGoalSetFromMobEntitySelector(
        MobEntity mobEntity,
        GoalSelectorFieldName goalSelectorFieldName
    ) {
        try {
            GoalSelector goalSelector = switch (goalSelectorFieldName) {
                case GOAL_SELECTOR -> ((ApathyMixinMobEntityAccessor) mobEntity).getGoalSelector();
                case TARGET_SELECTOR -> ((ApathyMixinMobEntityAccessor) mobEntity).getTargetSelector();
            };

            return ((ApathyMixinGoalSelectorAccessor) goalSelector).getGoals();
        } catch (Exception e) {
            logger.error(
                "Getting {} for {}: ",
                mobEntity,
                e
            );
            return Collections.emptySet();
        }
    }

    private boolean isPlayerActiveTargetGoal(ActiveTargetGoal<?> followTargetGoal) {
        try {
            Class<?> targetClass = ((ApathyMixinActiveTargetGoalAccessor) followTargetGoal).getTargetClass();
            return targetClass.equals(PlayerEntity.class);
        } catch (Exception e) {
            logger.error(
                "For {}: ",
                followTargetGoal,
                e
            );
            return false;
        }
    }

    private void updateMobSetApathyGoal(
        MobEntity entity,
        Set<Goal> targetSelectorSet,
        int priority,
        ActiveTargetGoal<PlayerEntity> followTargetGoal,
        ApathyConfig.ApathyBehaviourType apathyBehaviourType
    ) {
        ApathyMixinActiveTargetGoalAccessor followTargetGoalAccessor = (ApathyMixinActiveTargetGoalAccessor) followTargetGoal;
        ApathyMixinTrackTargetGoalAccessor trackTargetGoalAccessor = (ApathyMixinTrackTargetGoalAccessor) followTargetGoal;

        logger.debug(
            "[{}] Applied {}",
            entity,
            apathyBehaviourType
        );
        if (apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourDoNotFollowType doNotFollowType) {
            targetSelectorSet.add(new PrioritizedGoal(
                priority,
                new ApathyDoNotActiveTargetGoal<>(
                    entity,
                    PlayerEntity.class,
                    followTargetGoalAccessor.getReciprocalChance(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    followTargetGoalAccessor.getTargetPredicate()
                )
            ));
        } else if (apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfBlockBrokenType ifBlockBrokenBehaviour) {
            targetSelectorSet.add(new PrioritizedGoal(
                priority,
                new ApathyIfBlockBrokenActiveTargetGoal(
                    entity,
                    followTargetGoalAccessor.getReciprocalChance(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    followTargetGoalAccessor.getTargetPredicate(),
                    ifBlockBrokenBehaviour.maximalReactionDistance(),
                    ifBlockBrokenBehaviour.reactionBlock()
                )
            ));
        } else if (apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfItemSelectedType ifItemSelectedBehaviour) {
            targetSelectorSet.add(new PrioritizedGoal(
                priority,
                new ApathyIfItemSelectedActiveTargetGoal(
                    entity,
                    followTargetGoalAccessor.getReciprocalChance(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    trackTargetGoalAccessor.getCheckVisibility(),
                    followTargetGoalAccessor.getTargetPredicate(),
                    ifItemSelectedBehaviour.maximalReactionDistance(),
                    ifItemSelectedBehaviour.reactionItem(),
                    ifItemSelectedBehaviour.reactionItemCount()
                )
            ));
        }
    }

    private void updateMobSetApathyGoals(
        ApathyConfig apathyConfig,
        MobEntity entity,
        Set<Goal> targetSelectorSet,
        int priority,
        ActiveTargetGoal<PlayerEntity> followTargetGoal
    ) {
        List<ApathyConfig.ApathyBehaviourType> defaultApathyBehaviourType = apathyConfig
            .apathyBehaviourTypeMap()
            .getOrDefault(
                null,
                new ArrayList<>()
            );
        List<ApathyConfig.ApathyBehaviourType> mobSpecificApathyBehaviourType = apathyConfig
            .apathyBehaviourTypeMap()
            .get(Registries.ENTITY_TYPE.getId(entity.getType()));

        if (mobSpecificApathyBehaviourType == null) {
            defaultApathyBehaviourType.forEach(apathyBehaviourType -> updateMobSetApathyGoal(
                entity,
                targetSelectorSet,
                priority,
                followTargetGoal,
                apathyBehaviourType
            ));
        } else {
            mobSpecificApathyBehaviourType.forEach(apathyBehaviourType -> updateMobSetApathyGoal(
                entity,
                targetSelectorSet,
                priority,
                followTargetGoal,
                apathyBehaviourType
            ));
        }
    }

    @Override
    public void onInitialize() {
        ApathyConfig apathyConfig;
        try {
            apathyConfig = ApathyConfigLoader.loadConfig();
        } catch (IOException e) {
            apathyConfig = new ApathyConfig(new HashMap<>());
        }

        logger.info(
            "Loaded with config: {}",
            apathyConfig
        );
        final var finalApathyConfig = Objects.requireNonNull(apathyConfig);

        Registries.ENTITY_TYPE
            .streamEntries()
            .map(RegistryEntry.Reference::value)
            .map(entityType -> new Pair<>(
                (EntityType<Entity>) entityType,
                getEntityFactory((EntityType<Entity>) entityType)
            ))
            .forEach(entityTypeToFactory -> setEntityFactory(
                entityTypeToFactory.getLeft(),
                (type, world) -> {
                    Entity entity;
                    try {
                        entity = entityTypeToFactory
                            .getRight()
                            .create(type, world);
                    } catch (Exception e) {
                        logger.error(
                            "Creating entity for {}: ",
                            entityTypeToFactory.getLeft(),
                            e
                        );
                        return null;
                    }

                    if (!(entity instanceof MobEntity mobEntity)) {
                        return entity;
                    }

                    var targetSelectorSet = getGoalSetFromMobEntitySelector(mobEntity, GoalSelectorFieldName.TARGET_SELECTOR);

                    if (targetSelectorSet == null) {
                        return entity;
                    }

                    targetSelectorSet
                        .stream()
                        .map(PrioritizedGoal.class::cast)
                        .filter(goal -> {
                            if (goal == null) {
                                return false;
                            }

                            var goalInstance = goal.getGoal();
                            return goalInstance instanceof ActiveTargetGoal<?> activeTargetGoal
                                && isPlayerActiveTargetGoal(activeTargetGoal);
                        })
                        .forEach((PrioritizedGoal goal) -> {
                            var followTargetGoal = (ActiveTargetGoal<PlayerEntity>) goal.getGoal();
                            targetSelectorSet.remove(goal);
                            updateMobSetApathyGoals(
                                finalApathyConfig,
                                mobEntity,
                                targetSelectorSet,
                                goal.getPriority(),
                                followTargetGoal
                            );
                        });

                    return entity;
                }
            ));
    }
}
