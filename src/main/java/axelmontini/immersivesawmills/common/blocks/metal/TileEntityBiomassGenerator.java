package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.api.energy.BiomassHandler;
import axelmontini.immersivesawmills.common.Config;
import axelmontini.immersivesawmills.common.blocks.multiblock.MultiblockBiomassGenerator;
import axelmontini.immersivesawmills.common.utils.MultiStackItemStack;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.primitives.Ints;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.oredict.OreDictionary;
import scala.Int;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**Note: pos 48 is the hopper*/
public class TileEntityBiomassGenerator extends TileEntityMultiblockMetal<TileEntityBiomassGenerator, IMultiblockRecipe> implements IEBlockInterfaces.IPlayerInteraction {
    public TileEntityBiomassGenerator() {
        super(MultiblockBiomassGenerator.instance, new int[] {3,4,5}, 0, true);
    }

    /**Max fuel storage*/
    public final int maxFuel = Config.ISConfig.BiomassFuel.maxFuelStorage;
    /**Fuel queue {burnTime, energyPerTick}*/
    //Fuel converted to {burnTime, energyPerTick} once inserted. Retrievable only if not burned and config not changed
    private final Queue<Integer[]> fuel = new ArrayDeque<>();
    /**Max fuel burning parallel*/
    public final int maxParallelFuel = Config.ISConfig.BiomassFuel.maxFuelParallel;
    /**Burning fuel. Max length; takes the first fuel*/
    private final int[][] burning = new int[maxParallelFuel][];



    /**Water tank.*/
    public FluidTank[] tanks = new FluidTank[] {new FluidTank(5000)};
//    public MultiStackItemStack inventory = new MultiStackItemStack(128); RIP MY OLD FRIEND
    /**True when fired up already.*/
    public boolean active = false;

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        active = nbt.getBoolean("active");
        tanks[0].readFromNBT(nbt.getCompoundTag("tank_water"));
        if(isDummy())
            return;
        //Collect fuel into the queue
        {
            NBTTagList fuels = nbt.getTagList("fuel", Constants.NBT.TAG_LIST);
            this.fuel.addAll(IntStream.range(0, fuels.tagCount()).mapToObj(i -> IntStream.of(fuels.getIntArrayAt(i)).boxed().toArray(Integer[]::new)).collect(Collectors.toList()));
        }
        //Collect burning
        {
            NBTTagList burning = nbt.getTagList("burning", Constants.NBT.TAG_LIST);
            for(int i=0; i<Math.min(maxParallelFuel, burning.tagCount()); i++)
                this.burning[i] = burning.getIntArrayAt(i);
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank_water", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("active", active);
        if(isDummy())
            return;
        //Write out fuel
        {
            NBTTagList fuel = new NBTTagList();
            this.fuel.stream().forEach( f -> fuel.appendTag( new NBTTagIntArray( new int[]{f[0], f[1]} ) ) );
            nbt.setTag("fuel", fuel);
        }
        //Write out burning
        {
            NBTTagList burning = new NBTTagList();
            //Append arrays. Must filter, since the array burning[][] is of fixed size, prevent null entries
            Stream.of(this.burning).filter(b-> b!=null).forEach(b -> burning.appendTag(new NBTTagIntArray(b)));
            nbt.setTag("burning", burning);
        }
    }

    @Override
    public void onEntityCollision(World world, Entity entity) {
        if(pos == 48 && !world.isRemote && entity!=null && !entity.isDead && entity instanceof EntityItem
                && ((EntityItem)entity).getEntityItem()!=null && fuel.size()<maxFuel)
        {
            TileEntityBiomassGenerator master = master();
            if(master==null)
                return;
            ItemStack stack = ((EntityItem)entity).getEntityItem();
            if(stack==null)
                return;

            if(!BiomassHandler.isValidFuel(stack))
                return;

            int[] cht = BiomassHandler.getCharateristics(stack);
            final int toPut = Math.min(maxFuel-fuel.size(), stack.stackSize);
            //Put fuel in the queue
            for(int i=0; i<toPut; i++) {
                master.fuel.add(new Integer[] {cht[0], cht[1]});
            }
            if(toPut<stack.stackSize)    //If left, reduce.
                stack.stackSize-=toPut;
            else
                entity.setDead();   //If consumed, kill entity.
        }
    }

//    /**Get the actual master's Multistack inventory*/
//    public MultiStackItemStack getMasterInventory() {
//        return master().inventory;
//    }

    @Override
    public void update() {
        super.update();
        if(isDummy())
            return;
        boolean prevActive = active;
        if(active) {
            if (worldObj.isRemote) {
                BlockPos exhaust = getBlockPosForPos(48);
                worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, exhaust.getX() + .5, exhaust.getY() + .5, exhaust.getZ() + .5, 0, .1, 0);
            } else {
                if (!isRSDisabled()) {
                    int newEnergy = 0;

                    //Burn things and produce energy
                    for (int i = 0; i < burning.length; i++) {
                        if (burning[i] != null) {
                            if (burning[i][0] <= 0)  //If done burning, destroy the itemstack
                                burning[i] = null;
                            else {
                                burning[i][0]--; //Burn one tick
                                newEnergy += burning[i][1];
                            }
                        }
                    }

                    //Put new fuel int burning if needed: poll() from fuel.
                    int curBurn = (int) Stream.of(burning).filter(a -> a != null).count();
                    if (curBurn < maxParallelFuel && fuel.size() > 0) {
                        final Integer[][] newFuel = IntStream.of(0, maxParallelFuel - curBurn).mapToObj(ind -> fuel.poll()).toArray(Integer[][]::new);
                        for (int i = 0, cf = 0; i < maxParallelFuel && cf < newFuel.length; i++) {
                            if (burning[i] == null) {
                                Integer[] f = newFuel[cf];
                                burning[i] = new int[]{f[0], f[1]};
                                cf++;
                            }
                        }
                    }

                    //If burning is empty even after filling, set inactive
                    if (!Stream.of(burning).anyMatch(b -> b != null)) {
                        active = false;
                    }

                    //Actually output energy
                    TileEntity receiver0 = getEnergyOutput(0), receiver1 = getEnergyOutput(1);
                    int en0 = newEnergy / 2;
                    EnergyHelper.insertFlux(receiver0, facing.rotateY(), en0, false);
                    EnergyHelper.insertFlux(receiver1, facing.rotateY(), newEnergy - en0, false);
                }
            }
        }
        if(prevActive!=active) {    //Update blocks if state changed
            this.markDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override
    public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if(pos == 48&& canFireUp(heldItem) && !master().fuel.isEmpty()) {
            heldItem.damageItem(3, player);
            master().active = true;  //set active
        }

        return false;
    }

    public boolean canFireUp(ItemStack stack) {
        if(stack==null)
            return false;
        Item i = stack.getItem();
        return i instanceof ItemFlintAndSteel || i instanceof ItemFireball;
    }

    TileEntity getEnergyOutput(int w)
    {
        TileEntity eTile = worldObj.getTileEntity(this.getBlockPosForPos(w==0?25:30).offset(facing.rotateYCCW()));
        if(EnergyHelper.isFluxReceiver(eTile, facing.rotateY()))
            return eTile;
        return null;
    }



    @Override
    protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag) {
        return null;
    }

    @Override
    public int[] getEnergyPos() {
        return new int[] {25,30};
    }

    @Override
    public int[] getRedstonePos() {
        return new int[] {23};
    }

    @Override
    public IFluidTank[] getInternalTanks() {
        return tanks;
    }

    @Override
    public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting) {
        return null;
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

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        return new FluidTank[0];
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
        if(pos > 39) {
            if (pos > 52 || pos < 45 ||pos==47)
                return new float[] {0,0,0,0,0,0};
            else if(pos==48)
                return new float[] {0,0,0,1,.75f, 1};
            else
                return new float[] {0,0,0,1,.25f, 1};
        }
        return new float[] {0,0,0,1,1,1};
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
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Override
    public void disassemble() {
        super.disassemble();
        if(worldObj.isRemote || isDummy())
            return;
        Set<Map.Entry<String, Integer[]>> fuels = BiomassHandler.getCurrentFuelMap().entrySet();
        //Clusterf**k of pure functional code (bad idea writing this)
        Queue<ItemStack> drop = new ArrayDeque<>();

        for(Integer[] fuel : this.fuel) {
            for (Map.Entry<String, Integer[]> e : fuels) {
                final Integer[] v = e.getValue();
                if(fuel[0].equals(v[0])&&fuel[1].equals(v[1]))
                    drop.add(OreDictionary.getOres(e.getKey()).get(0));
            }
        }

        final BlockPos dropPos = master().getPos().offset(facing.getOpposite());
        drop.forEach(stack -> Utils.dropStackAtPos(worldObj, dropPos, stack));
    }
}
