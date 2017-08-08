package axelmontini.immersivesawmills.common.utils.compat.jei.sawmill;

import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.utils.compat.jei.ISRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SawmillRecipeCategory extends ISRecipeCategory<SawmillRecipe, SawmillRecipeWrapper> {
    public static ResourceLocation background = new ResourceLocation("immersivesawmills:textures/gui/sawmill.png");

    //BAckground properties
    public static int xOff = 14, yOff = 14, xSize = 200-14*2, ySize = 200-14*2;

    public SawmillRecipeCategory(IGuiHelper helper) {
        super("sawmill", "gui.immersivesawmills.sawmill",
                helper.createDrawable(background, xOff, yOff, xSize, ySize),
                SawmillRecipe.class, new ItemStack(ISContent.blockMetalMultiblock));
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, SawmillRecipeWrapper sawmillRecipeWrapper) {

    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SawmillRecipeWrapper recipeWrapper, IIngredients ingredients) {
        int xPos0 = (int) ((xSize-xOff)*1/6), yPos0 = (int) ((ySize-yOff)*1/3);
        int xPos1 = (int) ((xSize-xOff)*5/6), yPos1 = (int) ((ySize-yOff)*1/3);
        int xPos2 = (int) ((xSize-xOff)*4/6), yPos2 = (int) ((ySize-yOff)*2/3);

        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, xPos0 , yPos0);
        guiItemStacks.init(1, false, xPos1, yPos1);
        guiItemStacks.init(2, false, xPos2, yPos2);
        guiItemStacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
        guiItemStacks.set(1, recipeWrapper.getWoodOutput());
        guiItemStacks.set(2, recipeWrapper.getWoodchipsOutput());
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(SawmillRecipe sawmillRecipe) {
        return new SawmillRecipeWrapper(sawmillRecipe);
    }
}
