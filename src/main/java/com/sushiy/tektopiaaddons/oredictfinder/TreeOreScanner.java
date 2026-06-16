package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.tangotek.tektopia.Village;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TreeOreScanner extends OreDictScanner {
    public TreeOreScanner(Village v, int scansPerTick) {
        super("logWood", v, scansPerTick);
    }

    public BlockPos testBlock(World w, BlockPos bp) {
        IBlockState blockState = w.getBlockState(bp);
        return isLeaf(blockState) ? this.findTreeFromLeaf(w, bp) : null;
    }

    public void scanNearby(BlockPos bp) {
        for(int dy = 2; dy <= 10; dy += 2) {
            for(int dx = -9; dx <= 9; dx += 3) {
                for(int dz = -9; dz <= 9; dz += 3) {
                    if (dx != 0 || dz != 0) {
                        BlockPos scanPos = bp.add(dx, dy, dz);
                        this.scanBlock(scanPos);
                    }
                }
            }
        }

    }

    @Nullable
    protected BlockPos findTreeFromLeaf(World world, BlockPos leafPos) {
        return treeTest(world, leafPos.down());
    }

    public static BlockPos treeTest(World world, BlockPos bp) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(bp);
        boolean found = false;
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            visited.add(current);
            if (!isLog(world.getBlockState(current))) {
                if (found && world.getBlockState(current).getBlock() == Blocks.DIRT) {
                    return current.up();
                }
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                            BlockPos testPos = current.add(dx, 0, dz);
                            if (!visited.contains(testPos) && isLog(world.getBlockState(testPos))) {
                                queue.add(testPos.down());
                                visited.add(testPos);
                                found = true;
                            }
                        }
                    }
                }
            }
            else {
                queue.add(current.down());
                found = true;
            }
        }

        return null;
    }

    public static boolean isLog(IBlockState blockState) {
        return blockState.getBlock() == Blocks.LOG || blockState.getBlock() == Blocks.LOG2;
    }

    public static boolean isLeaf(IBlockState blockState) {
        return (blockState.getBlock() == Blocks.LEAVES || blockState.getBlock() == Blocks.LEAVES2) && (Boolean)blockState.getValue(BlockLeaves.DECAYABLE);
    }
}
