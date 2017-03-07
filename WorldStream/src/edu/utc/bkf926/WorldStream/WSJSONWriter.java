package edu.utc.bkf926.WorldStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class WSJSONWriter {
	
	private String world;
	private FileOutputStream stream;
	
	public WSJSONWriter(String worldName){
		world=worldName;
			Bukkit.getLogger().info("[DEBUG] Created JSON writer for "+world);
		
	}
	
	public void writeBlock(Block block) throws IOException{
		//Write a single block
		//Creates a string to match the JSON format that Unity is expecting, then writes it to the file
		
		List<MetadataValue> facing = block.getMetadata("facing");
		
		if (block.getType().toString().equals("AIR")) return; //Filters out empty (air) blocks. TODO Add this to the block-culling code when fully implemented.
		String blockText = "{ \n\"blocks\" : [ \n{ \n\"type\": \"" + block.getType().toString() + 
				"\",\n \"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + 
				"\"},\n },\n ]\n }\n";
		stream.write(blockText.getBytes());
	}
	
	public void writeChunk(Chunk chunk) throws IOException{
		//Write a chunk (16x16x256)
		//This is O(horrible) so remember only to call this method when someone requests it
		
		stream = new FileOutputStream("plugins/WorldStream/"+world+"_"+chunk.getX()+"_"+chunk.getZ()+".json");
		Block block;
		String header = "{ \n\"blocks\" : [ \n";
		String footer = "]\n }\n";
		stream.write(header.getBytes());
		for (int i=0; i<16; i++){
			for (int j=0; j<16; j++){
				for (int k=0; k<256; k++){
					block = chunk.getBlock(i, k, j);
					//this.writeBlock(chunk.getBlock(i, k, j));
					//copied code from above to split the header and footer for the .json file
					if (block.getType().toString().equals("AIR")) return; //Filters out empty (air) blocks. TODO Add this to the block-culling code when fully implemented.
					String blockText = "{ \n\"type\": \"" + block.getType().toString() + 
							"\",\n \"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + 
							"\"},\n },\n";
					stream.write(blockText.getBytes());
					//Does the same thing as the 'writeBlock()' method above, but without the header and footer ([]brackets), to make a cleaner .json file for the chunk
				}
			}
		}
		stream.write(footer.getBytes());
		
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
