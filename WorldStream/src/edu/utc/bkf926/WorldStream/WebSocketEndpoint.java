package edu.utc.bkf926.WorldStream;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSocketEndpoint extends WebSocketServer{
	
	private static WebSocketEndpoint instance;
	public static WebSocketEndpoint getInstance(){
		return instance;
	}
	
	public static void startServer(){
		int port = WorldStream.config.getInt("websockets-port");
		try {
			instance = new WebSocketEndpoint(new InetSocketAddress(port));
			instance.start();
		}
		catch (Exception e){
			instance.error = e.getClass().toString() + ":" + e.getMessage() + ":" + e.getStackTrace().toString();
			throw e;
		}
	}

	public WebSocketEndpoint(InetSocketAddress address){
		super(address);
		sessions = new ArrayList<Session>();
	}
	
	private List<Session> sessions;
	private String error;
	
	public int getSessionCount(){
		return sessions.size();
	}
	
	public List<Session> getSessions(){
		return sessions;
	}
	
	public String getError(){
		return error;
	}

	@Override
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
		Bukkit.getLogger().info("[WorldStream] WebSocket client disconnected: "+arg0.getRemoteSocketAddress().toString());
		Session session = getSession(arg0);
		WorldStream.announceStream(session.getUsername(), session.getWorld(), false);
		sessions.remove(session);
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket arg0, String arg1) {
		try {
			Session session = getSession(arg0);
			String[] cmdTokens = arg1.split(" ");
			String cmd = cmdTokens[0];
			
			if (cmd.equalsIgnoreCase("world")){
				if (Bukkit.getServer().getWorld(cmdTokens[1]) == null){
					session.send("> ERROR: World not found.");
				} else {
					if (session.getWorld()!=null) WorldStream.announceStream(session.getUsername(), session.getWorld(), false); //Send a "stopped streaming" chat to the old world
					session.changeWorld(Bukkit.getServer().getWorld(cmdTokens[1]));
					session.send("> OK: World updated to "+session.getWorld().getName());
					WorldStream.announceStream(session.getUsername(), session.getWorld(), true); //Send a "started streaming" chat to the new world
				}
			}
			
			else if (cmd.equalsIgnoreCase("user")){
				session.setUsername(cmdTokens[1]);
				session.send("> OK: Username updated to "+session.getUsername());
			}
			
			else if (cmd.equalsIgnoreCase("reset")){
				session.reset();
				session.send("> OK: Watchlist cleared.");
			}
			
			else if (cmd.equalsIgnoreCase("watchworld")){
				session.setWatchFullWorld(true);
				session.send("> OK: You are now watching this entire world.");
			}
			
			else if (cmd.equalsIgnoreCase("watch")){
				int cx = Integer.parseInt(cmdTokens[1]);
				int cz = Integer.parseInt(cmdTokens[2]);
				Chunk chunk = session.getWorld().getChunkAt(cx, cz);
				
				if (chunk==null){
					session.send("> ERROR: Couldn't load that chunk. Has it been generated/loaded recently?");
				} else {
					session.addChunk(chunk);
					session.send("> OK: Chunk added.");
				}
			}
			
			else if (cmd.equalsIgnoreCase("unwatch")){
				int cx = Integer.parseInt(cmdTokens[1]);
				int cz = Integer.parseInt(cmdTokens[2]);
				Chunk chunk = session.getWorld().getChunkAt(cx, cz);
				
				if (chunk==null){
					session.send("> ERROR: Couldn't load that chunk. Has it been generated/loaded recently?");
				} else {
					session.removeChunk(chunk);
					session.send("> OK: Chunk removed.");
				}
			}
			
			else if (cmd.equalsIgnoreCase("get")){
				int cx = Integer.parseInt(cmdTokens[1]);
				int cz = Integer.parseInt(cmdTokens[2]);
				Chunk chunk = session.getWorld().getChunkAt(cx, cz);
				
				if (chunk==null){
					session.send("> ERROR: Couldn't load that chunk. Has it been generated/loaded recently?");
				} else {
					session.send(JSONFactory.getChunkJSON(chunk));
				}
			}
			
			else if (cmd.equalsIgnoreCase("status")){
				session.send("> SESSION STATUS");
				session.send("> Username: "+session.getUsername());
				if (session.getWorld()==null){
					session.send("> World: Not set! Use \"world [name]\" to set a world name to stream.");
				} else {
					session.send("> World: "+session.getWorld().getName());
				}
				if (session.isWatchingWorld()){
					session.send("> Watchlist: Entire world");
				}
				else {
					session.send("> Watchlist: "+session.getChunkWatchlist().size()+" chunks:");
					for (Chunk chunk : session.getChunkWatchlist()){
						session.send("> cx="+chunk.getX()+", cz="+chunk.getZ());
					}
				}
			}
			
			else {
				session.send("> ERROR: Unknown command.");
			}
			
		} catch (Exception e){
			arg0.send("> ERROR: There was an unknown error processing your request. Check the formatting of your command.");
			WorldStream.logException(e, false);
		}
	}

	@Override
	public void onOpen(WebSocket arg0, ClientHandshake arg1) {
		Bukkit.getLogger().info("[WorldStream] WebSocket client connected from "+arg0.getRemoteSocketAddress().toString());
		arg0.send("> WorldStream Version "+WorldStream.VERSION+": Stream opened successfully.");
		arg0.send("> Please set a world and chunk(s) to stream. You will receive no updates until you add chunks to your watchlist.");
		Session session = new Session(arg0);
		sessions.add(session);
	}
	
	private Session getSession(WebSocket socket){
		for (Session session : sessions){
			if (session.getConnectionSocket()==socket) return session;
		}
		return null;
	}
	
	public void broadcastBlockChange(Block block, boolean place){
		for (Session session : sessions){
			if (session.getWorld().equals(block.getWorld())){
				if (session.isWatching(block.getChunk())){
					String json = JSONFactory.getEventJSON(block, place);
					session.send(json);
					if (!place){
						checkNeighborsForDeculling(block, session);
					}
				}
			}
		}
	}
	
	public void broadcastEntityChange(Entity entity, boolean place){
		for (Session session : sessions){
			if (session.getWorld().equals(entity.getWorld())){
					String json = JSONFactory.getEntityEventJSON(entity, place);
					session.send(json);
					// TODO Entity chunk-checking.
			}
		}
	}
	
	public void checkNeighborsForDeculling(Block block, Session session){
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.NORTH, 1), BlockFace.SOUTH)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.NORTH, 1), true));
		}
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.SOUTH, 1), BlockFace.NORTH)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.SOUTH, 1), true));
		}
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.EAST, 1), BlockFace.WEST)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.EAST, 1), true));
		}
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.WEST, 1), BlockFace.EAST)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.WEST, 1), true));
		}
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.UP, 1), BlockFace.DOWN)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.UP, 1), true));
		}
		if (JSONFactory.shouldBlockBeDeculled(block.getRelative(BlockFace.DOWN, 1), BlockFace.UP)){
			session.send(JSONFactory.getEventJSON(block.getRelative(BlockFace.DOWN, 1), true));
		}
	}
	
	public void closeAll(){
		for (Session session : sessions){
			session.close();
		}
	}
	
}
