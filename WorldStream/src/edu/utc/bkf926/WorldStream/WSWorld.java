package edu.utc.bkf926.WorldStream;

import java.util.HashSet;

public class WSWorld {

	private HashSet<WSChunk> chunks;
	private String name;
	
	/**
	 * Returns true if the block has at least one exposed face. Use this method for culling invisible blocks.
	 * @param b A WSBlock within this chunk to check.
	 * @return
	 */
	public boolean blockIsVisible(WSBlock b){
		return false; //TODO New visibility method
	}
	
}
