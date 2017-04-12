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
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class WSServerPlugin extends JavaPlugin implements Listener{

	static FileConfiguration config;
	public static String VERSION = "0.5.58";
	public static boolean cullingEnabled;
	
	@Override
	/**
	 * This method runs when the plugin is enabled (effectively on server start-up.)
	 * Think of onEnable() as the "main" method of the Bukkit/Spigot plugin.
	 */
	public void onEnable() {
		
		//Load the config.yml and save an initial config file - if one already exists, this will not overwrite in.
		loadConfigValues();
		this.saveDefaultConfig();
		
		Bukkit.getLogger().info("WorldStream "+VERSION+" enabled!");
		debug("WorldStream is running in verbose/debug mode! Expect a lot of console spam.");
		
		//Start the HTTP endpoint and handle any errors
		if (config.getBoolean("http-server-enabled")) try {
			WSHTTPEndpoint.startServer();
			Bukkit.getLogger().info("[WorldStream] HTTP Endpoint up and running on localhost:"+config.getInt("http-server-port"));
		} catch (IOException e){
			Bukkit.getLogger().severe("[WorldStream] HTTP Endpoint failed to start: see stacktrace");
			logException(e, true);
		}
		
		//Start the WebSockets server and handle any errors
		if (config.getBoolean("websockets-enabled")) try {
			WSStreamingServer.startServer();
			Bukkit.getLogger().info("[WorldStream] WebSocket Stream up and running on localhost:"+config.getInt("websockets-port"));
		}
		catch (Exception e1){
			Bukkit.getLogger().severe("[WorldStream] WebSocket Endpoint failed to start: see stacktrace");
			logException(e1, true);
		}
		
		//Register events with Bukkit, and set the culling mode
		cullingEnabled = config.getBoolean("cull-covered-blocks");
		getServer().getPluginManager().registerEvents(this, this);
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
		if (command.getName().equalsIgnoreCase("worldstream")){
			
			//Send helpful message if there are no args
			if (args.length==0){
				sender.sendMessage(ChatColor.YELLOW + "This server is running WorldStream version "+VERSION);
				sender.sendMessage("Usage: /worldstream [ info | clients]");
				return true;
			}
			
			if (args[0].equalsIgnoreCase("info")){
				sender.sendMessage(ChatColor.GREEN + "This server is running WorldStream v"+VERSION);
				
				if (config.getBoolean("http-server-enabled")){
					if (WSHTTPEndpoint.error == null){
						sender.sendMessage(ChatColor.GREEN + "HTTP Server: "+ChatColor.DARK_GREEN+"Started"+ChatColor.GREEN+" on port "+config.getInt("http-server-port"));
					} else {
						sender.sendMessage(ChatColor.GREEN + "HTTP Server: "+ChatColor.DARK_RED+"Stopped"+ChatColor.GREEN+" - see the console for details of the error.");
						Bukkit.getLogger().severe(WSHTTPEndpoint.error);
					}
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "HTTP Server: "+ChatColor.DARK_GRAY+"Stopped - Disabled by configuration file");
				}
				
				if (config.getBoolean("websockets-enabled")){
					if (WSStreamingServer.getInstance().getError()==null){
						sender.sendMessage(ChatColor.GREEN + "Streaming Server: "+ChatColor.DARK_GREEN+"Started"+ChatColor.GREEN+" on port "+config.getInt("websockets-port"));
						sender.sendMessage(ChatColor.GREEN + "Connected clients: "+ChatColor.DARK_GREEN+WSStreamingServer.getInstance().getSessionCount());
					}
					else {
						sender.sendMessage(ChatColor.GREEN + "Streaming Server: "+ChatColor.DARK_RED+"Stopped"+ChatColor.GREEN+" - see the console for details of the error.");
						Bukkit.getLogger().severe(WSStreamingServer.getInstance().getError());
					}
				}
				else {
					sender.sendMessage(ChatColor.GREEN + "Streaming Server: "+ChatColor.DARK_GRAY+"Stopped - Disabled by configuration file");
				}
				return true;
			}
			
			if (args[0].equalsIgnoreCase("clients")){
				
				int count = WSStreamingServer.getInstance().getSessionCount();
				
				sender.sendMessage(ChatColor.GREEN + "Connected clients: "+ChatColor.DARK_GREEN+count);
				for (WSStreamingServer.WSStreamingSession session : WSStreamingServer.getInstance().getSessions()){
					String user = session.getName();
					String world = session.getWorld().getName();
					if (session.getChunk()==null){
						sender.sendMessage(ChatColor.GREEN + "User: " + ChatColor.DARK_GREEN + user
								+ ChatColor.GREEN + ", World: " + ChatColor.DARK_GREEN + world
						);
					}
					else {
						int x = session.getChunk().getX();
						int z = session.getChunk().getZ();
						
						sender.sendMessage(ChatColor.GREEN + "User: " + ChatColor.DARK_GREEN + user
								+ ChatColor.GREEN + ", World: " + ChatColor.DARK_GREEN + world
								+ ChatColor.GREEN + ", Chunk: " + ChatColor.DARK_GREEN + "(" + x + ", " + z + ")"
						);
					}
				}
				return true;
			}
			
		}
		
		return false; //base case
	}
	
	/**
	 * Loads the config.yml file and sets the boolean values accordingly.
	 */
	public void loadConfigValues(){
		config = this.getConfig();
	}
	
	/**
	 * Announces to all players in a world when a remote client opens or closes a stream.
	 * @param name  Name of the remote client.
	 * @param world Name of the world the client is streaming.
	 * @param join  True if the stream is starting, false if it is stopping.
	 */
	public static void announceStream(String name, World world, boolean join){
		if (world==null) return;
		for (Player p : Bukkit.getOnlinePlayers()){
			if (p.getWorld().equals(world)){
				if (join){
					p.sendMessage(ChatColor.DARK_AQUA+"WorldStream: " + ChatColor.GREEN + name + " started streaming this world!");
				} else {
					p.sendMessage(ChatColor.DARK_AQUA+"WorldStream: " + ChatColor.YELLOW + name + " stopped streaming this world.");
				}
			}
		}
	}
	
	public static void announceBatchDownload(boolean range, int count){
		if (config.getBoolean("announce-batch-downloads")==false) return;
		if (!range){
			Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA+"WorldStream: " + ChatColor.GOLD + "Lag Incoming: A client is batch exporting an entire world ("+count+" chunks).");
		}
		else {
			Bukkit.getServer().broadcastMessage(ChatColor.DARK_AQUA+"WorldStream: " + ChatColor.GOLD + "Lag Incoming: A client is batch exporting "+count+" chunks.");
		}
	}
	
	public static void debug(String message){
		if (config.getBoolean("verbose-mode")){
			Bukkit.getLogger().info("[WorldStream][DEBUG] "+message);
		}
	}
	
	/**
	 * Prints an exception to the Bukkit console.
	 * @param e       The exception.
	 * @param severe  True if the exception is plugin-breaking. If false, it will only be logged while in debug mode.
	 */
	public static void logException(Exception e, boolean severe){
		if (severe || config.getBoolean("verbose-mode")){
			Bukkit.getLogger().severe(e.getClass().toString()+" : "+e.getMessage());
			for (StackTraceElement s : e.getStackTrace()){
				Bukkit.getLogger().severe(s.toString());
			}
		}
	}
	
	/*
	 * --------BEGIN EVENT HANDLERS--------
	 * 
	 * 	We use the highest event priority because we want to see the outcome of the event.
	 *	If it was cancelled by a lower priority plugin, then don't broadcast anything.
	 *	Otherwise autocancelling (like permissions preventing a block place) would spam two messages.
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
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityPlace(HangingPlaceEvent evt){
		if (!evt.isCancelled()){
			debug("Hanging entity place event fired!");
			WSStreamingServer.getInstance().broadcastEntityChange(evt.getEntity(), true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityBreak(HangingBreakEvent evt){
		if (!evt.isCancelled()){
			debug("Hanging entity break event fired!");
			WSStreamingServer.getInstance().broadcastEntityChange(evt.getEntity(), false);
		}
	}
	
	//TODO Do we need to handle any more events?
}
