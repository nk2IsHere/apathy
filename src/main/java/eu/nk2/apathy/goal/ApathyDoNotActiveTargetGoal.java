package eu.nk2.apathy.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;

public class ApathyDoNotActiveTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {

    public ApathyDoNotActiveTargetGoal(
        MobEntity mob,
        Class<T> targetClass,
        int reciprocalChance,
        boolean checkVisibility,
        boolean checkCanNavigate,
        TargetPredicate targetPredicate
    ) {
        super(
            mob,
            targetClass,
            reciprocalChance,
            checkVisibility,
            checkCanNavigate,
            null
        );
        this.targetPredicate = targetPredicate;
    }

    @Override
    public boolean canStart() {
        return false;
    }
}
