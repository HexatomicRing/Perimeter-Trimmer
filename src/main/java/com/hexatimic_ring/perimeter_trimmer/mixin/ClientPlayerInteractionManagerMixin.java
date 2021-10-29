package com.hexatimic_ring.perimeter_trimmer.mixin;

import com.hexatimic_ring.perimeter_trimmer.utils.BeaconBuildController;
import com.hexatimic_ring.perimeter_trimmer.utils.BreakingFlowController;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if (BreakingFlowController.isWorking()) {
            BreakingFlowController.tick();
            BeaconBuildController.tick();
        }
    }
}
