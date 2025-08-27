package com.sushiy.tektopiaaddons;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class ConfigHandler {
    public static Configuration config;

    public static float MODDED_FOOD_HUNGER_MULTIPLIER = 1.5f;
    public static float MODDED_FOOD_HAPPINESS_MULTIPLIER = 1.5f;

    public static String[] MODDED_FOOD_CUSTOM_STATS_DEFAULT = {};
    public static String[] MODDED_FOOD_CUSTOM_STATS;

    public static String[] MODDED_METAL_SMITH_PRIORITY_DEFAULT = {"Copper:1", "Bronze:3", "Silver:4", "Steel:5"};
    public static String[] MODDED_METAL_SMITH_PRIORITY;


    public static String[] MODDED_ORE_REGROW_CHANCE_DEFAULT = {"Stone:30","Coal:30", "Iron:25", "Gold:5", "Redstone:12", "Lapis:3", "Diamond:3", "Copper:30", "Silver:15", "Tin:10"};
    public static String[] MODDED_ORE_REGROW_CHANCE;


    public static int VILLAGER_SKILLRATE = 100;
    public static int VILLAGE_RADIUS = 100;
    public static int VILLAGE_ANIMALPEN_SIZE_PERCENTAGE_MULTIPLIER = 100;
    public static boolean NEW_PLAYERS_RECEIVE_STARTERBOOK = false;

    public static String[] MONSTER_IGNORE_LIST;
    public static String[] MONSTER_IGNORE_LIST_DEFAULT = {"Creeper", "Witch", "Enderman"};

    public static void init(File file)
    {
        config = new Configuration(file);

        String category;
        category = "Base Mod Config";
        config.addCustomCategoryComment(category, "Settings related to the base mod");
        //VILLAGER_SKILLRATE = config.getInt("Rate at which villagers gain skills", category, 100, 0, 10000, "default 100");
        //VILLAGE_RADIUS = config.getInt("Radius of village ", category, 100, 0, 300, "!!Change with caution!!.default 100");
        VILLAGE_ANIMALPEN_SIZE_PERCENTAGE_MULTIPLIER = Math.round(config.getFloat("Multiplier for animals in a pen", category, 1, 0, 10, "default 1")* 100);
        NEW_PLAYERS_RECEIVE_STARTERBOOK = config.getBoolean("Should new players get a starterbook", category, false, "");


        category = "Food";
        config.addCustomCategoryComment(category, "Settings related to food consumption");
        MODDED_FOOD_HUNGER_MULTIPLIER = config.getFloat("Modded food hunger multiplier", category, 1.5f, 1.0f, 5.0f, "Multiplier for modded food hunger value");
        MODDED_FOOD_HAPPINESS_MULTIPLIER = config.getFloat("Modded food happiness multiplier", category, 1.0f, 1.0f, 5.0f, "Multiplier for modded food happiness value");
        MODDED_FOOD_CUSTOM_STATS = config.getStringList("Modded food custom stats", category, MODDED_FOOD_CUSTOM_STATS_DEFAULT, "List of custom stats for fooditems. you can also add additional food items here that didn't get picked up automatically. vanilla item values are:" +
                "minecraft:apple, 12, -1\n" +
                "minecraft:baked_potato, 35, 1\n" +
                "minecraft:beetroot, 7, -1\n" +
                "minecraft:beetroot_soup, 50, 6, returnBowl\n" +
                "minecraft:bread, 55, 4\n" +
                "minecraft:cake, 7, 25\n" +
                "minecraft:carrot, 12, -1\n" +
                "minecraft:cooked_beef, 70, 14\n" +
                "minecraft:cooked_chicken, 60, 6\n" +
                "minecraft:cooked_mutton, 66, 4\n" +
                "minecraft:cooked_porkchop, 70, 14\n" +
                "minecraft:cookie, 5, 16\n" +
                "minecraft:golden_carrot, 70, 20\n" +
                "minecraft:melon, 6, 3\n" +
                "minecraft:mushroom_stew, 50, 4, returnBowl\n" +
                "minecraft:potato, 7, -1\n" +
                "minecraft:pumpkin_pie, 35, 18\n");


        category = "Smithing";
        MODDED_METAL_SMITH_PRIORITY = config.getStringList("Modded metal ingot priority", category, MODDED_METAL_SMITH_PRIORITY_DEFAULT, "Format: \"<Metalname>:<integer value>\" Metal should be capitalized. Higher numbers are crafted first by smith. Iron is priority 3. ");

        category = "Mining";
        MODDED_ORE_REGROW_CHANCE = config.getStringList("Modded ores regrow chance", category, MODDED_ORE_REGROW_CHANCE_DEFAULT, "Format: \"<oreName>:<integer value>\". Higher number is more likely to be found");

        category = "Combat";
        MONSTER_IGNORE_LIST = config.getStringList("Ignored Monsters List", category, MONSTER_IGNORE_LIST_DEFAULT, "Format: \"<monster_entity_name>\" This is the normal name in CamelCase. Typical: Skeleton, WitherSkeleton, Enderman, Spider, Zombie, etc.");

        config.save();
    }

    public static void registerConfig(FMLPreInitializationEvent event)
    {
        TektopiaAddons.config = new File(event.getModConfigurationDirectory() + "/" + TektopiaAddons.MODID);
        TektopiaAddons.config.mkdirs();
        init(new File(TektopiaAddons.config.getPath(), TektopiaAddons.MODID + ".cfg"));
    }
}
