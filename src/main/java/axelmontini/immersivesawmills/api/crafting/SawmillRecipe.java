package axelmontini.immersivesawmills.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to define Sawmill recipes
 */
public class SawmillRecipe extends MultiblockRecipe {
    public static float energyModifier = 1, timeModifier = 1;
    public static List<SawmillRecipe> recipeList = new ArrayList();
    public final IngredientStack input;
    public final ItemStack output;
    public final ItemStack woodchips;

    int totalProcessTime;
    int totalProcessEnergy;

    public SawmillRecipe(ItemStack output, ItemStack woodchips, Object input, int energy, int time) {
        this.output = output;
        this.woodchips = woodchips;
        this.input = ApiUtils.createIngredientStack(input);
        this.totalProcessEnergy = (int) Math.floor(energy * energyModifier);
        this.totalProcessTime = (int) Math.floor(time * timeModifier);

        this.outputList = Lists.newArrayList(this.output);
        this.inputList = Lists.newArrayList(this.input);
    }

    public static SawmillRecipe addRecipe(ItemStack output, ItemStack woodchips, Object input, int energy, int time) {
        SawmillRecipe recipe = new SawmillRecipe(output, woodchips, input, energy, time);
        if (recipe != null)
            recipeList.add(recipe);
        return recipe;
    }

    public static SawmillRecipe findRecipe(ItemStack input) {
        for (SawmillRecipe recipe : recipeList) {
            if (recipe.input != null && recipe.matches(input))
                return recipe;
        }
        return null;
    }

    @Override
    public int getMultipleProcessTicks() {
        return 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("input", input.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override
    public void setupJEI() {
        super.setupJEI();
        jeiItemInputList = new ArrayList[1];
        jeiItemInputList[0] = Lists.newArrayList(jeiTotalItemInputList);
        jeiTotalItemOutputList.add(woodchips);
    }

    public boolean matches(ItemStack input) {
        return this.input.matches(input);
    }


    @Override
    public int getTotalProcessTime() {
        return totalProcessTime;
    }

    @Override
    public int getTotalProcessEnergy() {
        return totalProcessEnergy;
    }

    public static SawmillRecipe loadFromNBT(NBTTagCompound nbt) {
        IngredientStack input = IngredientStack.readFromNBT(nbt.getCompoundTag("input"));
        return findRecipe(input.stack);
    }
}
