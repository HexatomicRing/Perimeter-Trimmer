package com.hexatimic_ring.perimeter_trimmer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class BeaconBuildController {
    private static ArrayList<BlockPos> fillPositions = new ArrayList<>();
    private static Block fillBlock = null;
    public static void tick(){
        if(fillBlock == null) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        ArrayList<BlockPos> removeList = new ArrayList<>();
        ArrayList<BlockPos> finishList = new ArrayList<>();
        for(BlockPos p:fillPositions){
            BlockState blockState = player.world.getBlockState(p);
            double distance = p.getSquaredDistance(player.getPos(), true);
            if(blockState.isOf(Blocks.IRON_BLOCK)||blockState.isOf(Blocks.GOLD_BLOCK)||blockState.isOf(Blocks.EMERALD_BLOCK)||blockState.isOf(Blocks.DIAMOND_BLOCK)||blockState.isOf(Blocks.NETHERITE_BLOCK)){
                finishList.add(p);
            }else if(distance <= 21){
                BlockPlacer.simpleBlockPlacement(p,fillBlock.asItem());
                break;
            }else if(distance > 400){
                removeList.add(p);
            }
        }
        for(BlockPos p:removeList) fillPositions.remove(p);
        for(BlockPos p:finishList) fillPositions.remove(p);
        if(removeList.size() == 0 && finishList.size() > 0 && fillPositions.size() == 0) Messager.chat("perimeter_trimmer.beacon.finish");
        if(removeList.size() > 0 && fillPositions.size() > 0) Messager.chat("perimeter_trimmer.beacon.remove_part");
        if(removeList.size() > 0 && fillPositions.size() == 0) Messager.chat("perimeter_trimmer.beacon.remove");

    }

    public static void addTask(World world, BlockPos beaconPos, ItemStack fillBlockItem){
        try{
            fillBlock = ((BlockItem)fillBlockItem.getItem()).getBlock();
        }catch (Exception e){
            return;
        }
        layer:
        for(int dy = -1;dy > -5;dy--){
            ArrayList<BlockPos> posBuffer = new ArrayList<>();
            for(int dx = dy;dx+dy<1;dx++){
                for(int dz = dy;dz+dy<1;dz++){
                    BlockState blockState = world.getBlockState(beaconPos.add(dx,dy,dz));
                    if(blockState.getMaterial().isReplaceable()){
                        posBuffer.add(beaconPos.add(dx,dy,dz));
                    }else if(!((blockState.isOf(Blocks.IRON_BLOCK)||blockState.isOf(Blocks.GOLD_BLOCK)||blockState.isOf(Blocks.EMERALD_BLOCK)||blockState.isOf(Blocks.DIAMOND_BLOCK)||blockState.isOf(Blocks.NETHERITE_BLOCK)))){
                        break layer;
                    }
                }
            }
            for(BlockPos p:posBuffer) addPos(p);
        }
    }

    private static void addPos(BlockPos pos){
        boolean canAdd = true;
        for(BlockPos p:fillPositions){
            if(p.isWithinDistance(pos,0.0)){
                canAdd = false;
                break;
            }
        }
        if(canAdd) fillPositions.add(pos);
    }
}
