package edu.utc.bkf926.WorldStream;

import java.util.HashMap;

public class WSBlock {

	int cx, cy, cz;
	//CX CY and CZ are the positions of the block within the chunk, 0-15
	
	private String blockType;
	private boolean isSolid; //True if the block will make up a part of the solid surface
	private HashMap<String, Integer> metadata;
	
	/*
	 * List of known metadata elements
	 * facing - north, south, east, west
	 * adjacent - north, south, east, west
	 * attachment - north, south, east, west, down
	 * orientation - ns, ew, ud
	 * inverted - true/false
	 * open - true/false
	 * powered - true/false
	 * hinge - left/right
	 * growth - 0-7
	 * item - [a number for the item inside, use for item frames or flower pots]
	 * painting - [a number for which painting to use]
	 */
	
	public void addMetadata(String name, Integer value){
		metadata.put(name, value);
	}
	
	
}
