package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.common.blocks.BlockISMultiblock;
import axelmontini.immersivesawmills.common.blocks.ItemBlockISBase;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblocks extends BlockISMultiblock<BlockTypes_MetalMultiblock> {
    public BlockMetalMultiblocks() {
        super("metalMultiblock", Material.IRON, PropertyEnum.create("type", BlockTypes_MetalMultiblock.class), ItemBlockISBase.class, IEProperties.DYNAMICRENDER,IEProperties.BOOLEANS[0], Properties.AnimationProperty,IEProperties.OBJ_TEXTURE_REMAP);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        switch(BlockTypes_MetalMultiblock.values()[meta]) {
            case SAWMILL:
                return new TileEntitySawmill();
            case BIOMASS_GENERATOR:
                return new TileEntityBiomassGenerator();
        }
        return null;
    }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityMultiblockPart)
        {
            TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
            if(tile instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal) tile).isRedstonePos())
                return true;
            if(te instanceof TileEntitySawmill)
            {
                return tile.pos!=7 && tile.pos != 1 && tile.pos != 3 && tile.pos != 9 && tile.pos != 11 && tile.pos != 15;
            }
        }
        return super.isSideSolid(state, world, pos, side);
    }

    @Override
    public boolean useCustomStateMapper()
    {
        return true;
    }
    @Override
    public String getCustomStateMapping(int meta, boolean itemBlock)
    {
        if(BlockTypes_MetalMultiblock.values()[meta].needsCustomState())
            return BlockTypes_MetalMultiblock.values()[meta].getCustomState();
        return null;
    }


    @Override
    public boolean allowHammerHarvest(IBlockState state)
    {
        return true;
    }

}
