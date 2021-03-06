package axelmontini.immersivesawmills;

import axelmontini.immersivesawmills.common.CommonProxy;
import axelmontini.immersivesawmills.common.Config;
import axelmontini.immersivesawmills.common.ISContent;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Axel Montini on 04.06.2017.
 */
@Mod(modid = ImmersiveSawmills.modid, version = ImmersiveSawmills.version, dependencies = "required-after:immersiveengineering;")
public class ImmersiveSawmills {
    public static final String modid="immersivesawmills", version = "@VERSION@";
    public static Logger log;
    public static final CreativeTabs creativeTab;

    //Proxy
    @SidedProxy(clientSide = "axelmontini.immersivesawmills.client.ClientProxy", serverSide = "axelmontini.immersivesawmills.common.CommonProxy")
    public static CommonProxy proxy;

    //Network wrapper
    public static final SimpleNetworkWrapper packetHandler;

    @SuppressWarnings("unused")
    @Mod.Instance(modid)
    public static ImmersiveSawmills instance;

    static {
//        FluidRegistry.enableUniversalBucket();
        packetHandler = NetworkRegistry.INSTANCE.newSimpleChannel(modid);

        creativeTab = new CreativeTabs(modid)
        {
            @Override
            public Item getTabIconItem()
            {
                return null;
            }
            @Override
            public ItemStack getIconItemStack()
            {
                return new ItemStack(ISContent.itemWoodchips,1,0);
            }
        };
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log  = LogManager.getLogger("Immersive Sawmills");
        log.info("Initializing Core Things! (Pre Init)");
        log.debug("Proxy Preinit");
        proxy.preInit(event);
        log.debug("Config Preinit");
        Config.preInit(event);
        log.debug("Content Preinit");
        ISContent.preInit(event);
        log.debug("Proxy Late-Preinit");
        proxy.preInitEnd(event);
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("Initializing Things that depend on Core Things!");
        ISContent.init(event);
        proxy.init(event);
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        log.info("Initializing Things that depend on Things depending on Core Things!");
        ISContent.postInit(event);
        proxy.postInit(event);
    }

    /**Used to register things*/
    public static <T extends IForgeRegistryEntry<?>> T register(T object, String name)
    {
        return registerByFullName(object, modid+":"+name);
    }
    public static <T extends IForgeRegistryEntry<?>> T registerByFullName(T object, String name)
    {
        object.setRegistryName(new ResourceLocation(name));
        return GameRegistry.register(object);
    }
    public static Block registerBlockByFullName(Block block, ItemBlock itemBlock, String name)
    {
        block = registerByFullName(block, name);
        registerByFullName(itemBlock, name);
        return block;
    }
    public static Block registerBlockByFullName(Block block, Class<? extends ItemBlock> itemBlock, String registryName) {
        try{return registerBlockByFullName(block, itemBlock.getConstructor(Block.class).newInstance(block), registryName);}
        catch(Exception e){e.printStackTrace();}
        return null;
    }
}
