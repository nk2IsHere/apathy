package eu.nk2.apathy;

import eu.nk2.apathy.goal.ApathyFollowTargetGoal;
import eu.nk2.apathy.mixin.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ApathyMod implements ModInitializer {

	private final Logger log = LogManager.getLogger();

	private EntityType.EntityFactory<Entity> getEntityFactory(EntityType<Entity> entityType) {
		try {
			return ((ApathyMixinEntityFactoryAccessor) entityType).getFactory();
		} catch (Exception e) {
			log.error("For " + entityType.toString() + ": ", e);
			return null;
		}
	}

	private EntityType<Entity> setEntityFactory(EntityType<Entity> entityType, EntityType.EntityFactory<Entity> entityFactory) {
		try {
			((ApathyMixinEntityFactoryAccessor) entityType).setFactory(entityFactory);
			return entityType;
		} catch (Exception e) {
			log.error("For " + entityType.toString() + ": ", e);
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
			log.error("For " + mobEntity.toString() + ": ", e);
			return null;
		}
	}

	private boolean isPlayerFollowTargetGoal(FollowTargetGoal<?> followTargetGoal) {
		try {
			Class<?> targetClass = ((ApathyMixinFollowTargetGoalAccessor) followTargetGoal).getTargetClass();
			return targetClass.equals(PlayerEntity.class);
		} catch (Exception e) {
			log.error("For " + followTargetGoal.toString() + ": ", e);
			return false;
		}
	}

	@Override
	public void onInitialize() {
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
								.filter((goal) -> goal.getGoal() instanceof FollowTargetGoal)
								.filter((goal) -> (isPlayerFollowTargetGoal((FollowTargetGoal) goal.getGoal())))
								.collect(Collectors.toList())
								.forEach((PrioritizedGoal goal) -> {
									FollowTargetGoal<PlayerEntity> followTargetGoal = (FollowTargetGoal<PlayerEntity>) goal.getGoal();
									ApathyMixinFollowTargetGoalAccessor followTargetGoalAccessor = (ApathyMixinFollowTargetGoalAccessor) followTargetGoal;
									ApathyMixinTrackTargetGoalAccessor trackTargetGoalAccessor = (ApathyMixinTrackTargetGoalAccessor) followTargetGoal;

									targetSelectorSet.remove(goal);
									targetSelectorSet.add(new PrioritizedGoal(
										goal.getPriority(),
										new ApathyFollowTargetGoal<>(
											(MobEntity) entity,
											PlayerEntity.class,
											followTargetGoalAccessor.getReciprocalChance(),
											trackTargetGoalAccessor.getCheckVisibility(),
											trackTargetGoalAccessor.getCheckVisibility(),
											followTargetGoalAccessor.getTargetPredicate()
										)
									));
								});
					}

					return entity;
				}))
				.forEach((entityType) -> {});
	}
}
