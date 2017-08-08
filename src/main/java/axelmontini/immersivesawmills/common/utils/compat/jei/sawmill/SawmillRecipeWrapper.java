package axelmontini.immersivesawmills.common.utils.compat.jei.sawmill;

import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Axel Montini on 16/07/2017.
 */
public class SawmillRecipeWrapper extends BlankRecipeWrapper {
    private final ItemStack input;
    private final ItemStack output;
    private final ItemStack woodchips;
    public SawmillRecipeWrapper(SawmillRecipe recipe)
    {
        this.input = recipe.input.stack;
        this.output = recipe.output;
        this.woodchips = recipe.woodchips;
    }


    @Override
    public void getIngredients(IIngredients ingredients)
    {
        ingredients.setInput(ItemStack.class, input);
        ingredients.setOutputs(ItemStack.class, Arrays.asList(output, woodchips));
    }

    public ItemStack getWoodOutput()
    {
        return output;
    }
    public ItemStack getWoodchipsOutput()
    {
        return woodchips;
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
    {
    }
}
