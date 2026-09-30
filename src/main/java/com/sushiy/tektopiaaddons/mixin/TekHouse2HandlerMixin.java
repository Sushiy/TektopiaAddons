package com.sushiy.tektopiaaddons.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.tangotek.tektopia.generation.TekHouse2;
import net.tangotek.tektopia.generation.TekHouse2Handler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(value = TekHouse2Handler.class)
public abstract class TekHouse2HandlerMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random parRandom, int size) {
        System.out.println("Getting village TekHouse2 piece weight");
        return new StructureVillagePieces.PieceWeight(TekHouse2.class, 60,
                MathHelper.getInt(parRandom, 2 + size, 5 + size * 3));
    }
}
