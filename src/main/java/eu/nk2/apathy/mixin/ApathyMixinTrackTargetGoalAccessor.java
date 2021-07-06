package eu.nk2.apathy.mixin;

import net.minecraft.entity.ai.goal.TrackTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrackTargetGoal.class)
public interface ApathyMixinTrackTargetGoalAccessor {
    @Accessor("checkVisibility") boolean getCheckVisibility();
    @Accessor("checkCanNavigate") boolean getCheckCanNavigate();
}
