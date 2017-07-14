package am.immersivesawmills.common.utils;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.util.EnumFacing.*;

/**
 * Created by Axel Montini on 07.06.2017.
 */
public class MultiblockUtils {
    /**Checks the integrity of a given multiblock
     * @param world the world where the multiblock is located
     * @param origin the front-bottom-left corner of the multiblock.
     * @param direction the direction of the multiblock (front face to the rear face).
     * @param structure the structure array, as defined in {@link MultiblockHandler.IMultiblock#getStructureManual()} <i>( format [height][length][width] )</i>.
     * @param allowOverlaps defines if there can be extraneous blocks instead of AIR blocks.
     * @see MultiblockHandler.IMultiblock#getStructureManual()
     * @return true only if the structure is right.*/
    public static final boolean isStructureRight(World world, BlockPos origin, EnumFacing direction, ItemStack[][][] structure, boolean allowOverlaps) {
        if(direction == UP || direction == DOWN || direction == null)
            throw new IllegalArgumentException("Provided EnumFacing is vertical or null and this is not allowed!");
        else if(structure == null)
            throw new IllegalArgumentException("The structure is null!");
        else if(structure.length == 0)
            throw new IllegalArgumentException("Provided structure is 0 blocks wide!");
        else if(structure[0].length == 0)
            throw new IllegalArgumentException("Provided structure is 0 blocks long!");
        else if(structure[0][0].length == 0)
            throw new IllegalArgumentException("Provided structure is 0 blocks tall!");

        IBlockState checkState;

        //Width direction (left to right) (origin is bottom front left corner)
        final EnumFacing dirWidth = direction == NORTH ? EAST : direction==EAST ? SOUTH : direction==SOUTH ? WEST : direction==WEST ? NORTH : null;

        for(int w=0; w<structure[0][0].length; w++) {
            for(int l=0; l<structure[0].length; l++) {
                for(int h=0; h<structure.length; h++) {
                    //CheckState from check offset
                    checkState = world.getBlockState( origin.offset(UP, h).offset(direction, l).offset(dirWidth, w) );
                    //If the check returned "air" and the structure contains "null", this is right (Allocating an array full of AIR blocks may be worse than just solid blocks
                    if(checkState == null && structure[h][l][w] == null)
                        continue;
                    //Structure must be empty but there is something (only if no overlap allowed)
                    else if( (( structure[h][l][w] == null ) && !( checkState.getBlock() == Blocks.AIR )) && !allowOverlaps )
                        return false;
                    //Return false if the block isn't at the right position
                    else if(! checkState.getBlock().equals( Block.getBlockFromItem(structure[h][l][w].getItem()) ))
                        return false;


                }
            }
        }

        return true;    //If the loop ends, it means the structure is ok
    }

    /**Checks the integrity of a given multiblock. <b>Doesn't allow overlap!</b> Use {@link #isStructureRight(World, BlockPos, EnumFacing, ItemStack[][][], boolean)}
     * if you want extraneous blocks to not influence structure formation.
     * @param world the world where the multiblock is located
     * @param origin the front-bottom-left corner of the multiblock.
     * @param direction the direction of the multiblock (front face to the rear face).
     * @param structure the structure array, as defined in {@link MultiblockHandler.IMultiblock#getStructureManual()} <i>( format [height][length][width] )</i>.
     * @see MultiblockHandler.IMultiblock#getStructureManual()
     * @see #isStructureRight(World, BlockPos, EnumFacing, ItemStack[][][], boolean)
     * @return true only if the structure is right.*/
    public static final boolean isStructureRight(World world, BlockPos origin, EnumFacing direction, ItemStack[][][] structure) {
        return isStructureRight(world, origin, direction, structure, false);
    }
}
