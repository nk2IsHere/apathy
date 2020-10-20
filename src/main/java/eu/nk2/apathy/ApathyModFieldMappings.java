package eu.nk2.apathy;

import java.util.HashMap;
import java.util.Map;

public enum ApathyModFieldMappings {
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