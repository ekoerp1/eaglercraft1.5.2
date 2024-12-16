package net.lax1dude.eaglercraft.sp;

import java.io.IOException;

import net.lax1dude.eaglercraft.sp.ipc.IPCPacket14StringList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EnumGameType;
import net.minecraft.src.ILogAgent;
import net.minecraft.src.WorldSettings;

public class EAGMinecraftServer extends MinecraftServer {
	
	protected int difficulty;
	protected EnumGameType gamemode;
	protected long lastTick;
	protected WorkerListenThread listenThreadImpl;
	protected WorldSettings newWorldSettings;
	protected boolean paused;
	private int tpsCounter = 0;
	private int tpsMeasure = 0;
	private long tpsTimer = 0l;

	public EAGMinecraftServer(String world, String owner, WorldSettings currentWorldSettings) {
		super(world);
		this.setServerOwner(owner);
		System.out.println("server owner: " + owner);
		this.setConfigurationManager(new EAGPlayerList(this));
		this.listenThreadImpl = new WorkerListenThread(this);
		this.newWorldSettings = currentWorldSettings;
		this.paused = false;
	}
	
	public void setBaseServerProperties(int difficulty, EnumGameType gamemode) {
		this.difficulty = difficulty;
		this.gamemode = gamemode;
		this.setCanSpawnAnimals(true);
		this.setCanSpawnNPCs(true);
		this.setAllowPvp(true);
		this.setAllowFlight(true);
	}
	
	public void mainLoop() {
		long ctm = SysUtil.steadyTimeMillis();
		
		long elapsed = ctm - tpsTimer;
		if(elapsed >= 1000l) {
			tpsTimer = ctm;
			tpsMeasure = tpsCounter;
			IntegratedServer.sendIPCPacket(new IPCPacket14StringList(IPCPacket14StringList.SERVER_TPS, getTPSAndChunkBuffer(tpsMeasure)));
			tpsCounter = 0;
		}
		
		if(paused && this.playersOnline.size() <= 1) {
			lastTick = ctm;
			return;
		}
		
		long delta = ctm - lastTick;
		
		if (delta > 2000L && ctm - this.timeOfLastWarning >= 15000L) {
			this.getLogAgent().func_98236_b("Can\'t keep up! Did the system time change, or is the server overloaded? Skipping " + ((delta - 2000l) / 50l) + " ticks");
			delta = 2000L;
			this.timeOfLastWarning = ctm;
		}

		if (delta < 0L) {
			this.getLogAgent().func_98236_b("Time ran backwards! Did the fucking system time change?");
			delta = 0L;
		}
		
		if (this.worldServers[0].areAllPlayersAsleep()) {
			this.tick();
			++tpsCounter;
			lastTick = SysUtil.steadyTimeMillis();
		} else {
			if (delta > 50l) {
				delta -= 50L;
				lastTick += 50l;
				this.tick();
				++tpsCounter;
			}
		}
		
	}
	
	public void setPaused(boolean p) {
		paused = p;
		if(!p) {
			lastTick = SysUtil.steadyTimeMillis();
		}
	}
	
	public boolean getPaused() {
		return paused;
	}

	@Override
	protected boolean startServer() throws IOException {
		SkinsPlugin.reset();
		VoiceChatPlugin.reset();
		this.loadAllWorlds(folderName, 0l, newWorldSettings);
		this.lastTick = SysUtil.steadyTimeMillis();
		return true;
	}

	@Override
	public void stopServer() {
		super.stopServer();
		SkinsPlugin.reset();
		VoiceChatPlugin.reset();
	}

	@Override
	public boolean canStructuresSpawn() {
		return false;
	}

	@Override
	public EnumGameType getGameType() {
		return gamemode;
	}

	@Override
	public int getDifficulty() {
		return difficulty;
	}

	@Override
	public boolean isHardcore() {
		return false;
	}

	@Override
	public boolean isDedicatedServer() {
		return false;
	}

	@Override
	public boolean isCommandBlockEnabled() {
		return true;
	}

	@Override
	public WorkerListenThread getNetworkThread() {
		return listenThreadImpl;
	}

	@Override
	public String shareToLAN(EnumGameType var1, boolean var2) {
		return null;
	}

	@Override
	public ILogAgent getLogAgent() {
		return IntegratedServer.logger;
	}

}
