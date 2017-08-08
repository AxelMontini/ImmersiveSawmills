package axelmontini.immersivesawmills.common.utils.compat.jei;


import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.utils.compat.jei.sawmill.SawmillRecipeCategory;
import mezz.jei.api.*;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;

import java.util.ArrayList;

@JEIPlugin
public class JEIHelper implements IModPlugin {

    public static IJeiHelpers jeiHelpers;
    public static IModRegistry modRegistry;
    public static IDrawable slotDrawable;

    @Override
    public void registerItemSubtypes(ISubtypeRegistry iSubtypeRegistry) {

    }

    @Override
    public void registerIngredients(IModIngredientRegistration iModIngredientRegistration) {

    }

    @Override
    public void register(IModRegistry registryIn) {
        this.modRegistry = registryIn;
        jeiHelpers = modRegistry.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        slotDrawable = guiHelper.getSlotDrawable();

        ISRecipeCategory[] categories = {
                new SawmillRecipeCategory(guiHelper)
        };

        modRegistry.addRecipeCategories(categories);
        modRegistry.addRecipeHandlers(categories);

        modRegistry.addRecipes(new ArrayList(SawmillRecipe.recipeList));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime iJeiRuntime) {

    }
}
