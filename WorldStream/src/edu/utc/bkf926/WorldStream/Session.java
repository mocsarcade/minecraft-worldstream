package edu.utc.bkf926.WorldStream;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.java_websocket.WebSocket;

public class Session {
	
	private static int nextSessionId = 0;

	private String user;
	private WebSocket connection;
	private World world;
	private List<Chunk> watchlist;
	
	public Session(WebSocket socket){
		connection = socket;
		user = "Streamer"+(nextSessionId++);
		watchlist = new ArrayList<Chunk>();
	}
	
	public void send(String message){
		connection.send(message);
	}
	
	public void setUsername(String username){
		user = username;
	}
	
	public void changeWorld(World world){
		this.world = world;
		watchlist.clear();
	}
	
	public void addChunk(Chunk chunk){
		watchlist.add(chunk);
	}
	
	public void removeChunk(Chunk chunk){
		watchlist.remove(chunk);
	}
	
	public String getUsername(){
		return user;
	}
	
	public World getWorld(){
		return world;
	}
	
	public WebSocket getConnectionSocket(){
		return connection;
	}
	
	public boolean isWatching(Chunk chunk){
		return watchlist.contains(chunk);
	}
	
	public void close(){
		connection.closeConnection(0, "Connection closed by server.");
	}
}
