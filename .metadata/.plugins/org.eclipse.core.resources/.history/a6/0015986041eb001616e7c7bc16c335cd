package edu.utc.bkf926.WorldStream;

import java.util.LinkedList;

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

}
