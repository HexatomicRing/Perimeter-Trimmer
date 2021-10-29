package com.hexatimic_ring.perimeter_trimmer.utils;

public class PosCounter {
    final private int x;
    final private int y;
    final private int z;
    final private boolean hasNext;


    public PosCounter(int px,int py,int pz,int lr,int ly){
        if(py + ly <= 0){
            y = ly;
            int r = Math.max(Math.abs(px),Math.abs(pz));
            if(r == 0){
                x = 0;
                z = -1;
                hasNext = true;
            }else{
                if(pz == -r && px > -r && px < r){
                    z = pz;
                    x = px - 1;
                    hasNext = true;
                }else if(px == -r && pz < r){
                    x = px;
                    z = pz + 1;
                    hasNext = true;
                }else if(pz == r && px < r){
                    z = pz;
                    x = px + 1;
                    hasNext = true;
                }else if(px == r && pz > -r){
                    x = px;
                    z = pz - 1;
                    hasNext = true;
                }else{
                    x = r;
                    z = -r-1;
                    hasNext = r < lr;
                }
            }
        }else{
            y = py - 1;
            x = px;
            z = pz;
            hasNext = true;
        }
    }
    public boolean isHasNext(){
        return hasNext;
    }
    public int nextX(){
        return x;
    }
    public int nextY(){
        return y;
    }
    public int nextZ(){
        return z;
    }
}
