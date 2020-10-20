package eu.nk2.apathy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class ApathyMod implements ModInitializer {

	private final Logger log = LogManager.getLogger();

	private EntityType.EntityFactory<Entity> getEntityFactory(EntityType<Entity> entityType) {
		try {
			Field factoryField = ReflectionUtils.findField(entityType.getClass(), ApathyModFieldMappings.KEY_EntityFactory_factory.getMapping(), null);
			factoryField.setAccessible(true);

			return (EntityType.EntityFactory<Entity>) factoryField.get(entityType);
		} catch (NullPointerException | IllegalAccessException e) {
			log.error("For " + entityType.toString() + ": ", e);
			return null;
		}
	}

	private EntityType<Entity> setEntityFactory(EntityType<Entity> entityType, EntityType.EntityFactory<Entity> entityFactory) {
		try {
			Field factoryField = ReflectionUtils.findField(entityType.getClass(), ApathyModFieldMappings.KEY_EntityFactory_factory.getMapping(), null);
			factoryField.setAccessible(true);

			factoryField.set(entityType, entityFactory);
			return entityType;
		} catch (NullPointerException | IllegalAccessException e) {
			log.error("For " + entityType.toString() + ": ", e);
			return null;
		}
	}

	private enum GoalSelectorFieldName {
		GOAL_SELECTOR(ApathyModFieldMappings.KEY_MobEntity_goalSelector),
		TARGET_SELECTOR(ApathyModFieldMappings.KEY_MobEntity_targetSelector);

		public final ApathyModFieldMappings mapping;

		GoalSelectorFieldName(ApathyModFieldMappings mapping) {
			this.mapping = mapping;
		}
	}
	private Set<Goal> getGoalSetFromMobEntitySelector(MobEntity mobEntity, GoalSelectorFieldName goalSelectorFieldName) {
		try {
			Field goalSelectorField = ReflectionUtils.findField(mobEntity.getClass(), goalSelectorFieldName.mapping.getMapping(), null);
			goalSelectorField.setAccessible(true);
			GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(mobEntity);

			Field goalSetField = ReflectionUtils.findField(goalSelector.getClass(), ApathyModFieldMappings.KEY_GoalSelector_goals.getMapping(), null);
			goalSetField.setAccessible(true);
			return (Set<Goal>) goalSetField.get(goalSelector);
		} catch (NullPointerException | IllegalAccessException e) {
			log.error("For " + mobEntity.toString() + ": ", e);
			return null;
		}
	}

	private boolean isPlayerFollowTargetGoal(FollowTargetGoal<?> followTargetGoal) {
		try {
			Field targetClassField = ReflectionUtils.findField(followTargetGoal.getClass(), ApathyModFieldMappings.KEY_FollowTargetGoal_targetClass.getMapping(), null);
			targetClassField.setAccessible(true);
			Class<?> targetClass = (Class<?>) targetClassField.get(followTargetGoal);

			return targetClass.equals(PlayerEntity.class);
		} catch (NullPointerException | IllegalAccessException e) {
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
							targetSelectorSet.removeAll(
								targetSelectorSet.stream()
									.map((goal) -> (PrioritizedGoal) goal)
									.filter((goal) -> goal.getGoal() instanceof FollowTargetGoal)
									.filter((goal) -> (isPlayerFollowTargetGoal((FollowTargetGoal) goal.getGoal())))
									.collect(Collectors.toList())
							);
					}

					return entity;
				}))
				.forEach((entityType) -> {});
	}
}
