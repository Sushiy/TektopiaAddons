package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityFarmer;
import net.tangotek.tektopia.entities.EntityMiner;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.storage.ItemDesire;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

@Mixin(value = EntityMiner.class)
public abstract class EntityMinerMixin extends EntityVillagerTek {

    public EntityMinerMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @Mutable
    @Unique
    private static @Final DataParameter<Boolean> MINE_STONE;

    @Unique
    public Predicate<ItemStack> tektopiaAddons$isStoneItem()
    {
        return p->  TektopiaAddons.stoneBlocks.contains(Block.getBlockFromItem(p.getItem()));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    //Update the isHarvestItem method to pickup oredictionary ores, gems and dusts
    public Predicate<ItemStack> isHarvestItem() {
        return (p) -> Arrays.stream(OreDictionary.getOreIDs(p)).anyMatch(w -> OreDictionary.getOreName(w).equals("coal"))
                ||  Arrays.stream(OreDictionary.getOreIDs(p)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("ore"))
                ||  Arrays.stream(OreDictionary.getOreIDs(p)).anyMatch(y -> OreDictionary.getOreName(y).startsWith("gem"))
                ||  Arrays.stream(OreDictionary.getOreIDs(p)).anyMatch(z -> OreDictionary.getOreName(z).startsWith("dust"))
                || tektopiaAddons$isStoneItem().test(p)
                || super.isHarvestItem().test(p);
    }

    @Inject(method = "entityInit", at = @At("TAIL"))
    protected void entityInit(CallbackInfo ci) {
        this.registerAIFilter("mining.stone", MINE_STONE);
    }

    @Inject(method = "initEntityAI", at = @At("TAIL"))
    protected void initEntityAI(CallbackInfo ci) {
        for(Item item : TektopiaAddons.oreItems)
        {
            if(ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft")) continue; //Minecraft items where already added

            this.getDesireSet().addItemDesire(new ItemDesire(item, 0, 0, 8, (Predicate) null));
        }
        for(Item item : TektopiaAddons.dustItems)
        {
            if(ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft")) continue; //Minecraft items where already added

            this.getDesireSet().addItemDesire(new ItemDesire(item, 0, 0, 16, (Predicate) null));
        }
        for(Item item : TektopiaAddons.gemItems)
        {
            if(ForgeRegistries.ITEMS.getKey(item).getNamespace().equals("minecraft")) continue; //Minecraft items where already added

            this.getDesireSet().addItemDesire(new ItemDesire(item, 0, 0, 5, (Predicate) null));
        }
        for(Block block : TektopiaAddons.stoneBlocks)
        {
            this.getDesireSet().addItemDesire(new ItemDesire(Item.getItemFromBlock(block), 1, 10, 64, (Predicate) null));
        }
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        MINE_STONE = EntityDataManager.createKey(EntityMiner.class, DataSerializers.BOOLEAN);
    }
}
