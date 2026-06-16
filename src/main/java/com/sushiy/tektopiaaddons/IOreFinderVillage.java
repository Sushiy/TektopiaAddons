package com.sushiy.tektopiaaddons;

import net.minecraft.util.math.BlockPos;

public interface IOreFinderVillage {
    boolean hasOreDict(String oreName);
    BlockPos requestOreDict(String oreName);
    void releaseOreDictClaim(String oreName, BlockPos pos);
}
