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

	private enum ApathyModFieldMappings {
		KEY_EntityFactory_factory,
		KEY_MobEntity_goalSelector,
		KEY_MobEntity_targetSelector,
		KEY_GoalSelector_goals,
		KEY_FollowTargetGoal_targetClass;

		private static final boolean DEBUG_MAPPINGS = false;

		private static final Map<String, String> MAPPING_DEBUG = new HashMap<String, String>() {{
			this.put(KEY_EntityFactory_factory.name(), "factory");
			this.put(KEY_MobEntity_goalSelector.name(), "goalSelector");
			this.put(KEY_MobEntity_targetSelector.name(), "targetSelector");
			this.put(KEY_GoalSelector_goals.name(), "goals");
			this.put(KEY_FollowTargetGoal_targetClass.name(), "targetClass");
		}};

		private static final Map<String, String> MAPPING_PRODUCTION = new HashMap<String, String>() {{
			this.put(KEY_EntityFactory_factory.name(), "field_6101");
			this.put(KEY_MobEntity_goalSelector.name(), "field_6201");
			this.put(KEY_MobEntity_targetSelector.name(), "field_6185");
			this.put(KEY_GoalSelector_goals.name(), "field_6461");
			this.put(KEY_FollowTargetGoal_targetClass.name(), "field_6643");
		}};

		public String getMapping() {
			return DEBUG_MAPPINGS?
				MAPPING_DEBUG.get(this.name())
				: MAPPING_PRODUCTION.get(this.name());
		}
	}

	private final Logger log = LogManager.getLogger();

	private EntityType.EntityFactory<Entity> getEntityFactory(EntityType<Entity> entityType) {
		try {
			Field factoryField = entityType.getClass().getDeclaredField(ApathyModFieldMappings.KEY_EntityFactory_factory.getMapping());
			factoryField.setAccessible(true);

			return (EntityType.EntityFactory<Entity>) factoryField.get(entityType);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("For " + entityType.toString() + ": ", e);
			e.printStackTrace();

			return null;
		}
	}

	private EntityType<Entity> setEntityFactory(EntityType<Entity> entityType, EntityType.EntityFactory<Entity> entityFactory) {
		try {
			Field factoryField = entityType.getClass().getDeclaredField(ApathyModFieldMappings.KEY_EntityFactory_factory.getMapping());
			factoryField.setAccessible(true);

			factoryField.set(entityType, entityFactory);
			return entityType;
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("For " + entityType.toString() + ": ", e);
			e.printStackTrace();

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
			Field goalSelectorField = mobEntity.getClass().getField(goalSelectorFieldName.mapping.getMapping());
			goalSelectorField.setAccessible(true);
			GoalSelector goalSelector = (GoalSelector) goalSelectorField.get(mobEntity);

			Field goalSetField = goalSelector.getClass().getDeclaredField(ApathyModFieldMappings.KEY_GoalSelector_goals.getMapping());
			goalSetField.setAccessible(true);
			return (Set<Goal>) goalSetField.get(goalSelector);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("For " + mobEntity.toString() + ": ", e);
			e.printStackTrace();

			return null;
		}
	}

	private boolean isPlayerFollowTargetGoal(FollowTargetGoal<?> followTargetGoal) {
		try {
			Field targetClassField = followTargetGoal.getClass().getField(ApathyModFieldMappings.KEY_FollowTargetGoal_targetClass.getMapping());
			targetClassField.setAccessible(true);
			Class<?> targetClass = (Class<?>) targetClassField.get(followTargetGoal);

			return targetClass.equals(PlayerEntity.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("For " + followTargetGoal.toString() + ": ", e);
			e.printStackTrace();

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
//						Set<Goal> goalSelectorSet = getGoalSetFromMobEntitySelector((MobEntity) entity, GoalSelectorFieldName.GOAL_SELECTOR);

						Set<Goal> targetSelectorSet = getGoalSetFromMobEntitySelector((MobEntity) entity, GoalSelectorFieldName.TARGET_SELECTOR);
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
