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
	int wx, wy, wz;
	
	
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
			if (bi.cy == b.cy && bi.cz == b.cz){
				if (bi.cx == b.cx+1) xe=true;
				if (bi.cx == b.cx-1) xw=true;
			}
			if (bi.cx == b.cx && bi.cz == b.cz){
				if (bi.cy == b.cy+1) ya=true;
				if (bi.cy == b.cy-1) yb=true;
			}
			if (bi.cx == b.cx && bi.cy == b.cy){
				if (bi.cz == b.cz+1) zs=true;
				if (bi.cz == b.cz-1) zn=true;
			}
		}
		if (!inchunk) return false; //If the block is not in this chunk. Avoid letting this happen.
		if (!(xe&&xw&&ya&&yb&&zn&&zs)){
			return true;
		}
		else return false;
	}

}
