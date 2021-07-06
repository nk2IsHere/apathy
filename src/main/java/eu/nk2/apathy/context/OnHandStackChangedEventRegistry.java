package eu.nk2.apathy.context;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public interface OnHandStackChangedEventRegistry {

    void registerOnHandStackChangedHandler(OnHandStackChangedEventHandler handler);
    void publishOnHandStackChangedEvent(OnHandStackChangedEventHandler.Hand hand, PlayerEntity playerEntity, ItemStack previousStack, ItemStack currentStack);

    OnHandStackChangedEventRegistry INSTANCE = new OnHandStackChangedEventRegistry() {

        private final ArrayList<OnHandStackChangedEventHandler> onHandStackChangedHandlers = new ArrayList<>();

        @Override
        public void registerOnHandStackChangedHandler(OnHandStackChangedEventHandler handler) {
            onHandStackChangedHandlers.add(handler);
        }

        @Override
        public void publishOnHandStackChangedEvent(OnHandStackChangedEventHandler.Hand hand, PlayerEntity playerEntity, ItemStack previousStack, ItemStack currentStack) {
            onHandStackChangedHandlers.forEach(handler -> handler.onHandStackChanged(hand, playerEntity, previousStack, currentStack));
        }
    };
}
