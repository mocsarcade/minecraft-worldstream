package edu.utc.bkf926.WorldStream;

import java.util.LinkedList;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

/**
 * This class represents a chunk.
 * In the game, this is a 16x16x16 area of blocks.
 * In WorldStream, we are converting the visible blocks into a "surface" since the solid blocks don't need to be rendered
 * in the game engine and doing so will cause massive slowdowns.
 * 
 * We can do this by looking at the relationships between blocks in the chunk and culling out those that have another
 * solid block adjacent on all six sides.
 */
public class WSChunk {
	
	/*
	 * Contains ALL blocks in the chunk, in no particular order.
	 * Use the cx, cy, and cz values of the blocks to set their position.
	 * The JSON writer will be responsible for culling non-visible blocks.
	 */
	private LinkedList<WSBlock> blocks;
	
	/**
	 * Constructs a new WSChunk from a Spigot Chunk.
	 * TODO this is horribly inefficient. We need to re-do this at some point to either use smaller sub-chunks or somehow check for changes instead of exporting the entire thing.
	 */
	@SuppressWarnings("deprecation")
	/*
	 * We are using numeric type ID's.
	 * We know this method is deprecated. It may be more effective to use the new textual IDs.
	 */
	public WSChunk(Chunk c){
		bi=-1;
		blocks = new LinkedList<WSBlock>();
		for (int i=0; i<16; i++){
			for (int j=0; j<128; j++){
				for (int k=0; k<16; k++){
					Block b = c.getBlock(i, j, k);
					int[] coords = {b.getX(), b.getY(), b.getZ()};
					blocks.add(new WSBlock(coords, b.getType().toString()));
				}
			}
		}
	}
	
	/**
	 * Returns true if the block has at least one exposed face. Use this method for culling invisible blocks.
	 * @param b A WSBlock within this chunk to check.
	 * @return
	 */
	public boolean blockIsVisible(WSBlock b){
		boolean inchunk = false;
		boolean xe=false, xw=false, ya=false, yb=false, zn=false, zs=false;
		for (WSBlock bi : blocks){
			if (bi==b) inchunk=true;
			if (bi.y == b.y && bi.z == b.z){
				if (bi.x == b.x+1) xe=true;
				if (bi.x == b.x-1) xw=true;
			}
			if (bi.x == b.x && bi.z == b.z){
				if (bi.y == b.y+1) ya=true;
				if (bi.y == b.y-1) yb=true;
			}
			if (bi.x == b.x && bi.y == b.y){
				if (bi.z == b.z+1) zs=true;
				if (bi.z == b.z-1) zn=true;
			}
		}
		if (!inchunk) return false; //If the block is not in this chunk. Avoid letting this happen.
		if (!(xe&&xw&&ya&&yb&&zn&&zs)){
			return true;
		}
		else return false;
	}
	
	private int bi;
	
	public WSBlock nextBlock(){
		if (bi>=blocks.size()-1){
			return null;
		}
		else return blocks.get(bi++);
	}
	
	public boolean hasNextBlock(){
		if (bi>=blocks.size()-1){
			return false;
		}
		return true;
	}
	
	public void resetIterator(){
		bi=-1;
	}
}
