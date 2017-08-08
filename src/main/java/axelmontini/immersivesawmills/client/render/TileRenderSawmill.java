package axelmontini.immersivesawmills.client.render;

import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.BlockPos;

public class TileRenderSawmill extends TileEntitySpecialRenderer<TileEntitySawmill> {

    @Override
    public void renderTileEntityAt(TileEntitySawmill tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if(!tile.formed || tile.isDummy() || !tile.getWorld().isBlockLoaded(tile.getPos()))
            return;
        BlockPos blockPos = tile.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if(state.getBlock() != ISContent.blockMetalMultiblock)
            return;

    }
}
