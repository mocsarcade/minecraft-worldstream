package edu.utc.bkf926.WorldStream;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WSServerPlugin extends JavaPlugin{
	
	public static final String VERSION = "PRE-PROTOTYPE 0.0.3";
	
	public static final int[] SOLID_SURFACE_IDS = {
			1,2,3,4,5,7,12,13	//This covers all the most basic surfaces. Add the others after initial testing
	};
	public static final int[] NONSOLID_STRUCTURES = {
			6,
	};
	
	static boolean serverEnabled = false;
	// TODO add this as a config option later, as well as the port number of the server.
	
	@Override
	public void onEnable() {
		Bukkit.getLogger().info("WorldStream v"+VERSION+" enabled!");
		if (serverEnabled) try {
			WSHTTPEndpoint.startServer();
			Bukkit.getLogger().info("HTTP Server started successfully on port 80");
		} catch (IOException e){
			Bukkit.getLogger().severe("Failed to start HTTP Server!");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player pSender = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("ws")){
			if (args[0].equalsIgnoreCase("version")){
				sender.sendMessage("WorldStream v"+VERSION);
				sender.sendMessage("Use /ws export to export the map data!");
			}
			else if (args[0].equalsIgnoreCase("export")){
				if (args[1].equalsIgnoreCase("chunk")){
					try {
						exportCurrentChunk((Player)sender);
						sender.sendMessage(ChatColor.GREEN+"Chunk exported successfully!");
						Bukkit.getLogger().info("Chunk exported by "+sender.getName());
					} catch (IOException e1){
						sender.sendMessage(ChatColor.RED+"Export failed - Could not write the file data. Check your folder permissions.");
					} catch (ClassCastException e2){
						sender.sendMessage(ChatColor.RED+"You cannot export a chunk via the console."
								+ " Please use /ws export loaded instead.");
					}
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
	
	public Chunk getSendersCurrentChunk(Player p){
		return p.getWorld().getChunkAt(p.getLocation());
	}
	
	public WSChunk pullChunkData(Chunk c){
		
	}
	
	public void exportCurrentChunk(Player p) throws IOException{
		WSChunk curr = pullChunkData(getSendersCurrentChunk(p));
		WSJSONWriter.writeChunk(curr);
	}
	
}
