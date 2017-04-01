package edu.utc.bkf926.WorldStream;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WSServerPlugin extends JavaPlugin{

	static boolean debug, serverEnabled, exportOnTimer, exportChunkOnBlockUpdate;
	static int serverPort, exportInterval;
	static boolean exportPartials, exportDeco, exportEntities, exportCoveredBlocks;
	static ExportScope exportScope;
	
	static enum ExportScope{
		CHUNK, LOADED, WORLD
	};
	
	public static final String VERSION = "0.3.27";
	
	@Override
	/**
	 * This method runs when the plugin is enabled (effectively on server start-up.)
	 * Think of onEnable() as the "main" method of the Bukkit/Spigot plugin.
	 */
	public void onEnable() {
		loadConfigValues();												//Load the config.yml settings
		this.saveDefaultConfig(); 										//Creates the initial config file - DOES NOT overwrite if it already exists
		Bukkit.getLogger().info("WorldStream "+VERSION+" enabled!");
		if (serverEnabled) try {
			WSHTTPEndpoint.startServer();								//Start the HTTP Server
			Bukkit.getLogger().info("WorldStream HTTP Server started successfully on port ");
		} catch (IOException e){
			Bukkit.getLogger().severe("Failed to start HTTP Server!");
			Bukkit.getLogger().severe(e.getStackTrace().toString());
		}
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
				sender.sendMessage("WorldStream "+VERSION);
				sender.sendMessage("Use /ws export to export the map data!");
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
		FileConfiguration cfg = this.getConfig();
		debug = cfg.getBoolean("debug-mode");
		serverEnabled = cfg.getBoolean("enable-http-server");
		serverPort = cfg.getInt("http-server-port");
		
		exportPartials = cfg.getBoolean("export-partial-blocks");
		exportDeco = cfg.getBoolean("export-decoration-blocks");
		exportEntities = cfg.getBoolean("export-entities");
		exportCoveredBlocks = cfg.getBoolean("export-covered-blocks");
		
		exportInterval = cfg.getInt("auto-export-interval");
		if (exportInterval==0){
			exportOnTimer=false;
			exportChunkOnBlockUpdate=true;
		} else if (exportInterval==-1){
			exportOnTimer=false;
			exportChunkOnBlockUpdate=false;
		} else {
			exportOnTimer=true;
			exportChunkOnBlockUpdate=false;
		}
	}
	
	/*
	 * --------BEGIN EVENT HANDLERS--------
	 */
	
	//TODO EventHandler: Write loaded chunks when player joins / switches world. This also can create the new JSON Writer for that world.
	
	//TODO EventHandler: Write block on block change event
}
