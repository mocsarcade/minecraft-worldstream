package edu.utc.bkf926.WorldStream;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class WSJson {
	
	public static String getWorldJSON(World world){
		String worldHeader = "{\n\"name\": \""+world+"\",\n"+
								"\"chunks\": [\n";
		String worldFooter = "]\n}\n";
		StringBuilder worldBuilder = new StringBuilder(worldHeader);
		for (Chunk chunk : world.getLoadedChunks()){
			worldBuilder.append(getChunkJSON(chunk));
		}
		worldBuilder.append(worldFooter);
		return worldBuilder.toString();
	}
	
	public static String getChunkJSON(Chunk chunk){
		String chunkHeader = "{ \n\"position\": {\"x\":"+chunk.getX()+", \"z\":"+chunk.getZ()+"},\n"
				+ "\"blocks\" : [ \n";
		String chunkMid = "\n], \n\"entities\": [ \n";
		String chunkFooter = "\n]\n }\n";
		StringBuilder chunkBuilder = new StringBuilder(chunkHeader);
		for (int i=0; i<16; i++){
			for (int j=0; j<16; j++){
				for (int k=0; k<256; k++){
					chunkBuilder.append(getBlockJSON(chunk.getBlock(i, k, j)));
				}
			}
		}
		chunkBuilder.append(chunkMid);
		chunkBuilder.append(getEntitiesJSON(chunk));
		chunkBuilder.append(chunkFooter);
		return chunkBuilder.toString();
	}

	public static String getBlockJSON(Block block){
		
		if (block.getType().toString().equals("AIR")) return "";
		
		// TODO if the block is covered, return null or empty string (culling)
		
		String blockText = "{ \n\"type\": \"" + block.getType().toString() + 
				"\",\n \"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + "\"}"
				+"\n"+WSMetadata.getBlockMetadata(block)
				+"\n}";
		return blockText;
	}
	
	public static String getEntitiesJSON(Chunk chunk){
		//TODO Stub method
		return "";
	}
	
}
