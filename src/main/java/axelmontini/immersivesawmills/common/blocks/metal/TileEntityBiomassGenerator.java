package axelmontini.immersivesawmills.common.blocks.metal;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import axelmontini.immersivesawmills.api.energy.BiomassHandler;
import axelmontini.immersivesawmills.common.Config;
import axelmontini.immersivesawmills.common.blocks.multiblock.MultiblockBiomassGenerator;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.minecraft.util.EnumFacing.UP;

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
    public final float fullEfficiencyTime = Config.ISConfig.BiomassFuel.fullEfficiencyTime;
    /**Energy production efficiency, from 0 to 1*/
    private float efficiency = 0;

    /**Water tank.*/
    public FluidTank[] tanks = new FluidTank[] {new FluidTank(5000)};
//    public MultiStackItemStack inventory = new MultiStackItemStack(128); RIP MY OLD FRIEND
    /**True when fired up already.*/
    public boolean active = false;

    /**Intake hopper / exhaust thing*/
    private double[] exhaustCenterPos = null;

    public double[] getExhaustPos() {
        if (exhaustCenterPos==null) {
            BlockPos pos = master().getPos().offset(facing).offset(facing.rotateY(), 3).offset(UP, 2);
            exhaustCenterPos = new double[] {pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5};
        }
        return exhaustCenterPos;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        active = nbt.getBoolean("active");
        efficiency = nbt.getFloat("efficiency");
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
        nbt.setFloat("efficiency", efficiency);
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

    private Random random = new Random();
    /**Returns true if a random genrated value is lower or equal to the given chance*/
    private boolean doRandomChance(double chance) {
        return random.nextDouble() <= chance;
    }

    private final double chance = 0.02;
    @Override
    public void update() {
        super.update();
        if(isDummy())
            return;
        boolean prevActive = active;
        if(active) {
            //Drip effect randomly (some shards of burning woodchips might end out of the generator while dropping in new fuel) (looks good)
            if (doRandomChance(chance)) {
                for(int i=0; i<random.nextInt(5); i++)
                    ImmersiveSawmills.proxy.spawnParticleOnlyClient(worldObj, EnumParticleTypes.FLAME, getExhaustPos()[0], getExhaustPos()[1] + .25f, getExhaustPos()[2], (random.nextDouble()-.5)/3, .125f, (random.nextDouble()-.5)/3);
            }
            // Spawn particle only on physical client
            ImmersiveSawmills.proxy.spawnParticleOnlyClient(worldObj, EnumParticleTypes.SMOKE_LARGE, getExhaustPos()[0], getExhaustPos()[1], getExhaustPos()[2], 0, 0, 0);

            if (!worldObj.isRemote) {
                if (!isRSDisabled()) {
                    int newEnergy = 0;

                    //Recalculate efficiency
                    if(efficiency!=1)
                        efficiency = getNewEfficiency(efficiency, 1);

                    //Burn things and produce energy
                    for (int i = 0; i < burning.length; i++) {
                        if (burning[i] != null) {
                            if (burning[i][0] <= 0)  //If done burning, destroy the itemstack
                                burning[i] = null;
                            else {
                                burning[i][0]--; //Burn one tick
                                newEnergy += (int) (burning[i][1]*efficiency); //Produce energy based on efficiency and energy/tick value
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
        } else if (efficiency > 0) {
            efficiency = getNewEfficiency(efficiency, -2);
            efficiency = efficiency>0?efficiency:0;
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
            this.updateMasterBlock(null, true);
        }

        return false;
    }


    //  X is the elapsed ticks
    //  Efficiency calculation:
    // x < fullEfficiencyTime => eff(x) = x/fullEfficiencyTime
    // x >= fullEfficiencyTime => eff(x) = 1
    /**@param efficiency the previous efficiency.
     * @param growth positive/negative value to add to x (intensity of increase/decrease).
     * @return the new efficiency given the previuous value and Increase/Decrease.*/
    public float getNewEfficiency(float efficiency, float growth) {
        final float x = efficiency*fullEfficiencyTime;
        return (x<fullEfficiencyTime || growth < 0) ? (x+growth)/fullEfficiencyTime : 1f;
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
        if(pos == 48)
            return new float[] {0,0,0,1,0.99f,1};
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

    @SideOnly(Side.CLIENT)
    @Override
    public void doGraphicalUpdates(int slot) {
    }


    @Override
    public void disassemble() {
        super.disassemble();
        if(worldObj.isRemote || isDummy())
            return;
        Set<Map.Entry<String, Integer[]>> fuels = BiomassHandler.getCurrentFuelMap().entrySet();
        Queue<ItemStack> drop = new ArrayDeque<>();

        for(Integer[] fuel : this.fuel) {
            for (Map.Entry<String, Integer[]> e : fuels) {
                final Integer[] v = e.getValue();
                if(fuel[0].equals(v[0])&&fuel[1].equals(v[1]))
                    drop.add(OreDictionary.getOres(e.getKey()).get(0));
            }
        }

        final BlockPos origin = master().getPos();

        final BlockPos dropPos = origin.offset(facing.getOpposite());
        drop.forEach(stack -> Utils.dropStackAtPos(worldObj, dropPos, stack));
    }


//    @SideOnly(Side.CLIENT)
//    @Override
//    public AxisAlignedBB getRenderBoundingBox() {
//        return new AxisAlignedBB(getPos(), getPos().offset(facing, 3).offset(facing.rotateY(), 4).offset(UP, 2));
//    }


}
