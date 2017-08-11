package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.api.energy.BiomassHandler;
import axelmontini.immersivesawmills.common.blocks.multiblock.MultiblockBiomassGenerator;
import axelmontini.immersivesawmills.common.utils.MultiStackItemStack;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class TileEntityBiomassGenerator extends TileEntityMultiblockMetal<TileEntityBiomassGenerator, IMultiblockRecipe> {
    public TileEntityBiomassGenerator() {
        super(MultiblockBiomassGenerator.instance, new int[] {3,4,5}, 0, true);
    }

    public FluidTank[] tanks = new FluidTank[] {new FluidTank(5000)};
    public MultiStackItemStack inventory = new MultiStackItemStack(128);
    /**True when fired up already.*/
    public boolean active = false;

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        active = nbt.getBoolean("active");
        tanks[0].readFromNBT(nbt.getCompoundTag("tankwater"));
        inventory = new MultiStackItemStack((NBTTagCompound) nbt.getTag("inventory"));
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tankwater", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("active", active);
        nbt.setTag("inventory", inventory.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void onEntityCollision(World world, Entity entity) {
        if(pos == 48 && !world.isRemote && entity!=null && !entity.isDead && entity instanceof EntityItem && ((EntityItem)entity).getEntityItem()!=null) {
            TileEntityBiomassGenerator master = master();
            if(master==null)
                return;
            ItemStack stack = ((EntityItem)entity).getEntityItem();
            if(stack==null)
                return;

            if(!BiomassHandler.isValidFuel(stack))
                return;

            ItemStack left = inventory.putStack(stack)[0];
            if(left.stackSize>0)
                stack.stackSize=left.stackSize;
            else
                entity.setDead();
        }
    }

    @Override
    protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag) {
        return null;
    }

    @Override
    public int[] getEnergyPos() {
        return new int[] {5,10,25,30};
    }

    @Override
    public int[] getRedstonePos() {
        return new int[] {23};
    }

    @Override
    public IFluidTank[] getInternalTanks() {
        return null;
    }

    @Override
    public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting) {
        return null;
    }

    @Override
    public int[] getOutputSlots() {
        return null;
    }

    @Override
    public int[] getOutputTanks() {
        return null;
    }

    @Override
    public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process) {
        return false;
    }

    @Override
    public void doProcessOutput(ItemStack output) {

    }

    @Override
    public void doProcessFluidOutput(FluidStack output) {

    }

    @Override
    public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process) {

    }

    @Override
    public int getMaxProcessPerTick() {
        return 0;
    }

    @Override
    public int getProcessQueueMaxLength() {
        return 0;
    }

    @Override
    public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process) {
        return 0;
    }

    @Override
    public boolean isInWorldProcessingMachine() {
        return false;
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        return null;
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
        return null;
    }   //TODO

    @Override
    public ItemStack[] getInventory() {
        return null;
    }//TODO

    @Override
    public boolean isStackValid(int slot, ItemStack stack) {
        return false;
    }//TODO

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }//TODO

    @Override
    public void doGraphicalUpdates(int slot) {

    }
}
