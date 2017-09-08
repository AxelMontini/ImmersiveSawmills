package axelmontini.immersivesawmills.client.render;

import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntityBiomassGenerator;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileRenderBiomassGenerator extends TileEntitySpecialRenderer<TileEntityBiomassGenerator> {
    @Override
    public void renderTileEntityAt(TileEntityBiomassGenerator tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if(!tile.formed || tile.isDummy() || !tile.getWorld().isBlockLoaded(tile.getPos(), false))
            return;
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos blockPos = tile.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if(state.getBlock() != ISContent.blockMetalMultiblock)
            return;

        state = state.getActualState(getWorld(), blockPos);
        state = state.withProperty(IEProperties.DYNAMICRENDER, true);
        IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldRenderer = tessellator.getBuffer();

        ClientUtils.bindAtlas();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
//        GlStateManager.rotate(180f, 0, 1, 0);
//        GlStateManager.translate(.5, .5, .5); In JSON file already

        RenderHelper.enableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(Minecraft.isAmbientOcclusionEnabled()?7425:7424);
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, blockPos, worldRenderer, true);
        tessellator.draw();
        GlStateManager.popMatrix();
    }
}
