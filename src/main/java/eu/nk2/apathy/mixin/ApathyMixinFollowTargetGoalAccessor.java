package eu.nk2.apathy.mixin;

import net.minecraft.entity.ai.goal.FollowTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FollowTargetGoal.class)
public interface ApathyMixinFollowTargetGoalAccessor {
    @Accessor("targetClass") Class<?> getTargetClass();
}
