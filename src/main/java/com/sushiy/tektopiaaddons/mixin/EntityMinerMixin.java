package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import net.tangotek.tektopia.entities.crafting.Recipe;
import net.tangotek.tektopia.storage.ItemDesire;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        if (!ConfigHandler.VILLAGER_STONE_SUPPORT_ENABLE) return;
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
        if (!ConfigHandler.VILLAGER_STONE_SUPPORT_ENABLE) return;
        for(Block block : TektopiaAddons.stoneBlocks)
        {
            this.getDesireSet().addItemDesire(new ItemDesire(Item.getItemFromBlock(block), 1, 10, 64, (Predicate) null));
        }
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void staticBlock(CallbackInfo ci) {
        MINE_STONE = EntityDataManager.createKey(EntityMiner.class, DataSerializers.BOOLEAN);
    }

    @Inject(method = "buildCraftSet", at = @At("RETURN"), cancellable = true, remap = false)
    private static void buildCraftSetInject(CallbackInfoReturnable<List<Recipe>> cir) {
        if (!ConfigHandler.VILLAGER_STONE_SUPPORT_ENABLE) return;
        List<Recipe> recipes = new ArrayList();
        List<ItemStack> ingredients = new ArrayList();
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.COBBLESTONE), 3));
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 99));
        Recipe recipe = new Recipe(ProfessionType.MINER, "craft_stone_pickaxe", 2, new ItemStack(Items.STONE_PICKAXE, 1), ingredients, 1, 1, (v) -> v.getSkillLerp(ProfessionType.MINER, 9, 3), 1, (v) -> !hasBetterPick(v));
        recipes.add(recipe);
        recipes.addAll(cir.getReturnValue());
        ingredients = new ArrayList();
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.COBBLESTONE), 3));
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 99));
        recipe = new Recipe(ProfessionType.MINER, "craft_stone_axe", 2, new ItemStack(Items.STONE_AXE, 1), ingredients, 1, 1, (v) -> v.getSkillLerp(ProfessionType.MINER, 9, 3), 1, (v) -> hasBetterPick(v));
        recipes.add(recipe);
        ingredients = new ArrayList();
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.COBBLESTONE), 2));
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 99));
        recipe = new Recipe(ProfessionType.MINER, "craft_stone_sword", 2, new ItemStack(Items.STONE_SWORD, 1), ingredients, 1, 1, (v) -> v.getSkillLerp(ProfessionType.MINER, 9, 3), 1, (v) -> hasBetterPick(v));
        recipes.add(recipe);
        ingredients = new ArrayList();
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.COBBLESTONE), 2));
        ingredients.add(new ItemStack(Item.getItemFromBlock(Blocks.LOG), 1, 99));
        recipe = new Recipe(ProfessionType.MINER, "craft_stone_hoe", 2, new ItemStack(Items.STONE_HOE, 1), ingredients, 2, 3, (v) -> v.getSkillLerp(ProfessionType.MINER, 9, 3), 1, (v) -> hasBetterPick(v));
        recipes.add(recipe);
        cir.setReturnValue(recipes);
    }

    private static boolean hasBetterPick(EntityVillagerTek villager) {
        List<ItemStack> weaponList = villager.getInventory().getItems(EntityMiner.getBestPick(villager), 1);
        for (ItemStack stack : weaponList) {
            if (stack.getItem() != Items.WOODEN_PICKAXE) {
                return true;
            }
        }
        return false;
    }
}
