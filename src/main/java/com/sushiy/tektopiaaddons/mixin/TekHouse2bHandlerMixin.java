package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.tangotek.tektopia.generation.TekHouse2b;
import net.tangotek.tektopia.generation.TekHouse2bHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = TekHouse2bHandler.class)
public abstract class TekHouse2bHandlerMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random parRandom, int size) {
        System.out.println("Getting village TekHouse2 piece weight");
        return new StructureVillagePieces.PieceWeight(TekHouse2b.class, 80,
                MathHelper.getInt(parRandom, 2 + size, 4 + size * 2));
    }
}
