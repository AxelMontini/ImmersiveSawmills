package axelmontini.immersivesawmills.common.blocks.multiblock;

import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.BlockTypes_MetalMultiblock;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntityBiomassGenerator;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import axelmontini.immersivesawmills.common.utils.MultiblockUtils;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static net.minecraft.util.EnumFacing.*;

public class MultiblockBiomassGenerator implements MultiblockHandler.IMultiblock {

    /**Format H L W*/
    public static final ItemStack[][][] structure = new ItemStack[3][4][5];
    public static final MultiblockBiomassGenerator instance = new MultiblockBiomassGenerator();

    static {
        //Structure assembly loop divided by BLOCK TYPES. Don't be scared of the conditions.
        //  "Math homework, you said?"
        for (int h = 0; h < 3; h++){
            for (int l = 0; l < 4; l++){
                for (int w = 0; w < 5; w++){
                    //Front row through W axis || back row through W axis ||                side right     || side left
                    if( (h==0&&l==0) || (h==0&&l==3&&w!=3) || (w==4 && ((h%2==0&&l%3!=0)||(h==1&&l%3==0))) || (w==0 && ((h==1&&l%3==0)||(h==2&&l%3!=0))) )
                        structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
                    //Generator block group
                    else if(w==0 && (l==1||l==2)&&(h==0||h==1))
                        structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.GENERATOR.getMeta());
                    //Sheetmetal tank
                    else if(((w==1&&h==0)||(w==2&&(h==1||h==0))) && (l==1||l==2))
                        structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal, 1, BlockTypes_MetalsAll.STEEL.getMeta());
                    //HeavyEng.: Line through L axis      || Single block near dropper
                    else if(h==1 && ((w==1&&(l==1||l==2)) || (w==3&&l==1)))
                        structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta());
                    //Pipes: height 0 pipe      || back line W axis           || top L shape, W axis as vertical
                    else if( (h==0&&l==3&&w==3) || (h==1 && l==3 && w%4 != 0) || (h==2 && ((l==1&&w==1)||(l==2&&w%4!=0))) )
                        structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
                    else if( w==3 && ((h==0&&l%3!=0) || (h==1&&l==2)))
                        structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RADIATOR.getMeta());
                    else if( w==3 ) {
                        if(h==1&&l==0)
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
                        else if(h==2&&l==1)
                            structure[h][l][w] = new ItemStack(Blocks.HOPPER, 1);
                    }
                }
            }
        }
    }

    @Override
    public String getUniqueName() {
        return "IS:BiomassGenerator";
    }

    /**Trigger block is <b>HOPPER</b>*/
    @Override
    public boolean isBlockTrigger(IBlockState state) {
        return state.getBlock() == Blocks.HOPPER;
    }

    @Override
    public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {

        EnumFacing facing = null;
        //Find origin
        if(Utils.isBlockAt(world, pos.offset(EAST), IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()))
            facing = NORTH;
        else if(Utils.isBlockAt(world, pos.offset(SOUTH), IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()))
            facing = EAST;
        else if(Utils.isBlockAt(world, pos.offset(WEST), IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()))
            facing = SOUTH;
        else if(Utils.isBlockAt(world, pos.offset(NORTH), IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()))
            facing = WEST;
        if(facing==null)
            return false;

        final BlockPos origin = pos.offset(facing.rotateYCCW(),3).offset(DOWN, 2).offset(facing.getOpposite());

        //Checkstructure
        final boolean assembled = MultiblockUtils.isStructureRight(world, origin, facing, structure, true);
        if(!assembled)
            return false;

        //Make tile entities

        for(int h = 0; h<3; h++)
            for(int l = 0; l<4; l++)
                for(int w = 0; w<5; w++) {
                    BlockPos crosshair = origin.offset(facing, l).offset(UP, h).offset(facing.rotateY(), w);
                    world.setBlockState(crosshair, ISContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.BIOMASS_GENERATOR.getMeta()));
                    TileEntity toCheck = world.getTileEntity(crosshair);
                    if(toCheck instanceof TileEntityBiomassGenerator) {
                        TileEntityBiomassGenerator tile = (TileEntityBiomassGenerator) toCheck;
                        tile.facing = facing;
                        tile.formed = true;
                        tile.pos = MultiblockUtils.getPosOfBlockInStructure(world, origin, facing, structure, crosshair);
                        Vec3i offset = crosshair.subtract(origin);
                        tile.offset = new int[] {offset.getX(), offset.getY(), offset.getZ()};
                        tile.markDirty();
                        world.addBlockEvent(crosshair, ISContent.blockMetalMultiblock, 255, 0);
                    }
                }


        return true;
    }

    @Override
    public ItemStack[][][] getStructureManual() {
        return structure;
    }

    @Override
    public IngredientStack[] getTotalMaterials() {
        return new IngredientStack[0]; //TODO
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        return false;
    }

    @Override
    public float getManualScale() {
        return 6;
    }

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;
    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderFormedStructure() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if(renderStack==null)
            renderStack = new ItemStack(ISContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.BIOMASS_GENERATOR.getMeta());
        GlStateManager.scale(.5, .5, .5);
        GlStateManager.translate(5,5,5);
        GlStateManager.rotate(180, 0, 1, 0);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }
}
