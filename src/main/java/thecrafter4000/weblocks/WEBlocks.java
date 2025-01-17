package thecrafter4000.weblocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thecrafter4000.weblocks.addon.StairStateFactory;
import thecrafter4000.weblocks.addon.TrapdoorStateFactory;
import thecrafter4000.weblocks.addon.carpenters.CarpentersSlabStateFactory;
import thecrafter4000.weblocks.addon.carpenters.CarpentersStairStateFactory;

import java.lang.reflect.Field;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * WEBlocks, a WorldEdit fix to support other mod's blocks for rotating operations.
 * @author TheCrafter4000
 * @author Veritaris
 */
@Mod(modid = WEBlocks.MODID, version = WEBlocks.VERSION, name = WEBlocks.NAME, acceptableRemoteVersions = "*")
public class WEBlocks {
	public static final String MODID = "@modid@";
	public static final String VERSION = "@version@";
	public static final String NAME = "@name@";
		
	@Instance
	public static WEBlocks Instance = new WEBlocks();
	public static Logger Logger = LogManager.getLogger(NAME);
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModdedBlockRegistry.load(event.getModConfigurationDirectory());
		ModdedBlockRegistry.registerFactory(new StairStateFactory());
		ModdedBlockRegistry.registerFactory(new TrapdoorStateFactory());
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		inject(LegacyWorldData.class.getName(), "INSTANCE", "blockRegistry");
		inject("com.sk89q.worldedit.forge.ForgeWorldData", "INSTANCE", "blockRegistry");
		
		if(Loader.isModLoaded("CarpentersBlocks")) {
			Logger.info("Enabled Carpenter's Blocks support!");
			ModdedBlockRegistry.registerFactory(new CarpentersStairStateFactory());
			ModdedBlockRegistry.registerFactory(new CarpentersSlabStateFactory());
			//TODO: Make more factories
			//TODO: Add layer rotation support.
		}
	}
	
	/**
	 * Injects a modified block registry into an {@link LegacyWorldData} instance field.
	 * @param classToInject The class you want to inject in. Must be a subclass of {@link {@link LegacyWorldData}
	 * @param instanceField Name of the instance field. Mostly "INSTANCE".
	 * @param registryField Name of the block registry field. Mostly "blockRegistry".
	 */
	public static void inject(String classToInject, String instanceField, String registryField) {
		Object instance = null;
		
		try {
			Class<?> c = Class.forName(classToInject); // Loads class
			Field f = c.getDeclaredField(instanceField);
			f.setAccessible(true);
			instance = f.get(null); // Creating instance
		} catch (NoSuchFieldException e) {
			Logger.fatal("Did not resolve instance " + classToInject + "." + instanceField + "!", e);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			Logger.fatal("Failed to get " + classToInject + " instance!", e);
		} catch (ClassNotFoundException e) {
			Logger.fatal("Failed to load class: " + classToInject + "!", e);
		} 
		
		if (instance == null) {
			return;
		}
		
		try {
			Field f = LegacyWorldData.class.getDeclaredField(registryField);
			f.setAccessible(true);
			f.set(instance, ModdedBlockRegistry.INSTANCE);
			Logger.debug("Successfully replaced blockRegistry for " + classToInject + "!");
		}catch(NoSuchFieldException e) {
			Logger.fatal("Did not resolve blockRegistry field " + registryField + " in " + classToInject + "!", e);
		}catch (IllegalArgumentException | IllegalAccessException e) {
			Logger.fatal("Failed to overwrite blockRegistry for " + classToInject + "!", e);
		}
	}
	
	/** Copies array data into a map. The {@code key} is the String representation of the index, using {@link String#valueOf(int)}. */
	public static <V> Map<String, V> toImmutableMap(V[] data){
		checkNotNull(data);
		Builder<String, V> builder = ImmutableMap.builder();
		for(int i = 0; i < data.length; i++) {
			builder.put(String.valueOf(i), data[i]);
		}
		return builder.build();
	}
}
