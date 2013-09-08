package scripting;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.Configuration;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ContinuationPending;

import scripting.core.ScriptCore;
import scripting.core.ServerCore;
import scripting.core.rhino.SandboxContextFactory;
import scripting.defaultfilters.DefaultFilters;
import scripting.forge.Proxy;
import scripting.forge.ServerTickHandler;
import scripting.items.SelectorItem;
import scripting.network.ClientPacketHandler;
import scripting.network.ConnectionHandler;
import scripting.network.ServerPacketHandler;
import scripting.packet.ScriptPacket;
import scripting.packet.ScriptPacket.PacketType;
import scripting.wrapper.ScriptArray;
import scripting.wrapper.ScriptHelper;
import scripting.wrapper.ScriptIO;
import scripting.wrapper.ScriptItemStack;
import scripting.wrapper.ScriptRandom;
import scripting.wrapper.ScriptVec2;
import scripting.wrapper.ScriptVec3;
import scripting.wrapper.entity.ScriptDataWatcher;
import scripting.wrapper.entity.ScriptEntity;
import scripting.wrapper.nbt.TAG_Byte;
import scripting.wrapper.nbt.TAG_Byte_Array;
import scripting.wrapper.nbt.TAG_Compound;
import scripting.wrapper.nbt.TAG_Double;
import scripting.wrapper.nbt.TAG_Float;
import scripting.wrapper.nbt.TAG_Int;
import scripting.wrapper.nbt.TAG_Int_Array;
import scripting.wrapper.nbt.TAG_List;
import scripting.wrapper.nbt.TAG_Long;
import scripting.wrapper.nbt.TAG_Short;
import scripting.wrapper.nbt.TAG_String;
import scripting.wrapper.settings.Setting;
import scripting.wrapper.settings.SettingBlock;
import scripting.wrapper.settings.SettingBoolean;
import scripting.wrapper.settings.SettingFloat;
import scripting.wrapper.settings.SettingInt;
import scripting.wrapper.settings.SettingItem;
import scripting.wrapper.settings.SettingList;
import scripting.wrapper.settings.SettingString;
import scripting.wrapper.tileentity.ScriptTileEntity;
import scripting.wrapper.world.ScriptBlock;
import scripting.wrapper.world.ScriptBlockID;
import scripting.wrapper.world.ScriptItemID;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;


/*
 * TODO 
 *      Work on wrapping (focus on client + server scripts now)
 * 
 */
@Mod(modid=ScriptingMod.MOD_ID, name=ScriptingMod.NAME, version=ScriptingMod.VERSION, dependencies = "after:guilib")
@NetworkMod(clientSideRequired = false, serverSideRequired = false,
connectionHandler = ConnectionHandler.class,
clientPacketHandlerSpec = @SidedPacketHandler(channels = ScriptPacket.PACKET_ID, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = ScriptPacket.PACKET_ID, packetHandler = ServerPacketHandler.class))
public class ScriptingMod {

	public static final String MOD_ID = "Scripting";
	public static final String NAME = "In-Game MCEdit Filters & Scripts";
	public static final String VERSION = "1.0.3";
	public static final char SECTION = '\u00A7';

	@Instance("Scripting")
	public static ScriptingMod instance;

	@SidedProxy(clientSide = "scripting.forge.ClientProxy", serverSide = "scripting.forge.Proxy")
	public static Proxy proxy;

	public static SelectorItem selector;

	
	private Map<String, Selection> selections;
	private ServerTickHandler serverTicker;

	private Map<String, Object> serverProps;
	private Map<String, Object> clientProps;
	private Map<String, Class<?>> abbreviations;
	

	public ScriptingMod() {
		selections = new HashMap<String, Selection>();
		abbreviations = new HashMap<String, Class<?>>();
		serverProps = new HashMap<String, Object>();
		clientProps = new HashMap<String, Object>();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.load(new Configuration(event.getSuggestedConfigurationFile()));

		selector = new SelectorItem(Config.selectorID);
		LanguageRegistry.instance().addNameForObject(selector, "en_US", "Selector Wand");
		GameRegistry.registerItem(selector, selector.getUnlocalizedName());

		ModMetadata modMeta = event.getModMetadata();
		modMeta.authorList = Arrays.asList(new String[] { "Davidee" });
		modMeta.autogenerated = false;
		modMeta.credits = "Thanks to Mojang, Forge, and all your support.";
		modMeta.description = "Adds Javascript support to Minecraft via Mozilla Rhino."
				+ " Users can write their own scripts to run in-game on either the client or server"
				+ " and also use filters to directly modify their world." 
				+ "\nFor more information, visit the URL above.";
		modMeta.url = "http://www.minecraftforum.net/topic/1951378-/";
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		/*
		 * Initialize our special context factory
		 */
		ContextFactory.initGlobal(new SandboxContextFactory());
		ReflectionHelper.init();
		DefaultFilters.init(new File(new File(proxy.getMinecraftDir(), "scripts"), "server"));
		
		addAbbreviation("Vec3", ScriptVec3.class);
		addAbbreviation("Vec2", ScriptVec2.class);
		addAbbreviation("ItemStack", ScriptItemStack.class);
		addAbbreviation("Rand", ScriptRandom.class);
		addAbbreviation("Array", ScriptArray.class);
		addAbbreviation("IO", ScriptIO.class);
		addAbbreviation("Script", ScriptHelper.class);
		
		addAbbreviation("Block", ScriptBlock.class);
		addAbbreviation("BlockID", ScriptBlockID.class);
		addAbbreviation("ItemID", ScriptItemID.class);
		
		addAbbreviation("Setting", Setting.class);
		addAbbreviation("SettingBoolean", SettingBoolean.class);
		addAbbreviation("SettingInt", SettingInt.class);
		addAbbreviation("SettingFloat", SettingFloat.class);
		addAbbreviation("SettingString", SettingString.class);
		addAbbreviation("SettingList", SettingList.class);
		addAbbreviation("SettingBlock", SettingBlock.class);
		addAbbreviation("SettingItem", SettingItem.class);
		
		addAbbreviation("DataWatcher", ScriptDataWatcher.class);
		addAbbreviation("Entity", ScriptEntity.class);
		
		addAbbreviation("TileEntity", ScriptTileEntity.class);

		addAbbreviation("TAG_Byte", TAG_Byte.class);
		addAbbreviation("TAG_Byte_Array", TAG_Byte_Array.class);
		addAbbreviation("TAG_Compound", TAG_Compound.class);
		addAbbreviation("TAG_Double", TAG_Double.class);
		addAbbreviation("TAG_Float", TAG_Float.class);
		addAbbreviation("TAG_Int", TAG_Int.class);
		addAbbreviation("TAG_Int_Array", TAG_Int_Array.class);
		addAbbreviation("TAG_List", TAG_List.class);
		addAbbreviation("TAG_Long", TAG_Long.class);
		addAbbreviation("TAG_Short", TAG_Short.class);
		addAbbreviation("TAG_String", TAG_String.class);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		/*
		 * Pretend it's the clientProxy.
		 * The ClientProxy will register the ClientTickHandler;
		 * the regular Proxy will do nothing.
		 * 
		 * ClientProxy.postInit will only be called from the Minecraft (client) main thread
		 */
		File scriptDir = new File(proxy.getMinecraftDir(), "scripts");
		proxy.postInit(scriptDir, clientProps, abbreviations);
		TickRegistry.registerScheduledTickHandler( (serverTicker = new ServerTickHandler(scriptDir, serverProps, abbreviations)) , Side.SERVER);
	}
	
	@EventHandler
	public void serverStarting (FMLServerStartingEvent event) {
		ServerCommandManager manager = (ServerCommandManager) event.getServer().getCommandManager();
		manager.registerCommand(new FilterCommand());
	}
	
	public ServerCore getServerCore() {
		return serverTicker.getServerCore();
	}

	public void scriptSleep(int ticks) throws ContinuationPending, IllegalAccessException, IllegalArgumentException {
		if (isClient()) 
			proxy.getClientCore().scriptSleep(ticks);
		else
			serverTicker.getServerCore().scriptSleep(ticks);
	}

	public void addAbbreviation(String abrev, Class<?> clazz) {
		abbreviations.put(abrev, clazz);
	}

	public void setClientProperty(String name, Object obj) {
		ScriptCore core = proxy.getClientCore();
		if (core == null)
			clientProps.put(name, obj);
		else
			core.setProperty(name, obj);
	}

	public void setServerProperty(String name, Object obj) {
		ScriptCore core = (serverTicker == null) ? null : serverTicker.getServerCore();
		if (core == null)
			serverProps.put(name, obj);
		else
			core.setProperty(name, obj);
	}

	public Selection getSelection(EntityPlayerMP player) {
		Selection s = selections.get(player.username);
		if (s == null) {
			s = new Selection(player.dimension);
			selections.put(player.username, s);
		}
		if (s.getDimension() != player.dimension) {
			s.reset(player.dimension);
			player.playerNetServerHandler.sendPacketToPlayer(ScriptPacket.getPacket(PacketType.SELECTION, s));
		}
		return s;
	}

	public void updateSelections(List<EntityPlayerMP> allPlayers) {
		for (EntityPlayerMP player : allPlayers) {
			Selection s = selections.get(player.username);
			if (s != null && (s.getDimension() != player.dimension || s.isInvalid())) {
				s.reset(player.dimension);
				player.playerNetServerHandler.sendPacketToPlayer(ScriptPacket.getPacket(PacketType.SELECTION, s));
			}
		}
	}
	
	public void clearSelections() {
		selections.clear();
	}

	public boolean isClient() {
		return FMLCommonHandler.instance().getEffectiveSide().isClient();
	}

}
