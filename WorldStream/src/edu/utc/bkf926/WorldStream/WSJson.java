package edu.utc.bkf926.WorldStream;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.material.Bed;
import org.bukkit.material.Crops;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.material.Torch;
import org.bukkit.material.Tree;
import org.bukkit.material.WoodenStep;

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
					try {
						chunkBuilder.append(getBlockJSON(chunk.getBlock(i, k, j)));
					}
					catch (IllegalStateException e){
						// Do nothing, leave this block out.
						// We don't know why getTypeId() throws the "Asynchronous Entity Track" IllegalStateException, and it's not documented anywhere.
						// So our only choice is to ignore a block that causes this error.
					}
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
				"\",\n\"position\": { \"x\":" + block.getX() + ", \"y\":" + block.getY() + ", \"z\":" + block.getZ() + "},"
				+"\n"+getBlockMetadata(block)
				+"\n},\n";
		return blockText;
	}
	
	public static String getTypeString(Block block) throws IllegalStateException{
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
					"\",\n\"position\": { \"x\":" + block.getX() + ", \"y\":" + block.getY() + ", \"z\":" + block.getZ() + "},"
					+"\n"+getBlockMetadata(block)
					);
		} else {
			event.append("\"event\": \"BREAK\",\n");
			event.append(
					"\"position\": { \"x\":" + block.getX() + ", \"y\":" + block.getY() + ", \"z\":" + block.getZ() + "},"
					+"\n"
					);
		}
		event.append("}");
		return event.toString();
		
	}
	
	public static boolean shouldBlockBeCulled(Block block){
		
		try {
		
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
		
		StringBuilder metadata = new StringBuilder();
		
		BlockState state = block.getState();
		Material material = block.getType();
		MaterialData data = block.getState().getData();
		
		//Directional blocks
		//Far as I know, the only things using angles other than 0/90/180/270 are signs and banners.
		if (data instanceof Directional){
			int h=0, v=0;
			switch (((Directional)data).getFacing()){
			case SOUTH: break;
			case DOWN: v=270; break;
			case EAST: h=90; break;
			case EAST_NORTH_EAST: h=112; break;
			case EAST_SOUTH_EAST: h=68; break;
			case NORTH: h=180; break;
			case NORTH_EAST: h=135; break;
			case NORTH_NORTH_EAST: h=158; break;
			case NORTH_NORTH_WEST: h=202; break;
			case NORTH_WEST: h=225; break;
			case SELF: break;
			case SOUTH_EAST: h=45; break;
			case SOUTH_SOUTH_EAST: h=22; break;
			case SOUTH_SOUTH_WEST: h=338; break;
			case SOUTH_WEST: h=315; break;
			case UP: v=90; break;
			case WEST: h=270; break;
			case WEST_NORTH_WEST: h=248; break;
			case WEST_SOUTH_WEST: h=292; break;
			default:break;
			}
			
			metadata.append("\"rotation\": {\"h\":"+h+", \"v\":"+v+"},\n");
		}
		
		//Wooden logs, because they don't implement Directional because of course they don't
		//I'm a little salty, can you tell?
		if (data instanceof Tree){
			Tree tree = (Tree)data;
			int h=0, v=0;
			switch(tree.getDirection()){
			case UP: case DOWN: break;
			case NORTH: case SOUTH: v=90; break;
			case EAST: case WEST: h=90; v=90; break;
			default: break;
			}
			
			metadata.append("\"rotation\": {\"h\":"+h+", \"v\":"+v+"},\n");
		}
		
		//Slabs
		if (data instanceof Step){
			Step theStep = (Step)data;
			
			int stepRotation = theStep.isInverted() ? 180 : 0;
			metadata.append("\"rotation\": {\"h\":"+0+", \"v\":"+stepRotation+"},\n");
		}
		//Because wooden slabs are special and need a different (identical) class.
		if (data instanceof WoodenStep){
			WoodenStep theStep = (WoodenStep)data;
			
			int stepRotation = theStep.isInverted() ? 180 : 0;
			metadata.append("\"rotation\": {\"h\":"+0+", \"v\":"+stepRotation+"},\n");
		}
		
		//Doors
		if (data instanceof Door){
			Door theDoor = (Door)data;
			
			String half = theDoor.isTopHalf() ? "top" : "bottom";
			metadata.append("\"half\": \""+half+"\",\n");
			
			String hinge = theDoor.getHinge() ? "right" : "left"; //TODO Is this correct, or backwards?
			metadata.append("\"hinge\": \""+hinge+"\",\n");
			
			if (!theDoor.isTopHalf()){ //"open" is undefined for the top half. Spigot API notes this.
				metadata.append("\"open\": \""+theDoor.isOpen()+"\",\n");
			}
			
		}
		
		//Beds
		if (data instanceof Bed){
			Bed theBed = (Bed)data;
			
			String half = theBed.isHeadOfBed() ? "top" : "bottom";
			metadata.append("\"half\": \""+half+"\",\n");
			
		}
		
		//Signs
		if (state instanceof Sign){
			Sign sign = (Sign)state;
			
			metadata.append("\"text\": [");
			for (int i=0; i<4; i++)
				metadata.append("\""+sign.getLine(i)+"\", ");
			
			metadata.append("],\n");
		}
		
		//Torches and redstone torches
		if (data instanceof Torch){
			boolean standing = ((Torch)data).getAttachedFace().equals(BlockFace.DOWN);
			metadata.append("\"standing\": \""+standing+"\",\n");
		}
		
		//Crops
		if (data instanceof Crops){
			int growth=0;
			switch(((Crops)data).getState()){
			case GERMINATED:
				growth=1; break;
			case MEDIUM:
				growth=4; break;
			case RIPE:
				if (block.getTypeId()==59) growth=7; else growth=3; break;
			case SEEDED:
				growth=0; break;
			case SMALL:
				if (block.getTypeId()==59) growth=3; else growth=1; break;
			case TALL:
				if (block.getTypeId()==59) growth=5; else growth=2; break;
			case VERY_SMALL:
				growth=2; break;
			case VERY_TALL:
				growth=6; break;
			}
			
			metadata.append("\"growth\": \""+growth+"\",\n");
			
		}
		//Giant mushroom textures?
		
		return metadata.toString();
	}
	
	public static String getEntitiesJSON(Chunk chunk){
		StringBuilder entityBuilder = new StringBuilder();
		for (Entity e : chunk.getEntities()){
			entityBuilder.append(getEntityJSON(e));
		}
		return entityBuilder.toString();
	}
	
	public static String getEntityJSON(Entity entity){
		
		StringBuilder info = new StringBuilder();
		Location location = entity.getLocation();
		
		if (entity instanceof Painting){
			Painting painting = (Painting)entity;
			info.append("{\n");
			info.append("\"id\": \""+entity.getEntityId()+"\",\n");
			info.append("\"type\": \"PAINTING\",\n");
			info.append("\"position\": {\"x\":"+location.getX()+", \"y\":"+location.getY()+", \"z\":"+location.getZ()+"},\n");
			info.append("\"rotation\": {\"h\":"+location.getYaw()+", \"v\":"+location.getPitch()+"},\n");
			info.append("\"art\": \""+painting.getArt().toString()+"\",\n");
			info.append("},\n");
		}
		
		if (entity instanceof ItemFrame){
			ItemFrame frame = (ItemFrame)entity;
			info.append("{\n");
			info.append("\"id\": \""+entity.getEntityId()+"\",\n");
			info.append("\"type\": \"ITEMFRAME\",\n");
			info.append("\"position\": {\"x\":"+location.getX()+", \"y\":"+location.getY()+", \"z\":"+location.getZ()+"},\n");
			info.append("\"rotation\": {\"h\":"+location.getYaw()+", \"v\":"+location.getPitch()+"},\n");
			info.append("\"itemtype\": \""+frame.getItem().getTypeId()+"\",\n");
			info.append("},\n");
		}
		
		return info.toString();
	}
	
	public static String getEntityEventJSON(Entity entity, boolean placed){
		StringBuilder event = new StringBuilder("{\n");
		
		if (placed){
			event.append("\"event\": \"ENTITY_PLACE\",\n");
			event.append("\"entity\": \n");
			event.append(getEntityJSON(entity));
		}
		else {
			event.append("\"event\": \"ENTITY_BREAK\",\n");
			event.append("\"id\": \""+entity.getEntityId()+"\",\n");
		}
		event.append("}\n");
		return event.toString();
	}
	
}
