package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.tangotek.tektopia.Village;

import java.util.*;

public abstract class OreDictScanner {
    protected final String scanOreDict;
    private Random rng = new Random();
    private long tickCount = 0L;
    private long releaseTick = 0L;
    private List<BlockPos> recentBlocks = new LinkedList<>();
    private Map<BlockPos, Long> claimedBlocks = new HashMap<>();
    protected Village village;
    private final int scansPerTick;
    protected Queue<BlockPos> scannedBlocks = new PriorityQueue<>(50, Comparator.comparingInt((a) -> (int)a.distanceSq(this.village.getCenter())));

    public OreDictScanner(String scanOreDict, Village village, int scansPerTick) {
        this.scanOreDict = scanOreDict;
        this.village = village;
        this.scansPerTick = scansPerTick;
    }

    public String getScanOreDict() {
        return this.scanOreDict;
    }

    public void update() {
        ++this.tickCount;
        if (!this.recentBlocks.isEmpty()) {
            BlockPos recent = this.recentBlocks.remove(0);
            this.scanNearby(recent);
        }

        for(int i = 0; i < this.scansPerTick; ++i) {
            this.scanRandomBlock(this.rng.nextFloat());
        }

        if (this.releaseTick-- < 0L) {
            this.releaseTick = 100L;
            this.releaseClaimedBlocks();
        }

    }

    public boolean hasBlocks() {
        return !this.scannedBlocks.isEmpty();
    }

    public int getBlockCount() {
        return this.scannedBlocks.size();
    }

    public BlockPos requestBlock() {
        while(true) {
            if (!this.scannedBlocks.isEmpty()) {
                BlockPos bp = this.scannedBlocks.poll();
                boolean found = false;
                try {
                    int[] oreIds = OreDictionary.getOreIDs(new ItemStack(this.village.getWorld().getBlockState(bp).getBlock()));
                    int oreId = OreDictionary.getOreID(this.scanOreDict);
                    for (int id : oreIds) {
                        if (id == oreId) {
                            found = true;
                            break;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    continue;
                }
                if (!found) {
                    continue;
                }

                this.claimedBlocks.put(bp, this.tickCount);
                return bp;
            }

            return null;
        }
    }

    public void releaseClaim(BlockPos bp) {
        this.claimedBlocks.remove(bp);
    }

    public abstract BlockPos testBlock(World var1, BlockPos var2);

    protected abstract void scanNearby(BlockPos var1);

    protected void scanRandomBlock(float mod) {
        int radius = Math.max((int)((float)this.village.getSize() * mod), 20);
        int vertOffset = (int)(20.0F * mod) + 5;
        int X = this.village.getCenter().getX() + radius - this.rng.nextInt(radius * 2);
        int Y = MathHelper.getInt(this.rng, (int)this.village.getAABB().minY - vertOffset, (int)this.village.getAABB().maxY + vertOffset);
        int Z = this.village.getCenter().getZ() + radius - this.rng.nextInt(radius * 2);
        this.scanBlock(new BlockPos(X, Y, Z));
    }

    protected void scanBlock(BlockPos testPos) {
        if (this.village.isInVillage(testPos)) {
            BlockPos targetPos = this.testBlock(this.village.getWorld(), testPos);
            if (targetPos != null && !this.scannedBlocks.contains(targetPos) && !this.claimedBlocks.containsKey(targetPos)) {
                this.scannedBlocks.add(targetPos);
                this.recentBlocks.add(targetPos);
            }
        }

    }

    protected void releaseClaimedBlocks() {
        Iterator<Map.Entry<BlockPos, Long>> itr = this.claimedBlocks.entrySet().iterator();

        while(itr.hasNext()) {
            Map.Entry<BlockPos, Long> entry = itr.next();
            long timeClaimed = this.tickCount - entry.getValue();
            if (timeClaimed > 2400L) {
                itr.remove();
            }
        }

    }
}
