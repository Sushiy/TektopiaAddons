package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.tangotek.tektopia.ProfessionType;
import net.tangotek.tektopia.caps.VillageData;
import net.tangotek.tektopia.economy.ItemEconomy;
import net.tangotek.tektopia.economy.ItemValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VillageData.class)
public abstract class VillageDataMixin {

    @Shadow(remap = false)
    protected ItemEconomy economy;

    @Inject(
            method = "initEconomy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/economy/ItemEconomy;addItem(Lnet/tangotek/tektopia/economy/ItemValue;)V",
                    ordinal = 4
            ),
            remap = false
    )
    private void initEconomyInject(CallbackInfo ci) {
        if (!ConfigHandler.VILLAGER_STONE_SUPPORT_ENABLE) return;
        this.economy.addItem(new ItemValue(new ItemStack(Blocks.COBBLESTONE, 64), 4, 22, ProfessionType.MINER));
    }
}
