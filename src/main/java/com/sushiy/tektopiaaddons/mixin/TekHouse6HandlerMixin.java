package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.tangotek.tektopia.generation.TekHouse6;
import net.tangotek.tektopia.generation.TekHouse6Handler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = TekHouse6Handler.class)
public abstract class TekHouse6HandlerMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random parRandom, int size) {
        System.out.println("Getting village TekHouse6 piece weight");
        return new StructureVillagePieces.PieceWeight(TekHouse6.class, 100,
                MathHelper.getInt(parRandom, 0 + size, 2 + size * 2));
    }
}
