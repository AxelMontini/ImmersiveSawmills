package axelmontini.immersivesawmills.client;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import axelmontini.immersivesawmills.client.render.TileRenderBiomassGenerator;
import axelmontini.immersivesawmills.client.render.TileRenderSawmill;
import axelmontini.immersivesawmills.common.CommonProxy;
import axelmontini.immersivesawmills.common.ISContent;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntityBiomassGenerator;
import axelmontini.immersivesawmills.common.blocks.metal.TileEntitySawmill;
import axelmontini.immersivesawmills.common.items.ItemISBase;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.Locale;

/**
 * Created by Axel Montini on 11/07/2017.
 */
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        ClientUtils.mc().getFramebuffer().enableStencil();//Enabling FBO stencils TODO Verify usefulness and decide if keep or remove
        OBJLoader.INSTANCE.addDomain("immersivesawmills");
    }

    @Override
    public void preInitEnd(FMLPreInitializationEvent event) {
        //Going through registered stuff at the end of preInit, because of compat modules possibly adding items
        for(Block block : ISContent.registeredISBlocks)
        {
            Item blockItem = Item.getItemFromBlock(block);
            final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);
            if(block instanceof IEBlockInterfaces.IIEMetaBlock)
            {
                IIEMetaBlock ieMetaBlock = (IEBlockInterfaces.IIEMetaBlock) block;
                if(ieMetaBlock.useCustomStateMapper())
                    ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
                ModelLoader.setCustomMeshDefinition(blockItem, (ItemStack stack) -> new ModelResourceLocation(loc, "inventory"));
                for(int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++)
                {
                    String location = loc.toString();
                    String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
                    if(ieMetaBlock.useCustomStateMapper())
                    {
                        String custom = ieMetaBlock.getCustomStateMapping(meta, true);
                        if(custom != null)
                            location += "_" + custom;
                    }
                    try
                    {
                        ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
                    } catch(NullPointerException npe)
                    {
                        throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
                    }
                }
            }
            else
                ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
        }

        for(Item item : ISContent.registeredISItems)
        {
            if(item instanceof ItemISBase)
            {
                ItemISBase ipMetaItem = (ItemISBase) item;
                if(ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0)
                {
                    for(int meta = 0; meta < ipMetaItem.getSubNames().length; meta++)
                    {
                        ResourceLocation loc = new ResourceLocation(ImmersiveSawmills.modid, ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);

                        ModelBakery.registerItemVariants(ipMetaItem, loc);
                        ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
                    }
                }
                else
                {
                    final ResourceLocation loc = new ResourceLocation(ImmersiveSawmills.modid, ipMetaItem.itemName);
                    ModelBakery.registerItemVariants(ipMetaItem, loc);
                    ModelLoader.setCustomMeshDefinition(ipMetaItem, stack -> new ModelResourceLocation(loc, "inventory"));
                }
            }
            else
            {
                final ResourceLocation loc = GameData.getItemRegistry().getNameForObject(item);
                ModelBakery.registerItemVariants(item, loc);
                ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(loc, "inventory"));
            }
        }
    }

    @Override
    public void init(FMLInitializationEvent e) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySawmill.class, new TileRenderSawmill());
//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBiomassGenerator.class, new TileRenderBiomassGenerator());
    }
    @Override
    public void postInit(FMLPostInitializationEvent e) {

    }

    @Override
    public void spawnParticleOnlyClient(World world, EnumParticleTypes type, double x, double y, double z, double speedX, double speedY, double speedZ, int... params) {
        world.spawnParticle(type, x,y,z, speedX, speedY, speedZ, params);
    }
}
