package eu.nk2.apathy.mixin;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FollowTargetGoal.class)
public interface ApathyMixinFollowTargetGoalAccessor {
    @Accessor("targetClass") Class<?> getTargetClass();
    @Accessor("reciprocalChance") int getReciprocalChance();
    @Accessor("targetPredicate") TargetPredicate getTargetPredicate();
}
