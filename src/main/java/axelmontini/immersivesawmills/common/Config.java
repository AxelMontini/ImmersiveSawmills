package axelmontini.immersivesawmills.common;

import axelmontini.immersivesawmills.ImmersiveSawmills;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Config {
    public static HashMap<String, Integer> burnTimes = new HashMap();
//    public static List<JSONConfigFile> jsonFiles = new ArrayList();
    public static HashMap<String, Integer[]> biomassBurnTimes = new HashMap();

    @SuppressWarnings("unused")
    @net.minecraftforge.common.config.Config(modid= ImmersiveSawmills.modid)
    public static class ISConfig {
        //General config
        @Comment({"Sawmill config."})
        public static Sawmill sawmill = new Sawmill();

        //Other configs
        @Comment({"Fuel settings for this mod's items/blocks"})
        public static FuelFurnace fuelFurnace = new FuelFurnace();

        @Comment({"Biomass Fuel settings. Add your own settings following the same format. (Item name, values)"})
        public static BiomassFuel biomassFuel = new BiomassFuel();

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

        public static class FuelFurnace {
            @Comment({"Burn time in furnace, in ticks."})
            public static int itemWoodchips = 100;
        }

        public static class BiomassFuel {
            /**Biomass fuel entry in json config.*/
            private static class BiomassConfigEntry {
                public BiomassConfigEntry(String namespacedName, int burnTime, int energyPerTick) {
                    this.namespacedName = namespacedName;
                    this.burnTime = burnTime;
                    this.energyPerTick = energyPerTick;
                }

                public String getNamespacedName() {
                    return namespacedName;
                }

                public int getBurnTime() {
                    return burnTime;
                }

                public int getEnergyPerTick() {
                    return energyPerTick;
                }

                /**Example minecraft:dirt:0*/
                public String namespacedName;
                /**Burn time*/
                public int burnTime;
                /**Energy output in one tick*/
                public int energyPerTick;
            }

            private static BiomassConfigEntry[] def = new BiomassConfigEntry[] {
                    new BiomassConfigEntry(ImmersiveSawmills.modid+":woodchips", 20, 300)
            };

            @Comment({"Map of fuel values. Format I:\"TextualID\" <\n\tBurnTime\n\tEnergyPerTick\n>"})
            public static Map<String, Integer[]> burnTimes = Arrays.asList(def).parallelStream().collect(Collectors.toMap(
                    BiomassConfigEntry::getNamespacedName, value -> new Integer[]{value.burnTime, value.energyPerTick}
            ));
        }
    }

    public static void preInit(FMLPreInitializationEvent event) {
        String unlocalizedName = null;
        try {
            //replace the field name like so: itemSomethingCool -> item.immersiveengineering.somethingCool
            //and then add it to the map. This means that config files need to be written in correct case.
            for (Field field : ISConfig.FuelFurnace.class.getFields()) {
                unlocalizedName = field.getName().replaceFirst("((block)|(item))", "$1."+ImmersiveSawmills.modid+".");
                int lastDot = unlocalizedName.lastIndexOf(".");
                unlocalizedName = unlocalizedName.substring(0, lastDot+1)+String.valueOf(unlocalizedName.charAt(lastDot+1)).toLowerCase()+unlocalizedName.substring(lastDot+2);
                burnTimes.put(unlocalizedName, field.getInt(ISConfig.fuelFurnace));
            }
        } catch(IllegalAccessException e) {
            System.err.printf("Failed to get burn time of item\"%s\" from Config file.", unlocalizedName);
            e.printStackTrace();
        }

        biomassBurnTimes.putAll(ISConfig.BiomassFuel.burnTimes);

        /*//Handle JSON Files
        createDefaultJSONFiles(event.getModConfigurationDirectory());*/
    }

/*    *//**@return true if at least a file has been created.*//*
    private static boolean createDefaultJSONFiles(File configFolder) throws FileNotFoundException {
        if(!configFolder.isDirectory())
            throw new FileNotFoundException("The mod config directory isn't actually a directory? "+configFolder.getAbsolutePath());

        Gson gson = new Gson();

        boolean createdNew = false;

        new JSONConfigFile() {
            @Override
            String getConfigFileName() {
                return "BiomassRegistry.json";
            }
        };

        for(JSONConfigFile cf : jsonFiles) {
            try {
                File cfgJson = new File(configFolder, cf.getConfigFileName());
                if (!cfgJson.exists()) {
                    createdNew = createdNew || cfgJson.createNewFile();   //Create new file
                    FileWriter fw = new FileWriter(cfgJson);
                    gson.toJson(cf.entries, fw);    //Write entries to json if not existing
                    fw.flush();
                    fw.close();
                } else {    //Else, read the file.
                    FileReader fr = new FileReader(cfgJson);
                    cf.entries = gson.fromJson(fr, Object[].class);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }*/
/*
    public abstract static class JSONConfigFile {
        private Object[]
        abstract Object[] getEntries();
        abstract String getConfigFileName();
        public JSONConfigFile() {
            jsonFiles.add(this);
        }
    }

    */
}

