package axelmontini.immersivesawmills.api.energy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 *  @author AxelMontini - 09.08.2017
 *
 *  The fuelFurnace handler for the Biomass Generator. Use this to register custom fuels.
 */
public class BiomassHandler {
    public static final HashMap<String, Integer[]> biomassGenBurnTime = new HashMap<>();

    /**
     *  @param block the fuelFurnace to be registered.
     *  @param meta its meta.
     *  @param burnTime its burn time.
     */
    public static void registerFuel(Block block, int meta, int burnTime, int energyPerTick) throws NullPointerException {
        registerFuel(new ItemStack(block, 1, meta), burnTime, energyPerTick);
    }

    /**
     *  @param item the fuelFurnace to be registered.
     *  @param meta its meta.
     *  @param burnTime its burn time.
     */
    public static void registerFuel(Item item, int meta, int burnTime, int energyPerTick) throws NullPointerException {
        registerFuel(new ItemStack(item, 1, meta), burnTime, energyPerTick);
    }

    /**
     *  @param stack the fuelFurnace to be registered. Must be of size 1.
     *  @param burnTime its burn time.
     *  @throws NullPointerException if the given stack is null
     */
    public static void registerFuel(ItemStack stack, int burnTime, int energyPerTick) throws NullPointerException {
        if(stack == null)
            throw new NullPointerException("Given ItemStack is null!");

        if (stack.stackSize == 1)
            for (int id : OreDictionary.getOreIDs(stack))
                biomassGenBurnTime.put(OreDictionary.getOreName(id), new Integer[]{burnTime, energyPerTick});
    }

    /**@return the burn time of the provided fuelFurnace. Returns -1 if the fuelFurnace isn't valid.*/
    public static int getBurnTime(ItemStack stack) {
        if(isValidFuel(stack))
            for (int id : OreDictionary.getOreIDs(stack))
             if(biomassGenBurnTime.containsKey(OreDictionary.getOreName(id)))
                 return biomassGenBurnTime.get(OreDictionary.getOreName(id))[0];
        return -1;
    }

    /**@return the energy per tick of the provided fuelFurnace. Returns -1 if the fuelFurnace isn't valid.*/
    public static int getEnergyPerTick(ItemStack stack) {
        if(isValidFuel(stack))
            for (int id : OreDictionary.getOreIDs(stack))
                if(biomassGenBurnTime.containsKey(OreDictionary.getOreName(id)))
                    return biomassGenBurnTime.get(OreDictionary.getOreName(id))[1];
        return -1;
    }

    /**@return this fuel charateristics {burnTime, energyPerTick}*/
    public static int[] getCharateristics(ItemStack stack) {
        if(isValidFuel(stack))
            for (int id : OreDictionary.getOreIDs(stack))
                if(biomassGenBurnTime.containsKey(OreDictionary.getOreName(id)))
                    return Arrays.stream(biomassGenBurnTime.get(OreDictionary.getOreName(id)))
                            .mapToInt(i->i).toArray();
        return new int[0];
    }

    /**@return true if and only if this itemstack is registered as fuelFurnace.*/
    public static boolean isValidFuel(ItemStack stack) {
        if(stack==null)
            return false;
        for (int id : OreDictionary.getOreIDs(stack))
            if(biomassGenBurnTime.containsKey(OreDictionary.getOreName(id)))
                return true;
        return false;
    }

    public static final int energyToHeatUp = 150000;

    public static int getEnergyOutputThisTick(ItemStack... fuels) {
        int totEnergy = 0;

        for(ItemStack stack : fuels)
            totEnergy += getEnergyPerTick(stack)*stack.stackSize;

        return totEnergy;
    }

    /**@return the needed time to head up the generator using this exact fuel mix.*/
    public static int getHeatupTime(ItemStack[] stacks, int energyHeat) {
        return (energyToHeatUp-energyHeat)/getEnergyOutputThisTick(stacks);
    }

    /**@return the needed time to head up the generator using this fuel.*/
    public static int getHeatupTime(ItemStack stack, int energyHeat) {
        return getHeatupTime(stack, energyHeat, true);
    }

    /**@return the needed time to heat up the generator using this fuel.
     * @param calcBySize if true, the time is*/
    public static int getHeatupTime(ItemStack stack, int energyHeat, boolean calcBySize) {
        return energyHeat>=energyToHeatUp?0:(energyToHeatUp-energyHeat) / (getEnergyPerTick(stack) * (calcBySize?stack.stackSize:1));
    }
}
