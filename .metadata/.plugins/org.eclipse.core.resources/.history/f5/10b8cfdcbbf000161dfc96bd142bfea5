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
		//Will need to edit filepath for each individual computer this runs on
		stream = new FileOutputStream("C:/Users/austi/Documents/UTC/Capstone/Test/test.txt");
		
	}
	
	public void writeBlock(WSBlock block) throws IOException{
		//TODO Write a single block
		//Creates a string to match the JSON format that Unity is expecting, then writes it to the file
		String blockText = "{ \"blocks\" : [ { \"type\": " + block.getBlockID() + 
				", \"position\": { \"x\"" + block.x + ", \"y\"" + block.y + ", \"z\"" + block.z
				+ "}, ] }"
		stream.write(blockText);
		
	}
	
	public void writeChunk(WSChunk chunk) throws IOException{
		//TODO Write a chunk (16x16x256)
		while (chunk.hasNextBlock())
		{
			writeBlock(chunk.nextBlock());
		}
	}
	
	public void close(){
		//TODO Close stream
		stream.close();
	}
	
	/**
	 * Returns the world associated with this JSON Writer.
	 * @return
	 */
	public World getWorld(){
		return Bukkit.getServer().getWorld(world);
	}
	
}
