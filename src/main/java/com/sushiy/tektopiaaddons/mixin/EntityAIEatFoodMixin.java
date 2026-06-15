package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import com.sushiy.tektopiaaddons.TektopiaAddons;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tangotek.tektopia.TekTopiaGlobalData;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIEatFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.function.BiConsumer;


@Mixin(value = EntityAIEatFood.class)
public abstract class EntityAIEatFoodMixin  extends EntityAIBase {

    @Shadow(remap = false)
    private static void registerFood(Item item, int hunger, int happy){}

    @Shadow(remap = false)
    private static void registerFood(Item item, int hunger, int happy, BiConsumer<EntityVillagerTek, ItemStack> postEat) {}

    @Inject(method = "<clinit>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void staticBlock(CallbackInfo ci, BiConsumer<EntityVillagerTek, ItemStack> returnBowl) {
        for(ItemFood food : TektopiaAddons.standardFoodItems)
        {
            if(food.getRegistryName().getNamespace().equals("minecraft")) continue;
            ItemStack stack = new ItemStack(food);
            float happy = food.getHealAmount(stack) *.5f * food.getSaturationModifier(stack);
            happy = happy * happy;
            happy = Math.min(happy * ConfigHandler.MODDED_FOOD_HAPPINESS_MULTIPLIER, 100);
            if(TektopiaAddons.cropItems.contains(food))
            {
                happy = -1;
            }
            float hunger = food.getHealAmount(stack) *.5f;
            hunger = Math.max(hunger*5, hunger * hunger) * ConfigHandler.MODDED_FOOD_HUNGER_MULTIPLIER;
            hunger = Math.min(hunger, 100);
            //TektopiaAddons.LOGGER.info(TektopiaAddons.MODID + " registered modded food " + food.getRegistryName().getPath() +"(" + food.getHealAmount(stack) + "," + food.getSaturationModifier(stack) + ")" + "(" + hunger + "," + happy + ")");

            for(IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection())
            {
                if(recipe.getRecipeOutput().getItem() == food)
                {
                    if(recipe.getIngredients().stream().anyMatch (x -> Arrays.stream(x.getMatchingStacks()).anyMatch(y-> y.getItem() == Items.BOWL)))
                    {
                        registerFood(food, Math.round(hunger), Math.round(happy), returnBowl);
                        return;
                    }

                }
            }
            registerFood(food, Math.round(hunger), Math.round(happy));
        }

        for(Item customfood : TektopiaAddons.configFoodItems.keySet())
        {
            registerFood(customfood, TektopiaAddons.configFoodItems.get(customfood).hunger, TektopiaAddons.configFoodItems.get(customfood).happiness);
            TektopiaAddons.LOGGER.info(TektopiaAddons.MODID + " registered food  config override" + customfood.getRegistryName().getPath() +"(" + TektopiaAddons.configFoodItems.get(customfood).hunger + "," + TektopiaAddons.configFoodItems.get(customfood).happiness + ")");
        }
    }
}
