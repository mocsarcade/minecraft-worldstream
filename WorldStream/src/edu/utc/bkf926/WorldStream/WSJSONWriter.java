package edu.utc.bkf926.WorldStream;

import java.io.FileOutputStream;
import java.io.IOException;

import org.bukkit.World;
import org.bukkit.Bukkit;

public class WSJSONWriter {
	
	private String world;
	private FileOutputStream stream;
	
	public WSJSONWriter(String worldName){
		//TODO Constructor stub - be sure to initialize stream
		//FileWriter(file) writer = new FileWriter(worldName +".json");
		//Use the file writer to create a text file ending in .json
		
		//Derek said he would commit changes to his branch, and you should merge them
		//into yours before going too much further, as it changes the WSBlock class
	}
	
	public void writeBlock(WSBlock block) throws IOException{
		//TODO Write a single block
	}
	
	public void writeChunk(WSChunk chunk) throws IOException{
		//TODO Write a chunk (16x16x256)
	}
	
	public void close(){
		//TODO Close stream
	}
	
	/**
	 * Returns the world associated with this JSON Writer.
	 * @return
	 */
	public World getWorld(){
		return Bukkit.getServer().getWorld(world);
	}
	
}
