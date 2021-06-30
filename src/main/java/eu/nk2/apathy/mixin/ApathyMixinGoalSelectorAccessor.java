package eu.nk2.apathy.mixin;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(GoalSelector.class)
public interface ApathyMixinGoalSelectorAccessor {
    @Accessor("goals") Set<Goal> getGoals();
}
