package eu.nk2.apathy.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.mob.MobEntity;

public class ApathyDoNotFollowTargetGoal<T extends LivingEntity> extends FollowTargetGoal<T> {

    public ApathyDoNotFollowTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, TargetPredicate targetPredicate) {
        super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, null);
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean canStart() {
        return false;
    }
}
