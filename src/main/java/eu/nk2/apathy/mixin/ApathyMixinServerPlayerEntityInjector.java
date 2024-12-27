package eu.nk2.apathy.mixin;

import eu.nk2.apathy.context.OnHandStackChangedEventHandler;
import eu.nk2.apathy.context.OnHandStackChangedEventRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ApathyMixinServerPlayerEntityInjector {

    @Unique
    private ItemStack previousMainhandEquippedStack;
    @Unique
    private ItemStack previousOffhandEquippedStack;

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ServerPlayerEntity playerEntity = (ServerPlayerEntity) (Object) this;
        ItemStack currentMainhandEquippedStack = playerEntity.getEquippedStack(EquipmentSlot.MAINHAND);
        ItemStack currentOffhandEquippedStack = playerEntity.getEquippedStack(EquipmentSlot.OFFHAND);

        if(previousMainhandEquippedStack == null && currentMainhandEquippedStack != null
            || previousMainhandEquippedStack != null && !ItemStack.areEqual(previousMainhandEquippedStack, currentMainhandEquippedStack)) {
            OnHandStackChangedEventRegistry.INSTANCE.publishOnHandStackChangedEvent(
                OnHandStackChangedEventHandler.Hand.MAIN,
                playerEntity,
                previousMainhandEquippedStack,
                currentMainhandEquippedStack
            );
        }
        previousMainhandEquippedStack = currentMainhandEquippedStack;

        if(previousOffhandEquippedStack == null && currentOffhandEquippedStack != null
            || previousOffhandEquippedStack != null && !ItemStack.areEqual(previousOffhandEquippedStack, currentOffhandEquippedStack)) {
            OnHandStackChangedEventRegistry.INSTANCE.publishOnHandStackChangedEvent(
                OnHandStackChangedEventHandler.Hand.OFF,
                playerEntity,
                previousOffhandEquippedStack,
                currentOffhandEquippedStack
            );
        }
        previousOffhandEquippedStack = currentOffhandEquippedStack;
    }
}
