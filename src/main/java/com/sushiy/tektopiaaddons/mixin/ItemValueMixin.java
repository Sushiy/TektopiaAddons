package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.tangotek.tektopia.economy.ItemValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.security.InvalidParameterException;

@Mixin(value = ItemValue.class)
public abstract class ItemValueMixin {
    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static String getName(ItemStack stack) {
        ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName == null) {
            throw new InvalidParameterException("No registry name set: " + stack.getItem().getTranslationKey() + "_" + stack.getMetadata());
        } else {
            return registryName + "_" + stack.getMetadata();
        }
    }
}
