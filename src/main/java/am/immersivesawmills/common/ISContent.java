package am.immersivesawmills.common;

import am.immersivesawmills.common.block.multiblock.MultiblockSawmill;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by Axel Montini on 11/07/2017.
 */
public class ISContent {
    public static void preInit(FMLPreInitializationEvent event) {
        MultiblockHandler.registerMultiblock(new MultiblockSawmill());
    }

    public static void init(FMLInitializationEvent event) {

    }

    public static void postInit(FMLPostInitializationEvent event) {

    }
}
