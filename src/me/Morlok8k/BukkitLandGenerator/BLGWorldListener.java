package me.Morlok8k.BukkitLandGenerator;


import org.bukkit.*;
import org.bukkit.event.world.*;
import java.util.logging.Logger;


public class BLGWorldListener extends WorldListener {

	private static final Logger log = Logger.getLogger("Minecraft");
	public static final String logPrefix = "[BukkitLandGenerator] ";
	public static String chatPrefix = ChatColor.GREEN + logPrefix + ChatColor.WHITE;
	
	
	public static BukkitLandGenerator plugin;
	
	public BLGWorldListener(BukkitLandGenerator instance) {			//needed
        plugin = instance;
	}

	public void onChunkLoad(ChunkLoadEvent event) {
		//
		World world = event.getWorld();
		Chunk chunk = event.getChunk();
		boolean newChunk = event.isNewChunk();
		
		int x = chunk.getX();
		int z = chunk.getZ();
		String chunkLoc = ("{" + x + "," + z + "}");
		
		if (newChunk) {
			log.info(logPrefix + "New Chunk Loaded - " + world.getName() + " " + chunkLoc);
			// we don't unload a new chunk yet - it needs to be populated first!
			
		} else {
			log.info(logPrefix + "Existing Chunk Loaded - " + world.getName() + " " + chunkLoc);
			
			//we can get in an infinite loop here. damn!
			/*
			boolean unloaded = world.unloadChunk(chunk.getX(), chunk.getZ(), true, true);
			if (unloaded) {
				log.info(logPrefix + "Existing Chunk Unloaded!");
			}
			// we attempt to unload this existing chunk safely, if we can.
			*/
		}
		
		
	}
	
	public void onChunkUnload(ChunkUnloadEvent event) {
		//
		World world = event.getWorld();
		Chunk chunk = event.getChunk();
		
		int x = chunk.getX();
		int z = chunk.getZ();
		String chunkLoc = ("{" + x + "," + z + "}");
		
		log.info(logPrefix + "Chunk unloaded - " + world.getName() + " " + chunkLoc);
		
		
		boolean unloaded = world.unloadChunk(chunk.getX(), chunk.getZ(), true, true);
		if (unloaded) {
			log.info(logPrefix + "Existing Chunk Unloaded!");
		}
		// we attempt to unload this existing chunk safely, if we can.
		
		
		
	}
	
	
	public void onChunkPopulate(ChunkPopulateEvent event) {
		//
		World world = event.getWorld();
		Chunk chunk = event.getChunk();
		
		int x = chunk.getX();
		int z = chunk.getZ();
		String chunkLoc = ("{" + x + "," + z + "}");
		
		log.info(logPrefix + "Chunk Populated - " + world.getName() + " " + chunkLoc);
		
	}
	
	
}
