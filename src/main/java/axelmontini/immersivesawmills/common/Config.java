package axelmontini.immersivesawmills.common;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Config {
    public static HashMap<String, Integer> burnTimes = new HashMap();

    @SuppressWarnings("unused")
    @net.minecraftforge.common.config.Config(modid= ImmersiveSawmills.modid)
    public static class ISConfig {
        //General config

        public static Sawmill sawmill = new Sawmill();

        //Other configs
        public static Fuel fuel = new Fuel();

        public static class Sawmill {
            @Comment({"The efficiency compared to Vanilla Recipes (output is (int)Math.floor(vanillaValue*planksCountModifier))"})
            public static float planksCountModifier = 2f;
            @Comment({"The woodchips production multiplier to apply to immersivesawmills recipes"})
            public static float woodchipsCountMultiplier = 1f;
            @Comment({"Energy used to process 1 log of wood."})
            public static int energyPerLog = 1000;
            @Comment({"Time spent to process 1 log of wood (ticks)."})
            public static int timePerLog = 60;
        }

        public static class Fuel {
            @Comment({"Burn time, in ticks."})
            public static int itemWoodchips = 100;
        }
    }

    public static void preInit(FMLPreInitializationEvent event) {
        String unlocalizedName = null;
        try {
            //replace the field name like so: itemSomethingCool -> item.immersiveengineering.somethingCool
            //and then add it to the map. This means that config files need to be written in correct case.
            for (Field field : ISConfig.Fuel.class.getFields()) {
                unlocalizedName = field.getName().replaceFirst("((block)|(item))", "$1."+ImmersiveSawmills.modid+".");
                int lastDot = unlocalizedName.lastIndexOf(".");
                unlocalizedName = unlocalizedName.substring(0, lastDot+1)+String.valueOf(unlocalizedName.charAt(lastDot+1)).toLowerCase()+unlocalizedName.substring(lastDot+2);
                burnTimes.put(unlocalizedName, field.getInt(ISConfig.fuel));
            }
        } catch(IllegalAccessException e) {
            System.err.printf("Failed to get burn time of item\"%s\" from Config file.", unlocalizedName);
            e.printStackTrace();
        }
    }
}
