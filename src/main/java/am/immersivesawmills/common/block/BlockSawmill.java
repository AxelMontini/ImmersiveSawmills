package am.immersivesawmills.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by Axel Montini on 07.06.2017.
 */
public class BlockSawmill extends Block implements ITileEntityProvider {

    public BlockSawmill(Material materialIn) {
        super(materialIn);
        isBlockContainer = true;
        setHardness(1F);

    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
