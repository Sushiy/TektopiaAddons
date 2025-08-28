package com.sushiy.tektopiaaddons.mixin;

import com.leviathanstudio.craftstudio.client.model.CSModelRenderer;
import com.leviathanstudio.craftstudio.client.model.ModelCraftStudio;
import com.leviathanstudio.craftstudio.client.util.MathHelper;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderArmorStand;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.util.EnumHandSide;
import net.tangotek.tektopia.client.LayerVillagerHeldItem;
import org.spongepowered.asm.mixin.*;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = LayerVillagerHeldItem.class)
public class LayerVillagerHeldItemMixin
{
    @Final
    @Shadow(remap = false)
    protected RenderLivingBase<?> livingEntityRenderer;

    /**
     * @author Sushiy
     * @reason should take both hands into account
     */
    @Overwrite(remap = false)
    protected void translateToHand(EnumHandSide handSide, float scale) {
        ModelCraftStudio model = (ModelCraftStudio)this.livingEntityRenderer.getMainModel();
        Deque<CSModelRenderer> stack = new ArrayDeque();

        for(CSModelRenderer parent : model.getParentBlocks()) {
            if (tektopiaAddons$findChildChain(handSide == EnumHandSide.RIGHT ? "ArmRightWrist" : "ArmLeftLower", parent, stack)) {
                stack.push(parent);

                while(!stack.isEmpty()) {
                    CSModelRenderer modelRenderer = (CSModelRenderer)stack.pop();
                    GlStateManager.translate(modelRenderer.rotationPointX * scale, modelRenderer.rotationPointY * scale, modelRenderer.rotationPointZ * scale);
                    FloatBuffer buf = MathHelper.makeFloatBuffer(modelRenderer.getRotationMatrix());
                    GlStateManager.multMatrix(buf);
                    if(handSide == EnumHandSide.RIGHT)
                        GlStateManager.translate(modelRenderer.offsetX * scale, modelRenderer.offsetY * scale, modelRenderer.offsetZ * scale);
                    else
                        GlStateManager.translate((modelRenderer.offsetX-.5) * scale, (modelRenderer.offsetY+1.5) * scale, (modelRenderer.offsetZ-.5) * scale);

                }
            }
        }

    }

    @Unique
    private boolean tektopiaAddons$findChildChain(String name, CSModelRenderer modelRenderer, Deque<CSModelRenderer> stack) {
        if (modelRenderer.boxName.equals(name)) {
            return true;
        } else {
            if (modelRenderer.childModels != null) {
                for(ModelRenderer child : modelRenderer.childModels) {
                    CSModelRenderer csModel = (CSModelRenderer)child;
                    if (this.tektopiaAddons$findChildChain(name, csModel, stack)) {
                        stack.push(csModel);
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
