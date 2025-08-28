package com.sushiy.tektopiaaddons.mixin;

import com.pam.harvestcraft.blocks.blocks.BlockBaseGarden;
import net.minecraft.block.BlockBush;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlockBaseGarden.class)
public class BlockBaseGardenMixin extends BlockBush
{

    @Unique
    private static final AxisAlignedBB GARDEN_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return true;
    }
}
