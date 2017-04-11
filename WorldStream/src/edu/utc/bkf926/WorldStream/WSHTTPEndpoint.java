package edu.utc.bkf926.WorldStream;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class WSHTTPEndpoint {
	
	static HttpServer server;
	static String error;

	@SuppressWarnings("restriction")
	public static void startServer() throws IOException{
		int port = WSServerPlugin.config.getInt("http-server-port");
		error = null;
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);

			server.createContext("/api/v1/get/", new V1GetHandler());
			server.createContext("/api/v1/info", new V1InfoHandler());
			
			server.setExecutor(null);
			server.start();
		}
		catch (Exception e){
			error = e.getClass().toString() + ":" + e.getMessage() + ":" + e.getStackTrace().toString();
			throw e;
		}
	}
	
	static class V1GetHandler implements HttpHandler{
		public void handle(HttpExchange exchange) throws IOException{
			OutputStream out = exchange.getResponseBody();
			
			String[] queryValues = exchange.getRequestURI().getQuery().split("&");
			HashMap<String, String> query = new HashMap<String, String>();
			try {
				for (String s : queryValues){
					query.put(
							s.split("=")[0],
							s.split("=")[1]
					);
				}
				
				String resp = null;
				
				if (!query.containsKey("world")) throw new Exception("world");
				World world = Bukkit.getServer().getWorld(query.get("world"));
				if (world==null) throw new Exception("world404");
				
				if (query.size()==1){
					if (WSServerPlugin.config.getBoolean("allow-batch-downloads")==false){
						throw new Exception("forbidden");
					}
					WSServerPlugin.announceBatchDownload(false, world.getLoadedChunks().length);
					resp = WSJson.getWorldJSON(world);
				}
				else if (query.containsKey("x") && query.containsKey("z"))
				{
					int x = Integer.parseInt(query.get("x"));
					int z = Integer.parseInt(query.get("z"));
					Chunk chunk = world.getChunkAt(x, z);
					if (chunk==null) throw new Exception("chunk404");
					resp = WSJson.getChunkJSON(chunk);
				}
				else if (query.containsKey("x1") && query.containsKey("x2")
						&& query.containsKey("z1") && query.containsKey("z2"))
				{
				
					if (WSServerPlugin.config.getBoolean("allow-batch-downloads")==false){
						throw new Exception("forbidden");
					}
					
					int x1 = Integer.parseInt(query.get("x1"));
					int z1 = Integer.parseInt(query.get("z1"));
					int x2 = Integer.parseInt(query.get("x2"));
					int z2 = Integer.parseInt(query.get("z2"));

					if (x1>x2 || z1>z2) throw new Exception("format");
					
					int count = ((x2-x1)+1)*((z2-z1)+1);
					WSServerPlugin.announceBatchDownload(true, count);
					
					resp = "";
					
					for (int x=x1; x<=x2; x++){
						for (int z=z1; z<=z2; z++){
							resp += WSJson.getChunkJSON(world.getChunkAt(x, z));
							resp += ",\n";
						}
					}
					
				}
				else {
					throw new Exception("format");
				}
				
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, resp.length());
				out.write(resp.getBytes());
				out.close();
			}
			catch (Exception e){
				int err = 0; String msg = "";
				if (e instanceof NumberFormatException || e.getMessage().equals("format")){
					err = 400;
					msg = "400 - WorldStream cannot understand your query. Check the format of your request URI against the HTTP API on our GitHub wiki.";
				}
				else if (e.getMessage().equals("world")){
					err = 400;
					msg = "400 - You must specify a world name in your query. If your server is single-world, try using 'world', 'world_nether', or 'world_the-end'.";
				}
				else if (e.getMessage().equals("world404")){
					err = 404;
					msg = "404 - World not found.";
				}
				else if (e.getMessage().equals("chunk404")){
					err = 404;
					msg = "404 - Chunk not found. Check your coordinates and make sure the chunk is loaded.";
				}
				else if (e.getMessage().equals("forbidden")){
					err = 403;
					msg = "403 - Forbidden: This server does not allow batch downloads. You must download one chunk at a time.";
				}
				else {
					err = 500;
					msg = "500 - Internal server error. Please report this to the developers so we can fix it! ";
					msg += e.getMessage() + "/";
					for (StackTraceElement ste : e.getStackTrace()){
						msg += ste.toString() + "/";
					}
				}
				exchange.sendResponseHeaders(err, msg.length());
				out.write(msg.getBytes());
				out.close();
			}
		}
	}
	
	static class V1InfoHandler implements HttpHandler{
		public void handle(HttpExchange exchange) throws IOException{
			String message = "Success! This server is running WorldStream version "+WSServerPlugin.VERSION;
			exchange.sendResponseHeaders(200, message.length());
			exchange.getResponseBody().write(message.getBytes());
			exchange.getResponseBody().close();
		}
	}
	
}
