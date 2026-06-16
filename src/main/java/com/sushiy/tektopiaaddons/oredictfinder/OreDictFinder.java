package com.sushiy.tektopiaaddons.oredictfinder;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class OreDictFinder {
    private int debugTick = 100;
    private Map<String, OreDictScanner> scanners = new HashMap<>();

    public void registerOreScanner(OreDictScanner oreDictScanner) {
        this.scanners.put(oreDictScanner.getScanOreDict(), oreDictScanner);
    }

    public boolean hasOreDict(String oreName) {
        OreDictScanner scanner = this.scanners.get(oreName);
        return scanner != null && scanner.hasBlocks();
    }

    public BlockPos requestOreDict(String oreName) {
        OreDictScanner scanner = this.scanners.get(oreName);
        if (scanner != null && scanner.hasBlocks()) {
            return scanner.requestBlock();
        } else {
            return null;
        }
    }

    public void releaseClaim(World world, String oreName, BlockPos bp) {
        OreDictScanner scanner = this.scanners.get(oreName);
        if (scanner != null) {
            scanner.releaseClaim(bp);
        }

    }

    public int getOreDictCount(String s) {
        OreDictScanner scanner = this.scanners.get(s);
        return scanner != null ? scanner.getBlockCount() : 0;
    }

    public void update() {
        this.scanners.forEach((k, v) -> v.update());
    }

    private void debugOut() {
        this.scanners.forEach((k, v) -> System.out.println("    OreDict Finder: [" + k + "]  " + v.getBlockCount()));
    }
}
