package com.sushiy.tektopiaaddons.mixin;

import com.sushiy.tektopiaaddons.ConfigHandler;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(value = StructureVillagePieces.class)
public class StructureVillagePiecesMixin {

    @Inject(method = "getStructureVillageWeightedPieceList", at = @At("RETURN"), cancellable = true)
    private static void removeHouse1(Random random, int size, CallbackInfoReturnable<List<StructureVillagePieces.PieceWeight>> cir) {
        if (!ConfigHandler.CHALLENGING_VILLAGE_START_ENABLED) return;
        List<StructureVillagePieces.PieceWeight> list = cir.getReturnValue();
        list.removeIf(weight -> weight.villagePieceClass == StructureVillagePieces.House1.class);
        list.removeIf(weight -> weight.villagePieceClass == StructureVillagePieces.House3.class);
        list.removeIf(weight -> weight.villagePieceClass == StructureVillagePieces.House4Garden.class);
        list.removeIf(weight -> weight.villagePieceClass == StructureVillagePieces.WoodHut.class);
        cir.setReturnValue(list);
    }
}
