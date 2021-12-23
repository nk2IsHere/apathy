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
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ApathyMod implements ModInitializer {

	private final Logger logger = LogManager.getLogger("Apathy");

	private EntityType.EntityFactory<Entity> getEntityFactory(EntityType<Entity> entityType) {
		try {
			return ((ApathyMixinEntityTypeAccessor) entityType).getFactory();
		} catch (Exception e) {
			logger.error("For " + entityType.toString() + ": ", e);
			return null;
		}
	}

	private EntityType<Entity> setEntityFactory(EntityType<Entity> entityType, EntityType.EntityFactory<Entity> entityFactory) {
		try {
			((ApathyMixinEntityTypeAccessor) entityType).setCustomFactory(entityFactory);
			return entityType;
		} catch (Exception e) {
			logger.error("For " + entityType.toString() + ": ", e);
			return null;
		}
	}

	private enum GoalSelectorFieldName {
		GOAL_SELECTOR,
		TARGET_SELECTOR
	}
	private Set<Goal> getGoalSetFromMobEntitySelector(MobEntity mobEntity, GoalSelectorFieldName goalSelectorFieldName) {
		try {
			GoalSelector goalSelector = null;
			switch (goalSelectorFieldName) {
				case GOAL_SELECTOR:
					goalSelector = ((ApathyMixinMobEntityAccessor) mobEntity).getGoalSelector();
					break;
				case TARGET_SELECTOR:
					goalSelector = ((ApathyMixinMobEntityAccessor) mobEntity).getTargetSelector();
					break;
			}

			return ((ApathyMixinGoalSelectorAccessor) goalSelector).getGoals();
		} catch (Exception e) {
			logger.error("For " + mobEntity.toString() + ": ", e);
			return null;
		}
	}

	private boolean isPlayerActiveTargetGoal(ActiveTargetGoal<?> followTargetGoal) {
		try {
			Class<?> targetClass = ((ApathyMixinActiveTargetGoalAccessor) followTargetGoal).getTargetClass();
			return targetClass.equals(PlayerEntity.class);
		} catch (Exception e) {
			logger.error("For " + followTargetGoal.toString() + ": ", e);
			return false;
		}
	}

	private void updateMobSetApathyGoal(MobEntity entity, Set<Goal> targetSelectorSet, int priority, ActiveTargetGoal<PlayerEntity> followTargetGoal, ApathyConfig.ApathyBehaviourType apathyBehaviourType) {
		ApathyMixinActiveTargetGoalAccessor followTargetGoalAccessor = (ApathyMixinActiveTargetGoalAccessor) followTargetGoal;
		ApathyMixinTrackTargetGoalAccessor trackTargetGoalAccessor = (ApathyMixinTrackTargetGoalAccessor) followTargetGoal;

		logger.debug("[" + entity + "] Applied " + apathyBehaviourType);
		if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourDoNotFollowType) {
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
		} else if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfBlockBrokenType) {
			ApathyConfig.ApathyBehaviourIfBlockBrokenType ifBlockBrokenBehaviour = (ApathyConfig.ApathyBehaviourIfBlockBrokenType) apathyBehaviourType;
			targetSelectorSet.add(new PrioritizedGoal(
				priority,
				new ApathyIfBlockBrokenActiveTargetGoal(
					entity,
					followTargetGoalAccessor.getReciprocalChance(),
					trackTargetGoalAccessor.getCheckVisibility(),
					trackTargetGoalAccessor.getCheckVisibility(),
					followTargetGoalAccessor.getTargetPredicate(),
					ifBlockBrokenBehaviour.getMaximalReactionDistance(),
					ifBlockBrokenBehaviour.getReactionBlock()
				)
			));
		} else if(apathyBehaviourType instanceof ApathyConfig.ApathyBehaviourIfItemSelectedType) {
			ApathyConfig.ApathyBehaviourIfItemSelectedType ifItemSelectedBehaviour = (ApathyConfig.ApathyBehaviourIfItemSelectedType) apathyBehaviourType;
			targetSelectorSet.add(new PrioritizedGoal(
				priority,
				new ApathyIfItemSelectedActiveTargetGoal(
					entity,
					followTargetGoalAccessor.getReciprocalChance(),
					trackTargetGoalAccessor.getCheckVisibility(),
					trackTargetGoalAccessor.getCheckVisibility(),
					followTargetGoalAccessor.getTargetPredicate(),
					ifItemSelectedBehaviour.getMaximalReactionDistance(),
					ifItemSelectedBehaviour.getReactionItem(),
					ifItemSelectedBehaviour.getReactionItemCount()
				)
			));
		}
	}

	private void updateMobSetApathyGoals(ApathyConfig apathyConfig, MobEntity entity, Set<Goal> targetSelectorSet, int priority, ActiveTargetGoal<PlayerEntity> followTargetGoal) {
		List<ApathyConfig.ApathyBehaviourType> defaultApathyBehaviourType = apathyConfig.getApathyBehaviourTypeMap().getOrDefault(null, new ArrayList<>());
		List<ApathyConfig.ApathyBehaviourType> mobSpecificApathyBehaviourType = apathyConfig.getApathyBehaviourTypeMap().get(Registry.ENTITY_TYPE.getId(entity.getType()));

		if(mobSpecificApathyBehaviourType == null) {
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

		logger.info("Loaded with config: " + apathyConfig);

		ApathyConfig finalApathyConfig = apathyConfig;
		Registry.ENTITY_TYPE.getEntries()
				.stream()
				.map(Map.Entry::getValue)
				.map((entityType) -> new Pair<>((EntityType<Entity>) entityType, getEntityFactory((EntityType<Entity>) entityType)))
				.map((entityTypeToFactory) -> setEntityFactory(entityTypeToFactory.getLeft(), (type, world) -> {
					Entity entity = entityTypeToFactory.getRight().create(type, world);

					if (entity instanceof MobEntity) {
						Set<Goal> targetSelectorSet = getGoalSetFromMobEntitySelector((MobEntity) entity, GoalSelectorFieldName.TARGET_SELECTOR);
						if(targetSelectorSet != null)
							targetSelectorSet.stream()
								.map((goal) -> (PrioritizedGoal) goal)
								.filter((goal) -> goal.getGoal() instanceof ActiveTargetGoal)
								.filter((goal) -> (isPlayerActiveTargetGoal((ActiveTargetGoal) goal.getGoal())))
								.collect(Collectors.toList())
								.forEach((PrioritizedGoal goal) -> {
									ActiveTargetGoal<PlayerEntity> followTargetGoal = (ActiveTargetGoal<PlayerEntity>) goal.getGoal();

									targetSelectorSet.remove(goal);
									updateMobSetApathyGoals(finalApathyConfig, (MobEntity) entity, targetSelectorSet, goal.getPriority(), followTargetGoal);
								});
					}

					return entity;
				}))
				.forEach((entityType) -> {});
	}
}
