package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.ModBlocks;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIMining;
import net.tangotek.tektopia.entities.ai.EntityAIMoveToBlock;
import net.tangotek.tektopia.structures.VillageStructureMineshaft;
import net.tangotek.tektopia.tickjob.TickJob;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityAIMining.class)
public abstract class EntityAIMiningMixin extends EntityAIMoveToBlock {

    public EntityAIMiningMixin(EntityVillageNavigator v) {
        super(v);
    }

    @Shadow(remap = false)
    protected @Final EntityVillagerTek villager;
    @Shadow(remap = false)
    private void tryBonusOre(EntityVillagerTek villager){}
    @Shadow(remap = false)
    private ItemStack toolUsed = null;
    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    private void mineBlock(BlockPos minePos, int skillChance) {
        int skill = this.villager.getSkill(ProfessionType.MINER);
        boolean dropStone = ConfigHandler.VILLAGER_STONE_SUPPORT_ENABLE && this.villager.isAIFilterEnabled("mining.stone") && stoneDropCheck(skill);
        boolean dropBlock = VillageStructureMineshaft.isOre(this.villager.world, minePos) || dropStone;
        if (this.villager.world.getBlockState(minePos).getBlock() == Blocks.STONE && !dropStone) {
            this.tryBonusOre(this.villager);
        }

        ModBlocks.villagerDestroyBlock(minePos, this.villager, dropBlock, this.villager.world.getBlockState(minePos).getBlock() != Blocks.EMERALD_ORE);
        this.villager.modifyHunger(-1);
        this.villager.throttledSadness(-this.villager.getRNG().nextInt(2) - 1);
        this.villager.tryAddSkill(ProfessionType.MINER, skillChance);
        if (this.villager.getRNG().nextInt(3) != 0) {
            this.villager.damageItem(this.toolUsed, 1);
        }

        this.villager.addJob(new TickJob(20, 0, false, () -> this.villager.pickupItems(4)));
    }

    private boolean stoneDropCheck(int skill) {
        return this.villager.getRNG().nextInt(5) == 0;
    }
}
