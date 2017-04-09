package edu.utc.bkf926.WorldStream;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class WSServerPlugin extends JavaPlugin implements Listener{

	static FileConfiguration config;
	
	static enum ExportScope{
		CHUNK, LOADED, WORLD
	};
	
	public static String VERSION = "0.4.51";
	
	public static boolean cullingEnabled;
	
	@Override
	/**
	 * This method runs when the plugin is enabled (effectively on server start-up.)
	 * Think of onEnable() as the "main" method of the Bukkit/Spigot plugin.
	 */
	public void onEnable() {
		loadConfigValues();												//Load the config.yml settings
		this.saveDefaultConfig(); 										//Creates the initial config file - DOES NOT overwrite if it already exists
		Bukkit.getLogger().info("WorldStream "+VERSION+" enabled!");
		debug("WorldStream is running in verbose/debug mode! Expect a lot of console spam.");
		if (config.getBoolean("http-server-enabled")) try {
			WSHTTPEndpoint.startServer();								//Start the HTTP Server
			Bukkit.getLogger().info("[WorldStream] HTTP Endpoint up and running on localhost:"+config.getInt("http-server-port"));
		} catch (IOException e){
			Bukkit.getLogger().severe("[WorldStream] HTTP Endpoint failed to start: see stacktrace");
			Bukkit.getLogger().severe(e.getStackTrace().toString());
		}
		
		if (config.getBoolean("websockets-enabled")) try {
			WSStreamingServer.startServer();
			Bukkit.getLogger().info("[WorldStream] WebSocket Stream up and running on localhost:"+config.getInt("websockets-port"));
		}
		catch (Exception e1){
			Bukkit.getLogger().severe("[WorldStream] WebSocket Endpoint failed to start: see stacktrace");
			Bukkit.getLogger().severe(e1.getStackTrace().toString());
		}
		cullingEnabled = config.getBoolean("cull-covered-blocks");
		getServer().getPluginManager().registerEvents(this, this);		//Register this as an event listener
	}
	
	@Override
	public void onDisable() {
		Bukkit.getLogger().info("[WorldStream] Shutting down - closing all open connections...");
		WSStreamingServer.getInstance().closeAll();
	}
	
	@Override
	/**
	 * This method handles all commands sent by players.
	 * @param sender    The entity (player or console) that issued the command
	 * @param command   The Command event object
	 * @param label
	 * @param args      Contains all the space-delimited arguments as a String array.
	 * @return          true if the command runs successfully, false otherwise.
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("ws")){
			Player p = null;
			String worldName = "";
			
			//Get the Player that sent the command, or send an error if the sender isn't a player
			try {
				p = (Player)sender;
				worldName = p.getWorld().getName();
			} catch (ClassCastException e){
				sender.sendMessage(ChatColor.RED + "WorldStream cannot be run from the console.");
				return true;
			}
			
			//Send helpful message if there are no args
			if (args.length==0){
				sender.sendMessage(ChatColor.YELLOW + "This server is running WorldStream version "+VERSION);
				sender.sendMessage("Usage: /ws [export | info | config]");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("info")){
				sender.sendMessage(ChatColor.GREEN + "WorldStream "+VERSION);
				if (config.getBoolean("http-server-enabled")){
					sender.sendMessage(ChatColor.GREEN + "HTTP Server: "+ChatColor.DARK_GREEN+"Enabled"+ChatColor.GREEN+" on port "+config.getInt("http-server-port"));
					// TODO Show server status error if the server couldn't start
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "HTTP Server: "+ChatColor.DARK_GRAY+"Disabled");
				}
				if (config.getBoolean("websockets-enabled")){
					sender.sendMessage(ChatColor.GREEN + "Streaming Server: "+ChatColor.DARK_GREEN+"Enabled"+ChatColor.GREEN+" on port "+config.getInt("websockets-port"));
					// TODO Show server status error if the server couldn't start
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "Streaming Server: "+ChatColor.DARK_GRAY+"Disabled");
				}
			}
			else if (args[0].equalsIgnoreCase("export")){
				
				if (args[1].equalsIgnoreCase("chunk")){
					
				}
				else if (args[1].equalsIgnoreCase("loaded")){
					
				}
				else if (args[1].equalsIgnoreCase("world")){
					
				}
				else {
					sender.sendMessage("Usage: /ws export [chunk | loaded | world]");
					return true;
				}
				
			}
			else if (args[0].equalsIgnoreCase("config")){
				//TODO change some config settings via the game chat
			}
			else {
				sender.sendMessage("Usage: /ws [export | info | config]");
				return true;
			}
		}
		
		return false; //base case
	}
	
	/**
	 * Returns the current chunk that a player is in.
	 * @param p
	 * @return
	 */
	public Chunk getSendersCurrentChunk(Player p){
		return p.getWorld().getChunkAt(p.getLocation());
	}
	
	/**
	 * Loads the config.yml file and sets the boolean values accordingly.
	 */
	public void loadConfigValues(){
		config = this.getConfig();
	}
	
	public static void announceStream(String name, World world, boolean join){
		if (world==null) return;
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.getWorld().equals(world)){
				if (join){
					p.sendMessage(ChatColor.GREEN + name + " started streaming this world!");
				} else {
					p.sendMessage(ChatColor.GOLD + name + " stopped streaming this world.");
				}
			}
		}
	}
	
	public static void debug(String message){
		if (config.getBoolean("verbose-mode")){
			Bukkit.getLogger().info("[WorldStream][DEBUG] "+message);
		}
	}
	
	/*
	 * --------BEGIN EVENT HANDLERS--------
	 * 
	 * 	We use the highest event priority because we want to see the outcome of the event.
	 *	If it was cancelled by a lower priority plugin, then don't broadcast anything.
	 *	Otherwise autocancelling (like permissions preventing a block place) would spam messages.
	 */
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent evt){
		if (!evt.isCancelled()){
			debug("Block place event fired!");
			WSStreamingServer.getInstance().broadcastBlockChange(evt.getBlockPlaced(), true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent evt){
		if (!evt.isCancelled()){
			debug("Block break event fired!");
			WSStreamingServer.getInstance().broadcastBlockChange(evt.getBlock(), false);
		}
	}
	
	//TODO Do we need to handle any more events?
}
