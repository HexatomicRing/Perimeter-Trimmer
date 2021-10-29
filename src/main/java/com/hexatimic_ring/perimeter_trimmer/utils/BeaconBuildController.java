package com.hexatimic_ring.perimeter_trimmer.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
        for(BlockPos p:fillPositions){
            double distance = p.getSquaredDistance(player.getPos(), true);
            if(distance <= 21){
                BlockPlacer.simpleBlockPlacement(p,fillBlock.asItem());
                break;
            }else if(distance > 400){
                removeList.add(p);
            }
        }
        for(BlockPos p:removeList) fillPositions.remove(p);
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
                    }else{
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
