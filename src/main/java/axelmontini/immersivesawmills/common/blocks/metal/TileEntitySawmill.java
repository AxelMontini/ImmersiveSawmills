package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.blocks.multiblock.MultiblockSawmill;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

import static net.minecraft.util.EnumFacing.*;


public class TileEntitySawmill extends TileEntityMultiblockMetal<TileEntitySawmill, SawmillRecipe> implements IEBlockInterfaces.ISoundTile, ConveyorHandler.IConveyorAttachable {

    public TileEntitySawmill() {
        super(MultiblockSawmill.instance, new int[] {4,4,3}, 16000, true);
    }

    @Override
    public boolean shoudlPlaySound(String sound) {
        return shouldRenderAsActive();
    }

   /*@Override
    public void update() {
        super.update();
        if(isDummy()||isRSDisabled()||worldObj.isRemote)
            return;
        for(MultiblockProcess proc : processQueue) {
            float tick = 1/(float) proc.maxTicks;
            float transportTime = 52.5f*tick;
            float fProcess = proc.processTick*tick;

        }
    }*/

    @Override
    protected SawmillRecipe readRecipeFromNBT(NBTTagCompound tag) {
        return null;
    }

    @Override
    public int[] getEnergyPos() {
        return new int[] {12};
    }

    @Override
    public int[] getRedstonePos() {
        return new int[]{2};
    }

    @Override
    public IFluidTank[] getInternalTanks() {
        return new IFluidTank[0];
    }

    @Override
    public SawmillRecipe findRecipeForInsertion(ItemStack inserting) {
        return SawmillRecipe.findRecipe(inserting);
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputTanks() {
        return new int[0];
    }

    @Override
    public boolean additionalCanProcessCheck(MultiblockProcess<SawmillRecipe> process) {
        return false;
    }

    @Override
    public EnumFacing[] sigOutputDirections() {
        if(pos==14)
            return new EnumFacing[]{this.facing};
        else if(pos==13)
            return new EnumFacing[]{this.facing.rotateY()};
        return new EnumFacing[0];
    }

    @Override
    public void doProcessOutput(ItemStack output) {
        BlockPos pos = getPos().offset(facing, 3);

        TileEntity  invTile  = this.worldObj.getTileEntity(pos);

        if(invTile != null) {
            output = Utils.insertStackIntoInventory(invTile, output, facing.getOpposite());
        }
        if(output != null) {    //Drop stacks
            Utils.dropStackAtPos(worldObj, pos, output, facing);
        }
    }

    @Override
    public void onEntityCollision(World world, Entity entity) {
        //When item dropped on front conveyor
        if(pos==8 && !world.isRemote && entity!=null && !entity.isDead && entity instanceof EntityItem && ((EntityItem)entity).getEntityItem()!=null)
        {
            TileEntitySawmill master = master();
            if(master==null)
                return;
            ItemStack stack = ((EntityItem)entity).getEntityItem();
            if(stack==null)
                return;
            IMultiblockRecipe recipe = master.findRecipeForInsertion(stack);
            if(recipe==null)
                return;
            ItemStack displayStack = null;
            for(IngredientStack ingr : recipe.getItemInputs())
                if(ingr.matchesItemStack(stack))
                {
                    displayStack = Utils.copyStackWithAmount(stack, ingr.inputSize);
                    break;
                }
            float transformationPoint = 56.25f/(float)recipe.getTotalProcessTime();
            MultiblockProcess process = new MultiblockProcessInWorld(recipe, transformationPoint, displayStack);
            if(master.addProcessToQueue(process, true))
            {
                master.addProcessToQueue(process, false);
                stack.stackSize -= displayStack.stackSize;
                if(stack.stackSize<=0)
                    entity.setDead();
            }
        }
    }

    @Override
    public void replaceStructureBlock(BlockPos pos, IBlockState state, ItemStack stack, int h, int l, int w) {
        super.replaceStructureBlock(pos, state, stack, h, l, w);
        if (this.pos == 8 || this.pos == 14) {
            TileEntity tile = worldObj.getTileEntity(pos);
            if(tile instanceof TileEntityConveyorBelt)
                ((TileEntityConveyorBelt)tile).setFacing(this.facing);
        } else if (this.pos == 13) {
            TileEntity tile = worldObj.getTileEntity(pos);
            if(tile instanceof TileEntityConveyorBelt)
                ((TileEntityConveyorBelt)tile).setFacing(this.facing.rotateY());
        }
    }

    @Override
    public void doProcessFluidOutput(FluidStack output) {

    }

    @Override
    public void onProcessFinish(MultiblockProcess<SawmillRecipe> process) {
        //Must drop woodchips here: happens after doProcessOutput(...)

        if (process.recipe.woodchips ==null)
            return;

        ItemStack woodchips = null;

        BlockPos pos = getPos().offset(facing, 3);
        TileEntity  invTile  = this.worldObj.getTileEntity(pos);
        if(invTile != null) {
            woodchips = Utils.insertStackIntoInventory(invTile, process.recipe.woodchips, facing.rotateYCCW());
        }
        if(woodchips != null) {    //Drop stacks
            Utils.dropStackAtPos(worldObj, pos, woodchips, facing.rotateY());
        }
    }

    @Override
    public int getMaxProcessPerTick() {
        return 4;
    }

    @Override
    public int getProcessQueueMaxLength() {
        return 4;
    }

    @Override
    public float getMinProcessDistance(MultiblockProcess<SawmillRecipe> process) {
        return 1 - 56.25f/(float)process.recipe.getTotalProcessTime();
    }

    @Override
    public boolean isInWorldProcessingMachine() {
        return true;
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        return new IFluidTank[0];
    }

    @Override
    protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
        return false;
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
        return false;
    }

    @Override
    public float[] getBlockBounds() {

        /*if (pos == 8)
            return new float[]{facing==WEST||facing==EAST?0.5f:0, 0, facing==NORTH||facing==SOUTH?0.5f:0, facing==WEST?0:1, 1, facing==NORTH?0:1};
        else if (pos == 14)
            return new float[]{facing==WEST||facing==EAST?0.5f:0, 0, facing==NORTH||facing==SOUTH?0.5f:0, facing==EAST?0:1, 1, facing==SOUTH?0:1};
        else if (pos == 13)
            return new float[]{0, 0, 0, 0, 0, 0};*/

        if (pos==8||pos==14||pos==13)   //Conveyor belt collider
            return new float[] {0, 0, 0, 1, 0.125f, 1};
        return new float[] {0,0,0,1,1,1};
    }

    @Override
    public ItemStack[] getInventory() {
        return null;
    }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public void doGraphicalUpdates(int slot) {
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if(cap==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntitySawmill master = master();
            if(master==null)
                return false;
            return pos==8&&facing==this.facing.getOpposite();
        }
        return super.hasCapability(cap, facing);
    }

    IItemHandler insertionHandler = new MultiblockInventoryHandler_DirectProcessing(this);
    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntitySawmill master = master();
            if(master==null)
                return null;
            if(pos==8 && facing==this.facing.getOpposite())
                return (T)master.insertionHandler;
            return null;
        }
        return super.getCapability(cap, facing);
    }
}
