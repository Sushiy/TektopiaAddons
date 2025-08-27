package com.sushiy.tektopiaaddons;

import com.leviathanstudio.craftstudio.client.registry.CSRegistryHelper;
import com.leviathanstudio.craftstudio.client.registry.CraftStudioLoader;
import com.leviathanstudio.craftstudio.client.util.EnumRenderType;
import com.leviathanstudio.craftstudio.client.util.EnumResourceType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockOre;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

@Mod(
	modid = TektopiaAddons.MODID,
	name = TektopiaAddons.NAME,
	version = TektopiaAddons.VERSION
)
public class TektopiaAddons {
	public static final String MODID = "tektopiaaddons";
	public static final String NAME = "Tekotpia Addons";
	public static final String VERSION = "1.4.6";
	
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static File config;

	public static HashSet<Item> seedItems;
	public static HashSet<Item> cropItems;
	public static HashSet<BlockCrops> cropBlocks;
	public static HashSet<ItemFood> standardFoodItems;

	public static class FoodStats
	{
		public int hunger;
		public int happiness;

		public FoodStats(int _hunger, int _happiness)
		{
			hunger = _hunger;
			happiness = _happiness;
		}
	}
	public static HashMap<Item, FoodStats> configFoodItems;

	public static HashSet<Item> oreItems;
	public static HashSet<Item> dustItems;
	public static HashSet<Item> gemItems;
	public static HashSet<Item> ingotItems;
	public static HashSet<Block> stoneBlocks;
	public static HashSet<BlockOre> oreBlocks;

	public static HashMap<Block, Integer> oreWeights;
	public static int totalOreWeight;

	public static HashMap<Item, Integer> smithIngotPriority;
	public static HashMap<ItemStack, ItemStack> reverseIngotFurnaceList;

	public static HashSet<String> ignoredMonsters;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent preinit) {
		ConfigHandler.registerConfig(preinit);
		OreDictionary.registerOre("cropBeetroot",Items.BEETROOT);
	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent postinit)
	{
		LOGGER.info(MODID + " Creating item lists");
		Collection<Item> items = ForgeRegistries.ITEMS.getValuesCollection();
		LOGGER.info(MODID + " Found " + items.size() + " items");

		//Food & Farming
		seedItems = new HashSet<Item>();
		cropItems = new HashSet<Item>();
		cropBlocks = new HashSet<BlockCrops>();
		standardFoodItems = new HashSet<ItemFood>();
		//Mining
		oreBlocks = new HashSet<BlockOre>();
		oreItems = new HashSet<Item>();
		dustItems = new HashSet<Item>();
		gemItems = new HashSet<Item>();
		ingotItems = new HashSet<Item>();
		stoneBlocks = new HashSet<Block>();

		oreWeights = new HashMap<>();

		//Smithing
		smithIngotPriority = new HashMap<>();
		reverseIngotFurnaceList = new HashMap<>();

		configFoodItems = new HashMap<>();
		//Read foodConfig
		for(String foodString : ConfigHandler.MODDED_FOOD_CUSTOM_STATS)
		{
			//remove any whitespace
			foodString = foodString.replace(" ", "");
			//Split by commas
			String[] split = foodString.split(",");
			if(split.length != 3)
			{
				LOGGER.info(MODID + " Config food item " + split[0] + "not setup correctly");
				continue;
			}
			Item item = Item.getByNameOrId(split[0]);
			if(item != null)
			{
				configFoodItems.put(item, new FoodStats(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
			}
			else
			{
				LOGGER.info(MODID + " Config food item " + split[0] + "does not exist");
			}
		}

		Random rand = new Random();
        for(Item item : items)
		{
			ItemStack stack = new ItemStack(item);
			if(stack.isEmpty()) continue;
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("crop")))
			{
				cropItems.add(item);
				LOGGER.info("Found Crop: " + item.getRegistryName());
			}
			if(item instanceof ItemSeeds)
			{
				seedItems.add(item);
				Block b = ((ItemSeeds)item).getPlant(null, null).getBlock();
				if(b instanceof BlockCrops)
				{
					cropBlocks.add((BlockCrops)b);
				}
				//LOGGER.info("Found Seed: " + item.getRegistryName());
			}
			else if(item instanceof ItemSeedFood)
			{
				seedItems.add(item);
				cropBlocks.add((BlockCrops) ((ItemSeedFood)item).getPlant(null, null).getBlock());
				//LOGGER.info("Found Seed: " + item.getRegistryName());
			}
			//FOODITEMS
			if(item instanceof  ItemFood)
			{
				if(!configFoodItems.containsKey(item))
					standardFoodItems.add((ItemFood)item);
				//LOGGER.info("Found Food: " + item.getRegistryName());
			}
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("ore")))
			{
				//Only add "ores" that drop themselves
				Block block = Block.getBlockFromItem(item);
				if(block.getRegistryName().getPath().contains("fossil")) continue;
				if(block.getItemDropped(block.getDefaultState(), rand, 0) == item)
				{
					oreItems.add(item);
					//LOGGER.info("Found OreItem: " + item.getRegistryName());
				}
				else
				{
					//LOGGER.info("Skipped OreItem: " + item.getRegistryName());
				}
			}
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("dust")))
			{
				dustItems.add(item);
				//LOGGER.info("Found DustItem: " + item.getRegistryName());
			}
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("gem")))
			{
				gemItems.add(item);
				//LOGGER.info("Found GemItem: " + item.getRegistryName());
			}
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("ingot") && !(OreDictionary.getOreName(x).startsWith("ingotBrick"))))
			{
				ingotItems.add(item);
				//LOGGER.info("Found IngotItem: " + item.getRegistryName());
			}

		}
		LOGGER.info("Found " + seedItems.size() + " seeds");
		LOGGER.info("Found " + cropItems.size() + " crops");
		LOGGER.info("Found " + standardFoodItems.size() + " foods");
		LOGGER.info("Found " + oreItems.size() + " ores");
		LOGGER.info("Found " + dustItems.size() + " dusts");
		LOGGER.info("Found " + gemItems.size() + " gems");
		LOGGER.info("Found " + ingotItems.size() + " ingots");


		Collection<Block> blocks = ForgeRegistries.BLOCKS.getValuesCollection();
		for(Block block : blocks)
		{
			ItemStack stack = new ItemStack(block);
			if(stack.isEmpty()) continue;
			if(block instanceof BlockOre && Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("ore")))
			{
				oreBlocks.add((BlockOre) block);
				//LOGGER.info("Found OreBlock: " + stack.getItem().getRegistryName());
			}
			if(Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).startsWith("stone")) || Arrays.stream(OreDictionary.getOreIDs(stack)).anyMatch(x -> OreDictionary.getOreName(x).contains("cobble")) )
			{
				stoneBlocks.add(block);
				//LOGGER.info("Found StoneBlock: " + stack.getItem().getRegistryName());
			}
		}

		LOGGER.info("Found " + oreBlocks.size() + " oreBlocks");
		LOGGER.info("Found " + stoneBlocks.size() + " stones");

		totalOreWeight = 0;
		for(BlockOre ore : oreBlocks)
		{
			int[] ids = OreDictionary.getOreIDs(new ItemStack(Item.getItemFromBlock(ore)));
			for(String s : ConfigHandler.MODDED_ORE_REGROW_CHANCE)
			{
				String[] split = s.split(":");
				if(Arrays.stream(ids).anyMatch(x -> OreDictionary.getOreName(x).contains(split[0])))
				{
					//Found an ore from my list
					int i = Integer.parseInt(split[1]);
					oreWeights.put(ore, i);
					totalOreWeight += i;
				}
				else
				{
					oreWeights.put(ore, 10);
					totalOreWeight += 10;
				}
			}
		}

		HashMap<String, Integer> priorityStringMap = new HashMap<>();
		for(String s : ConfigHandler.MODDED_METAL_SMITH_PRIORITY)
		{
			String[] split = s.split(":");
			if(split.length == 2)
			{
				try
				{
					String INPUT = split[0].toLowerCase();
					String metal = INPUT.substring(0, 1).toUpperCase() + INPUT.substring(1);
					priorityStringMap.put(metal, Integer.valueOf(split[1]));
				}
				catch (NumberFormatException e)
				{
                    TektopiaAddons.LOGGER.info(TektopiaAddons.MODID + "smithing metal priority config has bad entry: {}", s);
				}
			}
		}

		for(Item ingot : ingotItems)
		{
			int id = OreDictionary.getOreIDs(new ItemStack(ingot))[0];
			for(String metal : ConfigHandler.MODDED_METAL_SMITH_PRIORITY)
			{
				if(OreDictionary.getOreName(id).endsWith(metal))
				{
					smithIngotPriority.put(ingot, priorityStringMap.get(metal));
				}
			}
		}

		if (Loader.isModLoaded("magistuarmory"))
		{
			String Version = FMLCommonHandler.instance().findContainerFor("magistuarmory").getVersion();
			TektopiaAddons.LOGGER.info("magistuarmory " + Version + " detected");
		}


		LOGGER.info(MODID + " Creating entity lists");
		Collection<EntityEntry> entities = ForgeRegistries.ENTITIES.getValuesCollection();
		LOGGER.info(MODID + " Found " + entities.size() + " entities");

		ignoredMonsters = new HashSet<String>();
		for(String ignoredMonster : ConfigHandler.MONSTER_IGNORE_LIST)
		{
			LOGGER.info(MODID + " Ignored monster List: " + ignoredMonster);
		}
		for(EntityEntry entity : entities)
		{
			LOGGER.info(MODID + " Found Entity: " + entity.getName());
			for(String ignoredMonster : ConfigHandler.MONSTER_IGNORE_LIST)
			{
				if(ignoredMonster.equals(entity.getName()))
				{
					LOGGER.info(MODID + " Ignored monster Entity: " + entity.getName());
					ignoredMonsters.add(entity.getName());
				}
			}
		}
	}

	@CraftStudioLoader
	public static void loadStuff() {
		try
		{
			CSRegistryHelper registry = new CSRegistryHelper("tektopiaaddons");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "guard_body");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "armor_head");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "armor_chest");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "armor_leg");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "armor_feet");
			registry.register(EnumResourceType.MODEL, EnumRenderType.ENTITY, "armor_surcoat");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isOreBlock(Block block)
	{
		return block instanceof BlockOre && oreBlocks.contains((BlockOre) block);
	}

	public static boolean isCropBlock(Block block)
	{
		return block instanceof BlockCrops && cropBlocks.contains((BlockCrops)block);
	}

	public static String getPlantAIFilterName(Block block)
	{
		String result = "plant_tile.";

		if(block.getRegistryName().getNamespace().equals("minecraft"))
		{
			result += block.getRegistryName().getPath();
			if(result.equals("plant_tile.wheat"))
			{
				result = "plant_tile.crops";
			}
		}
		else
		{
			result += "modded";
		}

		return result;
	}
	public static String getHarvestAIFilterName(Block block)
	{
		String result = "harvest_tile.";

		if(block.getRegistryName().getNamespace().equals("minecraft"))
		{
			result += block.getRegistryName().getPath();
			if(result.equals("harvest_tile.wheat"))
			{
				result = "harvest_tile.crops";
			}
		}
		else
		{
			result += "modded";
		}

		return result;
	}
}
