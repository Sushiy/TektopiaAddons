package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.tangotek.tektopia.generation.TekHouse2b;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TekHouse2b.class)
public abstract class TekHouse2bMixin extends StructureVillagePieces.House4Garden {

    @Inject(method = "spawnVillagers", at = @At("TAIL"))
    public void spawnVillagersInject(World worldIn, StructureBoundingBox structurebb, int x, int y, int z, int count, CallbackInfo ci) {
        super.spawnVillagers(worldIn, structurebb, x + 1, y, z, count);
    }
}
