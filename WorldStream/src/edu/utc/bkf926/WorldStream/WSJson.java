package edu.utc.bkf926.WorldStream;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class WSJson {
	
	public static final int[] UNSUPPORTED = {
			0, 90, 119, 137, 176, 177, 209, 210, 211
	};
	
	//Represents block IDs that need a subtype
	public static final int[] VARIANT = {
			1, 3, 5, 6, 12, 17, 18, 19, 24, 31, 35, 38, 43, 44, 95, 97, 98, 125, 126, 139, 155,
			159, 160, 161, 162, 168, 171, 175, 179
	};
	
	//Represents block IDs that are solid, i.e. they will affect culling
	public static final int[] TRANSPARENT = {
			0, 6, 8, 9, 10, 11, 18, 20, 26, 27, 28, 30, 31, 32, 34, 37, 38, 39, 40, 44, 50, 51,
			52, 53, 55, 59, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 75, 76, 77, 79, 81, 83, 85,
			92, 93, 94, 95, 96, 101, 102, 104, 105, 106, 107, 108, 109, 111, 113, 114, 115, 116,
			117, 118, 119, 120, 122, 126, 127, 128, 130, 131, 132, 134, 135, 136, 138, 139, 140,
			141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 154, 156, 157, 160, 161, 163,
			164, 165, 166, 167, 171, 175, 176, 177, 178, 180, 182, 183, 184, 185, 186, 187, 188,
			189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 203, 205, 207
	};
	
	
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
		String chunkMid = "\n] \n\"entities\": [ \n";
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
		
		if (shouldBlockBeCulled(block)) return "";
		
		String blockText = "{ \n\"type\": \"" + getTypeString(block) + 
				"\",\n \"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + "\"},"
				+"\n"+getBlockMetadata(block)
				+"\n},\n";
		return blockText;
	}
	
	public static String getTypeString(Block block){
		for (int i : VARIANT){
			if (i==block.getTypeId()){
				WSServerPlugin.debug("Variant hit");
				return block.getTypeId()+"_"+block.getData();
			}
		}
		return block.getTypeId()+"";
	}
	
	public static String getEventJSON(Block block, boolean placed){
		
		StringBuilder event = new StringBuilder("{\n");
		if (placed){
			event.append("\"event\": \"PLACE\",\n");
			event.append(
					"\"type\": \"" + getTypeString(block) + 
					"\",\n\"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + "\"},"
					+"\n"+getBlockMetadata(block)
					);
		} else {
			event.append("\"event\": \"BREAK\",\n");
			event.append(
					"\"position\": { \"x\":\"" + block.getX() + "\", \"y\":\"" + block.getY() + "\", \"z\":\"" + block.getZ() + "\"},"
					+"\n"
					);
		}
		event.append("}");
		return event.toString();
		
	}
	
	public static boolean shouldBlockBeCulled(Block block){
		
		for (int i : UNSUPPORTED){
			if (i==block.getTypeId()) return true;
		}
		
		if (!WSServerPlugin.cullingEnabled) return false;
		
		/*
		 * We check each face of the block for a solid neighbor.
		 * If the neighbor is in the TRANSPARENT list, we can return false. We know the block will not be culled.
		 * TRANSPARENT includes air (0).
		 * If we get to the end, and none of the loops have returned a transparent neighbor block,
		 * we know there is a solid block on all six sides, and therefore the block is culled.
		 */
		
		try {
			//Top face
			Block testBlock = block.getRelative(BlockFace.UP, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			//Bottom face
			testBlock = block.getRelative(BlockFace.DOWN, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			//East face
			testBlock = block.getRelative(BlockFace.EAST, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			//West face
			testBlock = block.getRelative(BlockFace.WEST, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			//South face
			testBlock = block.getRelative(BlockFace.SOUTH, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			//North face
			testBlock = block.getRelative(BlockFace.NORTH, 1);
			for (int i : TRANSPARENT){
				if (testBlock.getTypeId()==i) return false;
			}
			
			return true;
		}
		catch (Exception e){
			WSServerPlugin.debug(e.getClass().toString()+" in culling: x="+block.getX()+" y="+block.getY()+" z="+block.getZ()+". Defaulting to false.");
			return false;
		}
		
	}
	
	public static String getBlockMetadata(Block block){
		return "";
	}
	
	public static String getEntitiesJSON(Chunk chunk){
		StringBuilder entityBuilder = new StringBuilder();
		for (Entity e : chunk.getEntities()){
			entityBuilder.append(getEntityJSON(e));
		}
		return entityBuilder.toString();
	}
	
	public static String getEntityJSON(Entity entity){
		return "";
	}
	
}
