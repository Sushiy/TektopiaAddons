package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.IOreFinderVillage;
import com.sushiy.tektopiaaddons.oredictfinder.TreeOreScanner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityLumberjack;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIChopTree;
import net.tangotek.tektopia.entities.ai.EntityAIMoveToBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(value = EntityAIChopTree.class)
public abstract class EntityAIChopTreeMixin extends EntityAIMoveToBlock {

    public EntityAIChopTreeMixin(EntityVillageNavigator v) {
        super(v);
    }

    @Shadow(remap = false)
    private @Final EntityVillagerTek villager;
    @Shadow(remap = false)
    private BlockPlanks.EnumType treeType;
    @Shadow(remap = false)
    private BlockPos treePos;
    @Shadow(remap = false)
    private ItemStack bestAxe;
    @Shadow(remap = false)
    private void releaseTreeClaim(){}
    @Shadow(remap = false)
    private boolean logDropCheck(int skill) {
        return false;
    }
    @Shadow(remap = false)
    private boolean chopLog(BlockPos pos, boolean dropBlock, boolean adjacentLeafCheck) {
        return false;
    }

    protected BlockPos getDestinationBlock() {
        if (this.villager.getVillage() != null) {
            this.releaseTreeClaim();
            BlockPos treePos = ((IOreFinderVillage)this.villager.getVillage()).requestOreDict("logWood");
            if (treePos != null) {
                IBlockState blockState = this.villager.world.getBlockState(treePos);
                String filterName;
                if (blockState.getBlock() == Blocks.LOG) {
                    filterName = "chop_tree_" + blockState.getValue(BlockOldLog.VARIANT).getName();
                }
                else {
                    filterName = "chop_tree_" + blockState.getValue(BlockNewLog.VARIANT).getName();
                }

                if (this.villager.isAIFilterEnabled(filterName)) {
                    return treePos;
                }
            }
        }

        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private void chopTree() {
        int logsChopped = 0;
        IBlockState blockState = this.villager.world.getBlockState(this.treePos);

        if (TreeOreScanner.isLog(blockState)) {
            if (blockState.getBlock() == Blocks.LOG) {
                this.treeType = blockState.getValue(BlockOldLog.VARIANT);
            }
            else {
                this.treeType = blockState.getValue(BlockNewLog.VARIANT);
            }

            logsChopped = this.chopTreeUniversal(this.treePos);

            if (logsChopped > 0) {
                this.villager.throttledSadness(-2);
                this.villager.tryAddSkill(ProfessionType.LUMBERJACK, 7);
                this.villager.modifyHunger(-logsChopped / 2);
                this.villager.debugOut("ChopTree [ " + this.treePos.getX() + ", " + this.treePos.getZ() + "] Chopped: " + logsChopped);
            }
        }

    }

    private static final BlockPos[][] DIR_4 = {
            {new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0)},
            {new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)}
    };

    private static final BlockPos[][] DIR_8 = {
            {new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0)},
            {new BlockPos(0, 0, -1), new BlockPos(0, 0, 1)},
            {new BlockPos(-1, 0, -1), new BlockPos(1, 0, 1)},
            {new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1)}
    };

    private int chopTreeUniversal(BlockPos treePos) {
        int count = 0;
        Block originalLog = this.villager.world.getBlockState(treePos).getBlock();
        int currentY = treePos.getY();
        Map<Integer, Set<BlockPos>> logsByLayer = new TreeMap<>();
        Set<BlockPos> currentLayerLogs = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> confirmed = new HashSet<>();
        Queue<BlockPos> currentLayerQueue = new ArrayDeque<>();
        currentLayerQueue.add(treePos);
        confirmed.add(treePos);
        visited.add(treePos);
        currentLayerLogs.add(treePos);
        while (!currentLayerQueue.isEmpty()) {
            BlockPos current = currentLayerQueue.poll();
            for (BlockPos[] dirs : DIR_4) {
                for (BlockPos dir : dirs) {
                    BlockPos neighbor = current.add(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        if ((confirmed.contains(neighbor) || this.villager.world.getBlockState(neighbor).getBlock() == originalLog) && confirmLogLine(neighbor, originalLog, visited, confirmed)) {
                            currentLayerQueue.add(neighbor);
                            confirmed.add(neighbor);
                            currentLayerLogs.add(neighbor);
                        }
                    }
                }
            }
        }
        logsByLayer.put(currentY, currentLayerLogs);
        currentY++;

        Set<BlockPos> nextLayerLogs = new HashSet<>();
        for (BlockPos logPos : currentLayerLogs) {
            BlockPos above = logPos.up();
            if (confirmed.contains(above) || this.villager.world.getBlockState(above).getBlock() == originalLog) {
                currentLayerQueue.add(above);
                confirmed.add(above);
                visited.add(above);
                nextLayerLogs.add(above);
            }
            else {
                for (BlockPos[] dirs : DIR_8) {
                    for (BlockPos dir : dirs) {
                        BlockPos neighbor = above.add(dir);
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            if ((confirmed.contains(neighbor) || this.villager.world.getBlockState(neighbor).getBlock() == originalLog) && !confirmed.contains(neighbor.down()) && this.villager.world.getBlockState(neighbor.down()).getBlock() != originalLog && confirmLogLine(neighbor, originalLog, visited, confirmed)) {
                                currentLayerQueue.add(neighbor);
                                confirmed.add(neighbor);
                                nextLayerLogs.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        currentLayerLogs = nextLayerLogs;
        while (!currentLayerQueue.isEmpty()) {
            BlockPos current = currentLayerQueue.poll();
            for (BlockPos[] dirs : DIR_8) {
                for (BlockPos dir : dirs) {
                    BlockPos neighbor = current.add(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        if ((confirmed.contains(neighbor) || this.villager.world.getBlockState(neighbor).getBlock() == originalLog) && !confirmed.contains(neighbor.down()) && this.villager.world.getBlockState(neighbor.down()).getBlock() != originalLog && confirmLogLine(neighbor, originalLog, visited, confirmed)) {
                            currentLayerQueue.add(neighbor);
                            confirmed.add(neighbor);
                            currentLayerLogs.add(neighbor);
                        }
                    }
                }
            }
            logsByLayer.put(currentY, currentLayerLogs);
            currentY++;

            nextLayerLogs = new HashSet<>();
            for (BlockPos logPos : currentLayerLogs) {
                BlockPos above = logPos.up();
                if (confirmed.contains(above) || this.villager.world.getBlockState(above).getBlock() == originalLog) {
                    currentLayerQueue.add(above);
                    confirmed.add(above);
                    visited.add(above);
                    nextLayerLogs.add(above);
                }
                else {
                    for (BlockPos[] dirs : DIR_8) {
                        for (BlockPos dir : dirs) {
                            BlockPos neighbor = above.add(dir);
                            if (!visited.contains(neighbor)) {
                                visited.add(neighbor);
                                if ((confirmed.contains(neighbor) || this.villager.world.getBlockState(neighbor).getBlock() == originalLog) && !confirmed.contains(neighbor.down()) && this.villager.world.getBlockState(neighbor.down()).getBlock() != originalLog && confirmLogLine(neighbor, originalLog, visited, confirmed)) {
                                    currentLayerQueue.add(neighbor);
                                    confirmed.add(neighbor);
                                    nextLayerLogs.add(neighbor);
                                }
                            }
                        }
                    }
                }
            }
            currentLayerLogs = nextLayerLogs;
        }

        int skill = this.villager.getSkill(ProfessionType.LUMBERJACK);
        for (Integer layerY : logsByLayer.keySet()) {
            for (BlockPos logPos : logsByLayer.get(layerY)) {
                if (this.chopLog(logPos, count == 0 || this.logDropCheck(skill), false)) {
                    count++;
                }
            }
        }

        return count;
    }

    private boolean confirmLogLine(BlockPos neighbor, Block originalLog, Set<BlockPos> visited, Set<BlockPos> confirmed) {
        int horizontalLength = 1;
        for (BlockPos[] dirs : DIR_8) {
            for (BlockPos dir : dirs) {
                BlockPos neighborPos = neighbor;
                while (horizontalLength < 5 && (!visited.contains(neighborPos.add(dir)) || neighborPos.add(dir).equals(this.treePos)) && (confirmed.contains(neighborPos.add(dir)) || this.villager.world.getBlockState(neighborPos.add(dir)).getBlock() == originalLog)) {
                    horizontalLength++;
                    neighborPos = neighborPos.add(dir);
                    confirmed.add(neighborPos);
                }
            }
            horizontalLength = horizontalLength < 5 ? 1 : horizontalLength;
        }
        return horizontalLength < 5;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public boolean shouldExecute() {
        if (this.villager.isAITick() && this.villager.hasVillage() && this.villager.isWorkTime() && ((IOreFinderVillage)this.villager.getVillage()).hasOreDict("logWood")) {
            List<ItemStack> axeList = this.villager.getInventory().getItems(EntityLumberjack.getBestAxe(this.villager), 1);
            if (!axeList.isEmpty()) {
                this.bestAxe = axeList.get(0);
                return super.shouldExecute();
            }

            this.villager.setThought(EntityVillagerTek.VillagerThought.AXE);
            this.bestAxe = null;
        }

        return false;
    }
}
