package eu.nk2.apathy.context;

import net.minecraft.item.ItemStack;

import java.util.UUID;

@FunctionalInterface
public interface OnHandStackChangedEventHandler {
    enum Hand {
        MAIN,
        OFF
    }

    void onHandStackChanged(Hand hand, UUID playerUuid, ItemStack previousStack, ItemStack currentStack);
}
