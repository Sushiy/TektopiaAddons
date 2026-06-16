package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;

public class SaplingOreScanner extends OreDictScanner {
    public SaplingOreScanner(Village v, int scansPerTick) {
        super("treeSapling", v, scansPerTick);
    }

    public BlockPos testBlock(World w, BlockPos bp) {
        IBlockState blockState = w.getBlockState(bp);
        return isSapling(blockState) ? bp : null;
    }

    public void scanNearby(BlockPos bp) {
        for(int dy = -2; dy <= 2; dy++) {
            for(int dx = -7; dx <= 7; dx++) {
                for(int dz = -7; dz <= 7; dz++) {
                    if (dx != 0 || dz != 0) {
                        BlockPos scanPos = bp.add(dx, dy, dz);
                        this.scanBlock(scanPos);
                    }
                }
            }
        }

    }

    public static boolean isSapling(IBlockState blockState) {
        return blockState.getBlock() == Blocks.SAPLING;
    }
}
