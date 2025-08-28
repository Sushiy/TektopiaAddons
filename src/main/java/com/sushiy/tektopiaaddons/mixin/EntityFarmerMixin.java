package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityFarmer;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.storage.ItemDesire;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = EntityFarmer.class)
public abstract class EntityFarmerMixin extends EntityVillagerTek {
    public EntityFarmerMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> HARVEST_MODDED;
    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> PLANT_MODDED;
    @Shadow(remap = false)
    private static @Final DataParameter<Boolean> GATHER_CANE;
    /**
     * @author Sushiy
     * @reason new method registers all available crops for planting and harvesting
     */

    @Inject(method = "entityInit", at = @At("TAIL"))
    protected void entityInit(CallbackInfo ci) {
        this.registerAIFilter("harvest_tile.modded", HARVEST_MODDED);
        this.registerAIFilter("plant_tile.modded", PLANT_MODDED);
    }

    @Inject(method = "initEntityAI", at = @At("TAIL"))
    protected void initEntityAI(CallbackInfo ci) {
        for(Item item : TektopiaAddons.seedItems)
        {
            if(ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft")) continue; //Minecraft items where already added

            this.getDesireSet().addItemDesire(new ItemDesire(item, 1, 8, 16, (p) -> p.isAIFilterEnabled("plant_tile.modded")));
        }
        for(Item item : TektopiaAddons.cropItems)
        {
            if(ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft")) continue; //Minecraft items where already added
            if(item instanceof ItemSeedFood)
            {
                this.getDesireSet().addItemDesire(new ItemDesire(item, 1, 5, 12, (p) -> p.isAIFilterEnabled("harvest_tile.modded")));
            }
            else
            {
                this.getDesireSet().addItemDesire(new ItemDesire(item, 0, 0, 12, (p) -> p.isAIFilterEnabled("harvest_tile.modded")));
            }
        }
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        HARVEST_MODDED = EntityDataManager.createKey(EntityFarmer.class, DataSerializers.BOOLEAN);
        PLANT_MODDED = EntityDataManager.createKey(EntityFarmer.class, DataSerializers.BOOLEAN);
    }

    /**
     * @author Sushiy
     * @reason oldMethod is terrible
     */
    @Overwrite(remap = false)
    protected ItemStack modifyPickUpStack(ItemStack itemStack) {
        if ((itemStack.getItem() instanceof ItemSeeds) && itemStack.getCount() > 1) {
            itemStack.setCount(1);
        }
        else if (itemStack.getItem() instanceof ItemSeedFood) {
            int skill = this.getSkill(this.getProfessionType());
            if (itemStack.getCount() >= 2 && this.getRNG().nextInt(100) > skill) {
                itemStack.setCount(itemStack.getCount() - 1);
            }
        }
        return itemStack;
    }

    /**
     * @author Sushiy
     * @reason  add compatibility for all crops and seeds
     */
    @Overwrite(remap = false)
    public static Item getSeed(IBlockState blockState)
    {
        if(blockState.getBlock() instanceof BlockCrops)
        {
            BlockCrops b = (BlockCrops) blockState.getBlock();
            return b.getItem(null, null, blockState).getItem();
        }
        return Items.AIR;
    }

    /**
     * @author Sushiy
     * @reason add compatibility for all crops and seeds
     */
    @Overwrite(remap = false)
    public Predicate<ItemStack> isSeed()
    {
        return (p) -> p.getItem() instanceof ItemSeeds || p.getItem() instanceof ItemSeedFood || TektopiaAddons.seedItems.contains(p.getItem());
    }/**
     * @author Sushiy
     * @reason add compatibility for all crops and seeds
     */
    @Overwrite(remap = false)
    public Predicate<ItemStack> isHarvestItem() {
        return (p) -> p.getItem() instanceof ItemSeedFood || TektopiaAddons.cropItems.contains(p.getItem()) || super.isHarvestItem().test(p);
    }
}
