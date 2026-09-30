package com.sushiy.tektopiaaddons;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.tangotek.tektopia.ModItems;
import net.tangotek.tektopia.TekVillager;

@EventBusSubscriber
public final class RegistrationHandler {

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
        if (!ConfigHandler.BASIC_PROFESSION_TOKEN_CRAFTING) return;
        GameRegistry.addShapelessRecipe(
                new ResourceLocation(TekVillager.MODID, "profession_farmer"),
                null,
                new ItemStack(ModItems.itemFarmer),
                Ingredient.fromItem(Items.EMERALD),
                Ingredient.fromItem(Items.WOODEN_HOE),
                Ingredient.fromItem(Items.STONE_HOE),
                Ingredient.fromItem(Items.IRON_HOE)
        );
        GameRegistry.addShapelessRecipe(
                new ResourceLocation(TekVillager.MODID, "profession_lumberjack"),
                null,
                new ItemStack(ModItems.itemLumberjack),
                Ingredient.fromItem(Items.EMERALD),
                Ingredient.fromItem(Items.WOODEN_AXE),
                Ingredient.fromItem(Items.STONE_AXE),
                Ingredient.fromItem(Items.IRON_AXE)
        );
        GameRegistry.addShapelessRecipe(
                new ResourceLocation(TekVillager.MODID, "profession_miner"),
                null,
                new ItemStack(ModItems.itemMiner),
                Ingredient.fromItem(Items.EMERALD),
                Ingredient.fromItem(Items.WOODEN_PICKAXE),
                Ingredient.fromItem(Items.STONE_PICKAXE),
                Ingredient.fromItem(Items.IRON_PICKAXE)
        );
        GameRegistry.addShapelessRecipe(
                new ResourceLocation(TekVillager.MODID, "profession_guard"),
                null,
                new ItemStack(ModItems.itemGuard),
                Ingredient.fromItem(Items.EMERALD),
                Ingredient.fromItem(Items.WOODEN_SWORD),
                Ingredient.fromItem(Items.STONE_SWORD),
                Ingredient.fromItem(Items.IRON_SWORD)
        );
    }
}
