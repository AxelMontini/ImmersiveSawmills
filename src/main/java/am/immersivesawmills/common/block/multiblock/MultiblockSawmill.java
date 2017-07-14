package am.immersivesawmills.common.block.multiblock;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static am.immersivesawmills.common.utils.MultiblockUtils.isStructureRight;
import static net.minecraft.util.EnumFacing.*;

//
//  TODO    --  Add model and render it --
//


/**
 * Created by Axel Montini on 04.06.2017.
 * Multiblock :: It's a sawmill, as the name says.
 */
public class MultiblockSawmill implements MultiblockHandler.IMultiblock {
    public static final ItemStack[][][] structure = new ItemStack[4][4][3];

    //MB composition (bottom to top):
    //4x3 hollow steel scaffolding
    //steel scaffolding at corners, light engineering block in the center (2x1), redstone engineering block right, just after the front scaffolding
    //--same as before--, without redstone engineering
    //same as the first step
    static {
        //Initialize structure array (I cried writing this) (GONE WRONG) (first time)
        for(int w=0; w<3; w++) {
            for(int l=0; l<4; l++) {
                for(int h=0; h<4; h++) {
                    if(h==0 || h==3) {
                        if(l==0 || l==3)    //width axis steel scaffolding
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
                        else if(w==0 || w==2)   //Length axis steel scaffolding
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
                    } else {
                        if(h==1 && w==2 && l==1)    //redstone engineering
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta());
                        else if((l==0 || l==3) && (w==0 || w==2))   //Steel scaffolding corners
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
                        else if (l>0 && l<3 && w==1)                                                    //Light engineering blocks
                            structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
                    }
                }
            }
        }
    }


    @Override
    public String getUniqueName() {
        return "IS:Sawmill";
    }

    //Trigger block: rs engineering block
    @Override
    public boolean isBlockTrigger(IBlockState state) {  //True only if engineering block
        return state.getBlock() == IEContent.blockMetalDecoration0 && state.getBlock().getMetaFromState(state) == BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta();
    }

    @Override
    public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        EnumFacing direction = null;

        if (Utils.isBlockAt(world, pos.add(1, 0, 0), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) {
            //Then direction = -z
            direction = SOUTH;
        } else if (Utils.isBlockAt(world, pos.add(-1, 0, 0), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) {
            //Then direction = z
            direction = NORTH;
        } else if (Utils.isBlockAt(world, pos.add(0, 0, 1), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) {
            //Then direction = -x
            direction = WEST;
        } else if (Utils.isBlockAt(world, pos.add(0, 0, -1), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())) {
            //Then direction = x
            direction = EAST;
        }

        if (direction == null)
            return false;   //If no block could be found, then this isn't this structure.

        //Bottom-left-front corner
        BlockPos origin = pos.offset(direction, -1).offset(DOWN, 1);

        if (direction == NORTH)     //90° anticlockwise angle, go to bottom-front-left corner.
            origin = origin.offset(WEST, 2);
        else if (direction == SOUTH)
            origin = origin.offset(EAST, 2);
        else if (direction == WEST)
            origin = origin.offset(SOUTH, 2);
        else
            origin = origin.offset(NORTH, 2);

        //Check if the structure is ok and return
        final boolean ok = isStructureRight(world, origin, direction, structure);

        return ok;
    }

    @Override
    public ItemStack[][][] getStructureManual() {
        return structure;
    }

    /**Structure materials (quantity per item)*/
    private static final IngredientStack[] materials = new IngredientStack[] {
            new IngredientStack("scaffoldingSteel", 24),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 3, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()))
    };

    @Override
    public IngredientStack[] getTotalMaterials() {
        return materials;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        return false;
    }

    @Override
    public float getManualScale() {
        return 18;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderFormedStructure() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {

    }
}
