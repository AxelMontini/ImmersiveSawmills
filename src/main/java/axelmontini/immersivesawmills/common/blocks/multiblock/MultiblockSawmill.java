package axelmontini.immersivesawmills.common.blocks.multiblock;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.BlockTypes_MetalMultiblock;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Conveyor;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static axelmontini.immersivesawmills.common.utils.MultiblockUtils.getPosOfBlockInStructure;
import static axelmontini.immersivesawmills.common.utils.MultiblockUtils.isStructureRight;
import static net.minecraft.util.EnumFacing.*;

//
//  TODO    --  Add model and render it --
//


/**
 * Multiblock :: It's a sawmill, as the name says.
 */
public class MultiblockSawmill implements MultiblockHandler.IMultiblock {
    /**Format H L W*/
    public static final ItemStack[][][] structure;
    public static final MultiblockSawmill instance = new MultiblockSawmill();

    static {
        //HLW
        structure = new ItemStack[][][] {
                {
                    {new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()), null},
                    {new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta()), null},
                    {new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()), new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta())},
                    {new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta()), null}
                },
                {
                    {new ItemStack(IEContent.blockConveyor, 1, BlockTypes_Conveyor.CONVEYOR.getMeta()), null},
                    {new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()), null},
                    {new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta()), new ItemStack(IEContent.blockConveyor, 1, BlockTypes_Conveyor.CONVEYOR.getMeta())},
                    {new ItemStack(IEContent.blockConveyor, 1, BlockTypes_Conveyor.CONVEYOR.getMeta()), null}
                }
        };
    }


    @Override
    public String getUniqueName() {
        return "IS:Sawmill";
    }

    //Trigger block: heavy engin. block
    @Override
    public boolean isBlockTrigger(IBlockState state) {  //True only if engineering block
        return state.getBlock() == IEContent.blockMetalDecoration0 && state.getBlock().getMetaFromState(state) == BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta();
    }

    @Override
    public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        EnumFacing direction = null;

        if (Utils.isBlockAt(world, pos.add(0, -1, 1), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) {
            direction = NORTH;
        } else if (Utils.isBlockAt(world, pos.add(0, -1, -1), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) {
            direction = SOUTH;
        } else if (Utils.isBlockAt(world, pos.add(1, -1, 0), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) {
            direction = WEST;
        } else if (Utils.isBlockAt(world, pos.add(-1, -1, 0), IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())) {
            direction = EAST;
        }

        if (direction == null)
            return false;   //If no block could be found, then this isn't this structure.

        //Bottom-left-front corner
        BlockPos origin = pos.offset(direction, -2).offset(DOWN, 1);

        //Check if the structure is ok and return
        final boolean ok = isStructureRight(world, origin, direction, structure);

        //If ok, set block state and check for tile entities of right type.
        if(ok) {
            for(int h = 0; h<structure.length; h++) {
                for (int l = 0; l < structure[0].length; l++) {
                    for (int w = 0; w < structure[0][0].length; w++) {
                        if (w == 1 && l != 2)
                            continue;   //Skip air blocks
                        //Offset, current crosshair pos to check
                        BlockPos pos2 = origin.offset(direction, l).offset(direction.rotateY(), w).offset(EnumFacing.UP, h);
                        //Set block state corresponding this the multiblock
                        world.setBlockState(pos2, ISContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_MetalMultiblock.SAWMILL.getMeta()));
                        TileEntity curr = world.getTileEntity(pos2);
                        if (curr instanceof TileEntitySawmill) {
                            TileEntitySawmill tile = (TileEntitySawmill) curr;
                            tile.facing = direction;
                            tile.formed = true;
                            tile.pos = getPosOfBlockInStructure(world, origin, direction, structure, pos2);
                            Vec3i offset = pos2.subtract(origin);   //Offset
                            tile.offset = new int[]{offset.getX(), offset.getY(), offset.getZ()};  //Offset is in world XYZ format
                            tile.markDirty();
                            world.addBlockEvent(pos2, ISContent.blockMetalMultiblock, 255, 0);
                        }
                    }
                }
            }
        }

        return ok;
    }

    @Override
    public ItemStack[][][] getStructureManual() {
        return structure;
    }

    /**Structure materials (quantity per item)*/
    private static final IngredientStack[] materials = new IngredientStack[] {
            new IngredientStack("scaffoldingSteel", 4),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
            new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
            new IngredientStack(new ItemStack(IEContent.blockConveyor, 3, BlockTypes_Conveyor.CONVEYOR.getMeta())),
    };

    @Override
    public IngredientStack[] getTotalMaterials() {
        return materials;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        if(iterator == 8 || iterator == 14) {
            return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.EAST);
        } else if(iterator == 13) {
            return ImmersiveEngineering.proxy.drawConveyorInGui("immersiveengineering:conveyor", EnumFacing.SOUTH);
        }

        return false;
    }

    @Override
    public float getManualScale() {
        return 18;
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
        if(renderStack == null)
            renderStack = new ItemStack(ISContent.blockMetalMultiblock, 1, BlockTypes_MetalMultiblock.SAWMILL.getMeta());
        GlStateManager.scale(0.5, 0.5, 0.5);
//        GlStateManager.translate();
        GlStateManager.rotate(180, 0, 1, 0);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }
}
