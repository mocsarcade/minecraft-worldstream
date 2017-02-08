package edu.utc.bkf926.WorldStream;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WSServerPlugin extends JavaPlugin{

	static boolean debug, serverEnabled, exportOnTimer, exportChunkOnBlockUpdate;
	static int serverPort, exportInterval;
	
	static boolean exportPartials, exportDeco, exportEntities, exportCoveredBlocks;
	
	static enum ExportScope{
		CHUNK, LOADED, WORLD
	};
	
	static ExportScope exportScope;
	
	public static final String VERSION = "DEV_PrePrototype_0.0.5";
	
	public static final int[] SOLID_SURFACE_IDS = {
			1,2,3,4,5,7,12,13	//This covers all the most basic surfaces. Add the others after initial testing
	};
	public static final int[] NONSOLID_STRUCTURES = {
			6,
	};
	
	@Override
	/**
	 * This method runs when the plugin is enabled (effectively on server start-up.)
	 * Think of onEnable() as the "main" method of the Bukkit/Spigot plugin.
	 */
	public void onEnable() {
		loadConfigValues();
		this.saveDefaultConfig(); //Creates the initial config file - DOES NOT overwrite if it already exists
		Bukkit.getLogger().info("WorldStream v"+VERSION+" enabled!");
		if (serverEnabled) try {
			WSHTTPEndpoint.startServer();
			Bukkit.getLogger().info("HTTP Server started successfully on port ");
		} catch (IOException e){
			Bukkit.getLogger().severe("Failed to start HTTP Server!");
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
		
		Player pSender = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("ws")){
			if (args[0].equalsIgnoreCase("version")){
				sender.sendMessage("WorldStream v"+VERSION);
				sender.sendMessage("Use /ws export to export the map data!");
			}
			else if (args[0].equalsIgnoreCase("export")){
				if (args[1].equalsIgnoreCase("chunk")){
					/*try {
						exportCurrentChunk((Player)sender);
						sender.sendMessage(ChatColor.GREEN+"Chunk exported successfully!");
						Bukkit.getLogger().info("Chunk exported by "+sender.getName());
					} catch (IOException e1){
						sender.sendMessage(ChatColor.RED+"Export failed - Could not write the file data. Check your folder permissions.");
					} catch (ClassCastException e2){
						sender.sendMessage(ChatColor.RED+"You cannot export a chunk via the console."
								+ " Please use /ws export loaded instead.");
					}*/
				}
				else if (args[1].equalsIgnoreCase("loaded")){
					//export all loaded chunks
				}
				else if (args[1].equalsIgnoreCase("world")){
					//export entire map
				}
				else {
					sender.sendMessage(ChatColor.RED + "Usage: /ws export [chunk/loaded/world]");
				}
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
	
	public void exportChunk(Player p) throws IOException{
		WSChunk exc = new WSChunk(getSendersCurrentChunk(p));
		WSJSONWriter.writeChunk(exc);
	}
}
