package axelmontini.immersivesawmills.common;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import axelmontini.immersivesawmills.api.crafting.SawmillRecipe;
import axelmontini.immersivesawmills.common.blocks.BlockISBase;
import axelmontini.immersivesawmills.common.blocks.metal.BlockMetalMultiblocks;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import axelmontini.immersivesawmills.common.blocks.multiblock.MultiblockSawmill;
import axelmontini.immersivesawmills.common.items.ItemWoodchips;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import static axelmontini.immersivesawmills.common.Config.ISConfig.Sawmill.*;

import java.util.ArrayList;
import java.util.List;


public class ISContent {
    public static ArrayList<Block> registeredISBlocks = new ArrayList();

    public static ArrayList<Item> registeredISItems = new ArrayList();

    //Items
    public static Item      itemWoodchips;
    //Blocks
    public static BlockISBase blockMetalMultiblock;

    /**Fuel Handler*/
    public static IFuelHandler isFuelHandler = (ItemStack s) -> {
        if(s.getItem() instanceof IFuel)
            return ((IFuel) s.getItem()).getBurnTime();
        else
            return 0;
    };

    public static void preInit(FMLPreInitializationEvent event)
    {
        itemWoodchips = new ItemWoodchips();

        blockMetalMultiblock = new BlockMetalMultiblocks();

        //Register multiblocks
        MultiblockHandler.registerMultiblock(new MultiblockSawmill());
    }

    public static void init(FMLInitializationEvent event)
    {
        registerTile(TileEntitySawmill.class);

        GameRegistry.registerFuelHandler(isFuelHandler);
    }

    public static void postInit(FMLPostInitializationEvent event) {
        //Cross-mod wood milling recipes
        List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();
        recipeList.parallelStream()
                .filter(recipe -> recipe instanceof ShapedRecipes)  //Filter only shapedrecipes
                .filter(recipe -> recipe.getRecipeOutput() != null) //Filter only with valid output
                .filter(recipe -> recipe.getRecipeSize() == 1)      //Filter only with size of 1
                .filter( recipe -> OreDictionary.getOres("plankWood").parallelStream().anyMatch(ore -> ore.getItem() == recipe.getRecipeOutput().getItem()) )    //Only wood recipes (check with oredictionary)
                .forEachOrdered(recipe -> {
                    ShapedRecipes shaped = (ShapedRecipes) recipe;
                    ItemStack craftingOutput = shaped.getRecipeOutput();
                    SawmillRecipe.addRecipe(
                            new ItemStack(craftingOutput.getItem(), (int) Math.floor(craftingOutput.stackSize*planksCountModifier), craftingOutput.getMetadata()),
                            new ItemStack((ItemWoodchips) ItemWoodchips.instance, (int) Math.floor(2*woodchipsCountMultiplier)),
                            shaped.recipeItems[0], energyPerLog, timePerLog);
                    ImmersiveSawmills.log.info("Detected wood recipe ({} -> {})! Added variant to Sawmill recipes...", shaped.recipeItems[0], shaped.getRecipeOutput());
                });

    }

    public static void registerTile(Class<? extends TileEntity> tile)
    {
        String s = tile.getSimpleName();
        s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
        GameRegistry.registerTileEntity(tile, ImmersiveSawmills.modid+":"+ s);
    }
}

