package axelmontini.immersivesawmills.client.render;

import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class TileRenderSawmill extends TileEntitySpecialRenderer<TileEntitySawmill> {

    final float structureLength = 4.0f-.5f, processPoint = 2f;

    @Override
    public void renderTileEntityAt(TileEntitySawmill tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if(!tile.formed || tile.isDummy() || !tile.getWorld().isBlockLoaded(tile.getPos()))
            return;


        BlockPos blockPos = tile.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if(state.getBlock() != ISContent.blockMetalMultiblock)
            return;

        float shift[] = new float[tile.processQueue.size()];
        for(int i=0; i<shift.length; i++)
        {
            MultiblockProcess process = tile.processQueue.get(i);
            if(process==null)
                continue;
            float travel = structureLength*process.processTick/process.maxTicks;

            shift[i] = -((tile.shouldRenderAsActive()||travel<processPoint?travel : processPoint));
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x+.5, y+1.5, z+.5);

        for(int i=0; i<shift.length; i++)
        {
            MultiblockProcess process = tile.processQueue.get(i);
            if(process==null || !(process instanceof TileEntityMultiblockMetal.MultiblockProcessInWorld))
                continue;

            List<ItemStack> displays;
            displays = ((TileEntityMultiblockMetal.MultiblockProcessInWorld)process).getDisplayItem();

            SawmillRecipe recipe = (SawmillRecipe)process.recipe;

            if(!displays.isEmpty())
            {
                if ((-shift[i]) > processPoint) {
                    GlStateManager.pushMatrix();    //render output
                    GlStateManager.translate(0,0,shift[i]);
                    float scale = .5f;
                    GlStateManager.scale(scale, scale, scale);
                    GlStateManager.rotate(-90, 1, 0, 0);
                    ClientUtils.mc().getRenderItem().renderItem(recipe.output, ItemCameraTransforms.TransformType.FIXED);
                    GlStateManager.popMatrix();
                    GlStateManager.pushMatrix();    //Render woodchips, moving to the side
                    GlStateManager.translate(-shift[i]*0.375f, 0,-2f); //shift[i]/4f*1.5f
                    GlStateManager.scale(scale, scale, scale);
                    GlStateManager.rotate(-90, 1, 0, 0);
                    ClientUtils.mc().getRenderItem().renderItem(recipe.woodchips, ItemCameraTransforms.TransformType.FIXED);
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();    //render input
                    GlStateManager.translate(0,0,shift[i]);
                    GlStateManager.rotate(-90, 1, 0, 0);
                    float scale = .5f;
                    GlStateManager.scale(scale, scale, scale);
                    ClientUtils.mc().getRenderItem().renderItem(displays.get(0), ItemCameraTransforms.TransformType.FIXED);
                    GlStateManager.popMatrix();
                }
            }
        }

        GlStateManager.popMatrix();
    }
}
