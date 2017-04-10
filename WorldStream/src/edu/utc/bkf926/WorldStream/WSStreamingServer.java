package edu.utc.bkf926.WorldStream;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WSStreamingServer extends WebSocketServer{
	
	private static WSStreamingServer instance;
	public static WSStreamingServer getInstance(){
		return instance;
	}
	
	public static void startServer(){
		int port = WSServerPlugin.config.getInt("websockets-port");
		try {
			instance = new WSStreamingServer(new InetSocketAddress(port));
			instance.start();
		}
		catch (Exception e){
			instance.error = e.getClass().toString() + ":" + e.getMessage() + ":" + e.getStackTrace().toString();
			throw e;
		}
	}

	public WSStreamingServer(InetSocketAddress address){
		super(address);
		sessions = new ArrayList<WSStreamingSession>();
	}
	
	private List<WSStreamingSession> sessions;
	private String error;
	
	public int getSessionCount(){
		return sessions.size();
	}
	
	public List<WSStreamingSession> getSessions(){
		return sessions;
	}
	
	public String getError(){
		return error;
	}

	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
		Bukkit.getLogger().info("[WorldStream] WebSocket client disconnected: "+arg0.getRemoteSocketAddress().toString());
		WSStreamingSession session = getSession(arg0);
		WSServerPlugin.announceStream(session.getName(), session.getWorld(), false);
		sessions.remove(session);
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket arg0, String arg1) {
		String[] parameters = arg1.split(",");
		HashMap<String, String> msgValues = new HashMap<String, String>();
		WSStreamingSession session = getSession(arg0);
		WSServerPlugin.announceStream(session.getName(), session.getWorld(), false);
		
		try {
			for (String s : parameters){
				msgValues.put(s.split("=")[0], s.split("=")[1]);
			}
			
			if (msgValues.containsKey("user")){
				session.setName(msgValues.get("user"));
			}
			if (msgValues.containsKey("world")){
				session.setWorld(Bukkit.getServer().getWorld(msgValues.get("world")));
				
			}
			if (msgValues.containsKey("x") && msgValues.containsKey("z")){
				int x = Integer.parseInt(msgValues.get("x"));
				int z = Integer.parseInt(msgValues.get("z"));
				session.setChunk(session.getWorld().getChunkAt(x, z));
			}
			
			WSServerPlugin.announceStream(session.getName(), session.getWorld(), true);
		}
		catch (NumberFormatException | IndexOutOfBoundsException e){
			arg0.send("ERROR: WorldStream cannot parse your request. Please check the WebSockets API and make sure your request is formatted correctly.");
		}
		catch (NullPointerException e){
			Bukkit.getLogger().warning("[WorldStream] Encountered Null Session!");
		}
	}

	@Override
	public void onOpen(WebSocket arg0, ClientHandshake arg1) {
		Bukkit.getLogger().info("[WorldStream] WebSocket client connected from "+arg0.getRemoteSocketAddress().toString());
		arg0.send("WorldStream Version "+WSServerPlugin.VERSION+": Stream opened successfully.");
		arg0.send("Please specify world and chunk(s) to stream; you will not receive any updates until you do so!");
		WSStreamingSession session = new WSStreamingSession(arg0);
		sessions.add(session);
	}
	
	private WSStreamingSession getSession(WebSocket socket){
		for (WSStreamingSession session : sessions){
			if (session.getConnection()==socket) return session;
		}
		return null;
	}
	
	public static class WSStreamingSession{
		private WebSocket connection;
		private String name;
		private World world;
		private Chunk chunk;
		private WSStreamingSession(WebSocket socket){
			name = "Anonymous user";
			connection = socket;
		}
		private WebSocket getConnection() {
			return connection;
		}
		public String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
		public World getWorld() {
			return world;
		}
		private void setWorld(World world) {
			this.world = world;
		}
		public Chunk getChunk() {
			return chunk;
		}
		private void setChunk(Chunk chunk) {
			this.chunk = chunk;
		}
		
	}
	
	public void broadcastBlockChange(Block block, boolean place){
		for (WSStreamingSession session : sessions){
			if (block.getWorld().equals(session.getWorld())){
				if (session.getChunk()==null || block.getChunk().equals(session.getChunk())){
					String blockJson = WSJson.getEventJSON(block, place);
					WSServerPlugin.debug("Sending update to user "+session.getName()+": "+blockJson);
					//session.getConnection().send("testing");
					session.getConnection().send(blockJson);
				}
			}
		}
	}
	
	public void broadcastEntityChange(Entity entity, boolean place){
		for (WSStreamingSession session : sessions){
			if (entity.getWorld().equals(session.getWorld())){
					String Json = WSJson.getEntityEventJSON(entity, place);
					WSServerPlugin.debug("Sending update to user "+session.getName()+": "+Json);
					//session.getConnection().send("testing");
					session.getConnection().send(Json);
			}
		}
	}
	
	public void closeAll(){
		for (WSStreamingSession session : sessions){
			session.getConnection().closeConnection(0, "Server is shutting down.");
		}
	}
	
}
