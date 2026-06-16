package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.Village;
import net.tangotek.tektopia.structures.VillageStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Village.class)
public abstract class VillageMixin {
    @Redirect(
            method = "addStructure",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/tangotek/tektopia/structures/VillageStructure;getDoorOutside()Lnet/minecraft/util/math/BlockPos;",
                    ordinal = 0
            ),
            remap = false
    )
    private BlockPos getDoorOutsideRedirect(VillageStructure struct) {
        return struct.getDoor();
    }
}
