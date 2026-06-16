package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.OreDictStack;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.entities.EntityButcher;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityButcher.class)
public abstract class EntityButcherMixin extends EntityVillagerTek {
    public EntityButcherMixin(World worldIn, ProfessionType profType, int roleMask) {
        super(worldIn, profType, roleMask);
    }

    @ModifyArg(
            method = "buildCraftSet",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/entities/EntityButcher$1;<init>(Lnet/tangotek/tektopia/ProfessionType;Ljava/lang/String;ILnet/minecraft/item/ItemStack;Ljava/util/List;IILjava/util/function/Function;ILjava/util/function/Predicate;)V",
                    ordinal = 0
            ),
            index = 4,
            remap = false
    )
    private static List<ItemStack> woodenAxeIngredientsModify(List<ItemStack> original) {
        List<Object> ingredients = new ArrayList<>();
        ingredients.add(new OreDictStack("logWood"));

        return (List<ItemStack>)(List<?>) ingredients;
    }
}
