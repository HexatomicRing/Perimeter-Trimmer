package com.hexatimic_ring.perimeter_trimmer.mixin;

import com.hexatimic_ring.perimeter_trimmer.utils.BeaconBuildController;
import com.hexatimic_ring.perimeter_trimmer.utils.BreakingFlowController;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(MinecraftClient.class)
public abstract class ClientWorldMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void checkTask(CallbackInfo info){
        try{

        }catch(Exception ignored){}

    }@Shadow
    @Nullable
    public ClientWorld world;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "doItemUse", at = @At(value = "HEAD"))
    private void onInitComplete(CallbackInfo ci) {
        if (this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
            if ((world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.BEDROCK) && player.getMainHandStack().isEmpty()) || BreakingFlowController.isWorking()) {
                BreakingFlowController.switchOnOff();
            }
            if((world.getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.BEACON) &&
                    (player.getMainHandStack().isOf(Items.IRON_BLOCK)||player.getMainHandStack().isOf(Items.GOLD_BLOCK)||player.getMainHandStack().isOf(Items.EMERALD_BLOCK)||player.getMainHandStack().isOf(Items.DIAMOND_BLOCK)||player.getMainHandStack().isOf(Items.NETHERITE_BLOCK)))){
                BeaconBuildController.addTask(world,blockHitResult.getBlockPos(),player.getMainHandStack());
            }
        }

    }



}
