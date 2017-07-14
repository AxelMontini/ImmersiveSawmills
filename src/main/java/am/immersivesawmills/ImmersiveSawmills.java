package am.immersivesawmills;

import am.immersivesawmills.common.CommonProxy;
import am.immersivesawmills.common.ISContent;
import am.immersivesawmills.common.block.multiblock.MultiblockSawmill;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Axel Montini on 04.06.2017.
 */
@Mod(modid = ImmersiveSawmills.modid, version = ImmersiveSawmills.version, dependencies = "required-after:immersiveengineering;")
public class ImmersiveSawmills {
    public static final String modid="immersivesawmills", version = "@VERSION@";
    public static Logger log;

    //Proxy
    @SidedProxy(clientSide = "am.immersivesawmills.client.ClientProxy", serverSide = "am.immersivesawmills.common.CommonProxy")
    public CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log  = LogManager.getLogger("Immersive Sawmills");
        log.info("Initializing Core Things! (Pre Init)");

        ISContent.preInit(event);

        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        log.info("Initializing Things that depends on Core Things!");
        ISContent.init(event);
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        log.info("Initializing Things that depends on Things depending on Core Things!");
        ISContent.postInit(event);
        proxy.postInit(event);
    }
}
