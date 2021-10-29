package com.hexatimic_ring.perimeter_trimmer.utils;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.world.ClientWorld;

import java.util.ArrayList;

public class BreakingFlowController {
    private static ArrayList<TargetBlock> cachedTargetBlockList = new ArrayList<>();

    public static boolean isWorking() {
        return working > 0;
    }

    private static int working = 0;
    private static long readyTime = 0L;
    private static long checkTime = 0L;

    public static void addBlockPosToList(BlockPos pos) {
        ClientWorld world = MinecraftClient.getInstance().world;

        String haveEnoughItems = InventoryManager.warningMessage();
        if (haveEnoughItems != null) {
            Messager.actionBar(haveEnoughItems);
            return;
        }

        if (shouldAddNewTargetBlock(pos)) cachedTargetBlockList.add(new TargetBlock(pos, world));

    }


    public static void tick() {
        if (InventoryManager.warningMessage() != null) {
            return;
        }
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity player = minecraftClient.player;

        if (!"survival".equals(minecraftClient.interactionManager.getCurrentGameMode().getName())) {
            return;
        }

        int count = 0;
        ClientWorld world = MinecraftClient.getInstance().world;

        int px = (int)player.getPos().x;
        int pz = (int)player.getPos().z;
        int py = (int)player.getPos().y;

        if(working == 1){
            if(player.getPos().y > 64.0){
                working = 0;
                Messager.chat("perimeter_trimmer.force_stop");
                return;
            }
            int dx = 0,dz = 0,dy = 4;
            while(true){
                PosCounter pc = new PosCounter(dx,dy,dz,3,3);
                if(!pc.isHasNext()) break;
                dx = pc.nextX();
                dy = pc.nextY();
                dz = pc.nextZ();

                if (py + dy <= 0) continue;
                assert world != null;
                BlockPos pos = new BlockPos(px + dx, py + dy, pz + dz);
                BlockState blockState = world.getBlockState(pos);
                if (blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
                    if(blockState.getFluidState().getLevel()==8)
                        BlockPlacer.simpleBlockPlacement(pos, Blocks.STONE);
                    continue;
                }
                if (!world.getBlockState(new BlockPos(px + dx, py + dy + 1, pz + dz)).isAir()) continue;
                if (blockState.isOf(Blocks.BEDROCK)) {
                    if (!world.getBlockState(new BlockPos(px + dx, py + dy + 2, pz + dz)).isAir()) continue;
                    if (!world.getBlockState(new BlockPos(px + dx+1, py + dy+1, pz + dz)).isAir()) continue;
                    if (!world.getBlockState(new BlockPos(px + dx-1, py + dy+1, pz + dz)).isAir()) continue;
                    if (!world.getBlockState(new BlockPos(px + dx, py + dy+1, pz + dz+1)).isAir()) continue;
                    if (!world.getBlockState(new BlockPos(px + dx, py + dy+1, pz + dz-1)).isAir()) continue;
                    if(cachedTargetBlockList.size() <= 10){
                        if(cachedTargetBlockList.size()==10)
                            Messager.chat("perimeter_trimmer.warn.full");
                        addBlockPosToList(pos);
                    }
                }else if (!blockState.isAir() && !blockState.isOf(Blocks.PISTON) && !blockState.isOf(Blocks.SLIME_BLOCK) && !blockState.isOf(Blocks.REDSTONE_TORCH)
                        && blockState.getMaterial()!= Material.METAL && !blockState.isOf(Blocks.BEACON)) {
                    if (++count > 4) continue;
                    BlockBreaker.breakBlock(world, pos);
                }
            }

            for (int i = 0; i < cachedTargetBlockList.size(); i++) {
                TargetBlock selectedBlock = cachedTargetBlockList.get(i);

                //玩家切换世界，或离目标方块太远时，删除所有缓存的任务
                if (selectedBlock.getWorld() != MinecraftClient.getInstance().world ) {
                    cachedTargetBlockList = new ArrayList<>();
                    break;
                }

                if (blockInPlayerRange(selectedBlock.getBlockPos(), player, 4.95f)) {

                    TargetBlock.Status status = selectedBlock.tick();
                    if (status == TargetBlock.Status.RETRACTING) {
                        break;
                    } else if (status == TargetBlock.Status.FAILED || status == TargetBlock.Status.RETRACTED) {
                        cachedTargetBlockList.remove(i);
                        break;
                    } else {
                        break;
                    }

                }
            }
        }else if(working == 2){
            if(!cachedTargetBlockList.isEmpty()) cachedTargetBlockList = new ArrayList<TargetBlock>();


            int dx = 0,dz = 0,dy = 5;
            while(true){
                PosCounter pc = new PosCounter(dx,dy,dz,3,4);
                if(!pc.isHasNext()) break;
                dx = pc.nextX();
                dy = pc.nextY();
                dz = pc.nextZ();

                if (py + dy <= 0) continue;
                assert world != null;
                BlockPos pos = new BlockPos(px + dx, py + dy, pz + dz);
                BlockState blockState = world.getBlockState(pos);

                if (blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.WATER)) {
                    if(blockState.getFluidState().getLevel()==8)
                        BlockPlacer.simpleBlockPlacement(pos, Blocks.STONE);
                    continue;
                }
                if (!world.getBlockState(new BlockPos(px + dx, py + dy + 1, pz + dz)).isAir()) continue;
                if (!blockState.isOf(Blocks.BEDROCK) && blockState.getMaterial()!= Material.METAL && !blockState.isOf(Blocks.BEACON)) {
                    BlockBreaker.breakBlock(world, pos);
                }

            }

        }

    }

    private static boolean blockInPlayerRange(BlockPos blockPos, PlayerEntity player, float range) {
        return (blockPos.getSquaredDistance(player.getPos(), true) <= range * range);
    }

    public static WorkingMode getWorkingMode() {
        return WorkingMode.VANILLA;
    }

    private static boolean shouldAddNewTargetBlock(BlockPos pos){
        for (int i = 0; i < cachedTargetBlockList.size(); i++) {
            if (cachedTargetBlockList.get(i).getBlockPos().getSquaredDistance(pos.getX(),pos.getY(),pos.getZ(),false) == 0){
                return false;
            }
        }
        return true;
    }

    public static void switchOnOff(){
        Long currentTime = System.currentTimeMillis();
        if (working == 1){
            Messager.chat("perimeter_trimmer.toggle.toggle");
            working = 2;
        } else if(working == 2) {
            Messager.chat("perimeter_trimmer.toggle.off");
            working = 0;
        } else if(working == 0) {
            if(currentTime - readyTime > 10000){
                Messager.chat("perimeter_trimmer.toggle.check");
                readyTime = currentTime;
            }else if(currentTime - checkTime > 1000){
                checkTime = currentTime;
            }else{
                Messager.chat("perimeter_trimmer.toggle.on");

                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (!minecraftClient.isInSingleplayer()){

                    Messager.chat("perimeter_trimmer.warn.multiplayer");
                }
                working = 1;
            }
        }
    }


    //测试用的。使用原版模式已经足以满足大多数需求。
    //just for test. The VANILLA mode is powerful enough.
    enum WorkingMode {
        CARPET_EXTRA,
        VANILLA,
        MANUALLY;
    }
}
