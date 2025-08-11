package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.tangotek.tektopia.entities.*;
import net.tangotek.tektopia.generation.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = TekStructureVillagePieces.class)
public abstract class TekStructureVillagePiecesMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static void replaceVillagers(World world, Random ran, double x1, double y1, double z1, double x2, double y2, double z2) {
        for(EntityVillager vil : world.getEntitiesWithinAABB(EntityVillager.class, new AxisAlignedBB(x1, y1, z1, x2, y2, z2), v -> !v.isDead)) {
            EntityVillagerTek newVillager = TekStructureVillagePieces.generateVillager(world, ran);
            newVillager.copyLocationAndAnglesFrom(vil);
            newVillager.onInitialSpawn(world.getDifficultyForLocation(newVillager.getPosition()), (IEntityLivingData)null);
            world.spawnEntity(newVillager);
            vil.setDead();
        }
    }
}
