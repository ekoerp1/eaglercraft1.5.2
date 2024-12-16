package net.lax1dude.eaglercraft.sp;

import java.io.IOException;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;

public class WorldConverterEPK {

	public static void importWorld(byte[] archiveContents, String newName) throws IOException {
		String folder = VFSSaveHandler.worldNameToFolderName(newName);
		VFile dir = new VFile("worlds", folder);
		EPKDecompiler dc = new EPKDecompiler(archiveContents);
		EPKDecompiler.FileEntry f = null;
		int lastProgUpdate = 0;
		int prog = 0;
		boolean hasReadType = dc.isOld();
		while((f = dc.readFile()) != null) {
			byte[] b = f.data;
			if(!hasReadType) {
				if(f.type.equals("HEAD") && f.name.equals("file-type") && EPKDecompiler.readASCII(f.data).equals("epk/world152")) {
					hasReadType = true;
					continue;
				}else {
					throw new IOException("file does not contain a singleplayer 1.5.2 world!");
				}
			}
			if(f.type.equals("FILE")) {
				if(f.name.equals("level.dat")) {
					NBTTagCompound worldDatNBT = CompressedStreamTools.decompress(b);
					worldDatNBT.getCompoundTag("Data").setString("LevelName", newName);
					worldDatNBT.getCompoundTag("Data").setLong("LastPlayed", System.currentTimeMillis());
					b = CompressedStreamTools.compress(worldDatNBT);
				}
				VFile ff = new VFile(dir, f.name);
				ff.setAllBytes(b);
				prog += b.length;
				if(prog - lastProgUpdate > 10000) {
					lastProgUpdate = prog;
					IntegratedServer.updateStatusString("selectWorld.progress.importing.0", prog);
				}
			}
		}
		String[] worldsTxt = SYS.VFS.getFile("worlds.txt").getAllLines();
		if(worldsTxt == null || worldsTxt.length <= 0) {
			worldsTxt = new String[] { folder };
		}else {
			String[] tmp = worldsTxt;
			worldsTxt = new String[worldsTxt.length + 1];
			System.arraycopy(tmp, 0, worldsTxt, 0, tmp.length);
			worldsTxt[worldsTxt.length - 1] = folder;
		}
		SYS.VFS.getFile("worlds.txt").setAllChars(String.join("\n", worldsTxt));
	}

	public static byte[] exportWorld(String worldName) {
		String realWorldName = worldName;
		String worldOwner = "UNKNOWN";
		int j = realWorldName.lastIndexOf(new String(new char[] { (char)253, (char)233, (char)233 }));
		if(j != -1) {
			worldOwner = realWorldName.substring(j + 3);
			realWorldName = realWorldName.substring(0, j);
		}
		final int[] bytesWritten = new int[1];
		final int[] lastUpdate = new int[1];
		String pfx = "worlds/" + realWorldName + "/";
		EPK2Compiler c = new EPK2Compiler(realWorldName, worldOwner, "epk/world152");
		SYS.VFS.iterateFiles(pfx, false, (i) -> {
			byte[] b = i.getAllBytes();
			c.append(i.path.substring(pfx.length()), b);
			bytesWritten[0] += b.length;
			if (bytesWritten[0] - lastUpdate[0] > 10000) {
				lastUpdate[0] = bytesWritten[0];
				IntegratedServer.updateStatusString("selectWorld.progress.exporting.1", bytesWritten[0]);
			}
		});
		return c.complete();
	}

}
