package me.Morlok8k.BukkitLandGenerator;

import org.bukkit.*;
import java.lang.*;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


import org.bukkit.ChatColor;
//import net.minecraft.server.Chunk;		//when we want this, we will specifically code for it
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


//import org.bukkit.craftbukkit.CraftWorld;     //importing based on craftbukkit
import net.minecraft.*;
import net.minecraft.server.*;


/** Bukkit Land Generator 
 * By Morlok8k
 * 
 * A very simple yet effective plugin for bukkit.  
 * 
 * Generates Chunks if they don't exist in in a customizable size square based on the centerpoint of the map [0,0]
 * 
 * Suitable for mapping, or to avoid on-demand chunk generation via players exploring.
 * 
 * Use Minecraft Land Generator (not a plugin) for more features!
 * 
 */

/*  Notes:
 *  X represents North/South, with positive X being South.
 *  Z represents East/West, with Positive Z being West.
 *  Y represents Height, with 64 being sea level.
 */





@SuppressWarnings("unused")
public class BukkitLandGenerator extends JavaPlugin implements Runnable {

	private final BLGWorldListener worldListener = new BLGWorldListener(this);
	//private final BLGEntityListener entityListener = new BLGEntityListener(this);
	//private final BLGBlockListener blockListener = new BLGBlockListener(this);
	
	
	
	//reminder:
	//Static is "Global"
	//Final is "Constant"
	
	// ThreadVars (child thread is read only)
	public static Player threadPlayer;
	public static int threadRadius;
	public static World threadWorld;
	public static Location threadLocation;
	// End ThreadVars
	
	
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Server server;
	public static final String logPrefix = "[BukkitLandGenerator] ";
	public static String chatPrefix = ChatColor.GREEN + logPrefix + ChatColor.WHITE;
	String version;
	
	public static final boolean debug = true;    // debug output
	
	public boolean pluginEnabled = false;  //true means plugin is enabled
	
	

	public void onEnable(){ 
		//log.info("Bukkit Land Generator is enabled.");
		pluginEnabled = true;
		
		server = getServer();
		
		PluginManager pm = server.getPluginManager();
		PluginDescriptionFile pdf = this.getDescription();
		version = pdf.getVersion();
		
		if (debug) {
			version = (pdf.getVersion() + " (Debug Version!)"); 
		}
		
		pm.registerEvent(Event.Type.CHUNK_LOAD, worldListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.CHUNK_POPULATED, worldListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Event.Priority.High, this);

		log.info(logPrefix + "Bukkit Land Generator v" + version + " enabled!");
	} 
	
	
	
	public void onDisable(){ 
		//log.info("Bukkit Land Generator is disabled.");
		pluginEnabled = false;
		log.info(logPrefix + "Bukkit Land Generator v" + version + " is disabled!");
	}	
	
	
	public synchronized void run() {
		
		// this is where the real generating happens, in a separate thread!
		
		Player player = threadPlayer;		// I can't send a new thread arguments,
		int radius = threadRadius;			// so I'm using global vars
		World world = threadWorld;			// I only read them once to local vars
		Location loc = threadLocation;		// so the thread can run independently
		threadPlayer = null;				// and to make sure i don't cause any 
		threadRadius = 0;					// problems elsewhere
		threadWorld = null;					//
		threadLocation = null;				// I know... its not memory efficient
		
		log.info(logPrefix + " BLG Generation Thread: " + Thread.currentThread().getName());
                
        
        if (debug) {
			log.info(logPrefix + " [DEBUG-Generate] " + " player: " + player.getDisplayName() + " radius: " + radius + " World: " + world.getName() + " Location [X,Y,Z]: [" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "]");
		}
		
		// we need to generate now...
		
		int x = 0;
		int z = 0;
		
		/*
		Location originalSpawn = world.getSpawnLocation();
		int orgX = Location.locToBlock(originalSpawn.getX());
		int orgY = Location.locToBlock(originalSpawn.getY());
		int orgZ = Location.locToBlock(originalSpawn.getZ());
		*/
		
		Chunk chunk = world.getChunkAt(loc);
		x = chunk.getX();
		z = chunk.getZ();	
		
		//x = Location.locToBlock(loc.getX());
		//z = Location.locToBlock(loc.getZ());		//for block based locations
		
		int increment = 1; 
		int xRange = radius, yRange = radius;
		int xOffset = x, yOffset = z;
		
		// MLG copy/pasta/modify		
		
		int totalIterations = (xRange / increment + 1) * (yRange / increment + 1);
		int currentIteration = 0;
		
		long differenceTime = System.currentTimeMillis();
		Long[] timeTracking = new Long[]{differenceTime, differenceTime, differenceTime, differenceTime};
		
		
		boolean count;
		int countUnload = 0;
		
		// Start Loop!
		for (int currentX = 0 - xRange / 2; currentX <= xRange / 2; currentX += increment) {
			
			for (int currentY = 0 - yRange / 2; currentY <= yRange / 2; currentY += increment) {
				currentIteration++;
				
				countUnload = 0;
				
				//here i want to check the status of the queued chunks
				Chunk[] cache = world.getLoadedChunks();
				
				log.info(logPrefix + "cache.length:" + cache.length);
				
				
				log.info(logPrefix + "Chunk Location: {" + Integer.toString(currentX + xOffset) + "," + Integer.toString(currentY + yOffset) + "}   (" + currentIteration + "/" + totalIterations + ") " + Float.toString((Float.parseFloat(Integer.toString(currentIteration)) / Float.parseFloat(Integer.toString(totalIterations))) * 100) + "% Done" );			// Time Remaining estimate
				
				x = currentX + xOffset;
				z = currentY + yOffset;		//MLG uses a traditional "Y" axis, while minecraft (and bukkit) use non-standard Z
				
				/*
				world.setSpawnLocation(x, 128, z);
				server.createWorld(world.getName(), world.getEnvironment());
				*/
				
				//server.unloadWorld(world, true);
				
				
				world.loadChunk(x, z, true);
				//world.regenerateChunk(x, z);
				
				/*
				try {
					Thread.sleep(10);  //Sleep the thread for 10 milliseconds, to help allow the server to catch up.
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				*/				
				
				
				/*
				int unloadLoop = 0;
				
				// here I unload all loaded chunks, to make room for new chunks
				
				for (unloadLoop = 0; unloadLoop < cache.length; unloadLoop++) {
					count = world.unloadChunk(cache[unloadLoop].getX(), cache[unloadLoop].getZ(), true, true);
					if (count == true) {
						countUnload++;
						count = false;
					}
				
				}
				*/
				
				//log.info(logPrefix + "Cache: Unloaded " + countUnload + " of " + unloadLoop + " chunks.");
				
				//world.refreshChunk(x, z);
				
				
				
				
				timeTracking[0] = timeTracking[1];
				timeTracking[1] = timeTracking[2];
				timeTracking[2] = timeTracking[3];
				timeTracking[3] = System.currentTimeMillis();
				if (currentIteration >= 4) {
					differenceTime = (timeTracking[3] - timeTracking[0]) / 3; // well, this is what it boils down to
					differenceTime *= 1 + (totalIterations - currentIteration);
					log.info(String.format("Estimated time remaining: %dh%dm%ds",
							differenceTime / (1000 * 60 * 60), (differenceTime % (1000 * 60 * 60)) / (1000 * 60), ((differenceTime % (1000 * 60 * 60)) % (1000 * 60)) / 1000));
				}
				
			}
			
			
			log.info(logPrefix + "Sleeping for 20 Seconds...");
			try {
				Thread.sleep(20000); // 1000 = do nothing for 1000 milliseconds (1 second)
			} catch(InterruptedException e){
				e.printStackTrace();
			}
			
			
		}
		//originalSpawn
		//world.setSpawnLocation(orgX, orgY, orgZ);
		
		log.info(logPrefix + "Finished Queuing chunks to be generated.");
//end of MLG copy
        
		try {
			Thread.sleep(60000); // 1000 = do nothing for 1000 milliseconds (1 second)
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		
		server.dispatchCommand((CommandSender) player, "save-all");
        
        
    }
	
	
	public synchronized boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		
		
		log.info(logPrefix + " [DEBUG-onCommand] " + " Sender: " + sender.toString());
		log.info(logPrefix + " BLG onCommand Thread: " + Thread.currentThread().toString());
		
		boolean isConsole = false;
		
		if(sender.toString().contains("org.bukkit.craftbukkit.command")) {
			//check to see if the command is from the console.
			
			//isConsole = true;
				
			log.info(logPrefix + "Bukkit Land Generator needs to be run In-Game! (And only by an OP!)");
			return true;
		}
		
		//Location test;
		//test.add(0, 64, 0)
		//test.setWorld(world)
		
		
		Player player = (Player) sender;
		if (debug) {
			player.sendMessage(chatPrefix + " [DEBUG-onCommand] " + " Command: " + cmd.getName() + " label: " + label + " Args: " + Integer.parseInt(args[0]));
			log.info(logPrefix + " [DEBUG-onCommand] " + " Command: " + cmd.getName());
			log.info(logPrefix + " [DEBUG-onCommand] " + " label: " + label);
			log.info(logPrefix + " [DEBUG-onCommand] " + " Args: " + Integer.parseInt(args[0]) + " Arg.length: " + args.length);
			log.info(logPrefix + " [DEBUG-onCommand] " + " Sender: " + sender.toString());
			log.info("");
		}
		
		if (debug) {
			List<World> lst = server.getWorlds();
			int listSize = 0;
			//listSize = lst.size();
			
			log.info(logPrefix + "List of Worlds:");
			while (listSize != lst.size()){
				log.info(logPrefix + listSize + ": " + lst.get(listSize).getName() + " - " + lst.get(listSize).getEnvironment().toString());
				listSize = (listSize + 1);
			}

		}
				
		if(cmd.getName().equalsIgnoreCase("blggen")){
		
			if (player.isOp()) {
				if(label.equalsIgnoreCase("blggen")){
					if(args.length == 1){
						Integer radius;
						try {
							radius = Integer.parseInt(args[0]);			//i was originally going to do a radius, but its technically diameter
							log.info(logPrefix + " Radius: " + radius);
							if ((radius > 0) | (radius <= 75)) {		// put a cap of 75 chunk diameter
								if (pluginEnabled == false){
									log.info(logPrefix + "Plugin Disabled!");
									return false;				//if server is shutting down while generating, dont generate any more!
								}
									
								generate(player, radius, player.getWorld(), player.getLocation());
							} else {
								log.info(logPrefix + args[0] + " is not valid!");
								return false;
							}
						} catch (NumberFormatException ex){
							ex.printStackTrace();
						}
					}
				}
				
			} else {
				//You must be an Op!
				player.sendMessage(chatPrefix + "You must be an OP!");
			}
			 
		return true;
		} //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
		return false; }


	private synchronized void generate(Player player, int radius, World world, Location loc) {
		// generate
		
		
			
		// ThreadVars (child thread is read only)
		threadPlayer = player;
		threadRadius = radius;
		threadWorld = world;
		threadLocation = loc;
		
		
		
		//call thread
		(new Thread(new BukkitLandGenerator())).start();
		
	}

}