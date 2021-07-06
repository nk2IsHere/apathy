package eu.nk2.apathy.context;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface OnHandStackChangedEventHandler {
    enum Hand {
        MAIN,
        OFF
    }

    void onHandStackChanged(Hand hand, PlayerEntity playerEntity, ItemStack previousStack, ItemStack currentStack);
}
