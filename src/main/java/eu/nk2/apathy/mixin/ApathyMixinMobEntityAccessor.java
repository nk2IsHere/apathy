package eu.nk2.apathy.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEntity.class)
public interface ApathyMixinMobEntityAccessor {
    @Accessor("goalSelector") GoalSelector getGoalSelector();
    @Accessor("targetSelector") GoalSelector getTargetSelector();
}
