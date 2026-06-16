package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.OreDictStack;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityChef;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(value = EntityChef.class)
public abstract class EntityChefMixin extends EntityVillagerTek {
    public EntityChefMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @ModifyArg(
            method = "buildCraftSet",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/crafting/Recipe;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;ILjava/util/function/Predicate;)V",
                    ordinal = 0
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> woodenBowlIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("logWood"));

        return (List<ItemStack>)(List<?>) ingredients;
    }

    @ModifyArg(
            method = "initEntityAI",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/ai/EntityAISmelting;<init>(Lnet/tangotek/tektopia/entities/EntityVillagerTek;[Lnet/tangotek/tektopia/structures/VillageStructureType;Ljava/util/function/Predicate;Ljava/util/function/Function;Ljava/lang/Runnable;)V",
                    ordinal = 0
            ),
            index = 3,
            remap = false
    )
    private static Function<ItemStack, Integer> bestSmeltableModify(Function<ItemStack, Integer> original) {
        int oreID = OreDictionary.getOreID("logWood");
        return p -> {
            if (p.isEmpty()) return 0;

            for (int id : OreDictionary.getOreIDs(p)) {
                if (id == oreID) return 1;
            }
            return 0;
        };
    }
}
