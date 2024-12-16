package net.lax1dude.eaglercraft.sp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.RegionFile;

public class WorldConverterMCA {

	public static void importWorld(byte[] archiveContents, String newName) throws IOException {
		String folderName = newName.replaceAll("[\\./\"]", "_");
		VFile worldDir = new VFile("worlds", folderName);
		while((new VFile(worldDir, "level.dat")).exists() || (new VFile(worldDir, "level.dat_old")).exists()) {
			folderName += "_";
			worldDir = new VFile("worlds", folderName);
		}
		List<char[]> fileNames = new ArrayList<>();
		try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archiveContents))) {
			ZipEntry folderNameFile = null;
			while((folderNameFile = zis.getNextEntry()) != null) {
				if (folderNameFile.getName().contains("__MACOSX/")) continue;
				if (folderNameFile.isDirectory()) continue;
				String lowerName = folderNameFile.getName().toLowerCase();
				if (!(lowerName.endsWith(".dat") || lowerName.endsWith(".dat_old") || lowerName.endsWith(".mca") || lowerName.endsWith(".mcr"))) continue;
				fileNames.add(folderNameFile.getName().toCharArray());
			}
		}
		final int[] i = new int[] { 0 };
		while(fileNames.get(0).length > i[0] && fileNames.stream().allMatch(w -> w[i[0]] == fileNames.get(0)[i[0]])) i[0]++;
		int folderPrefixOffset = i[0];
		try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archiveContents))) {
			ZipEntry f = null;
			int lastProgUpdate = 0;
			int prog = 0;
			while ((f = zis.getNextEntry()) != null) {
				if (f.getName().contains("__MACOSX/")) continue;
				if (f.isDirectory()) continue;
				String lowerName = f.getName().toLowerCase();
				if (!(lowerName.endsWith(".dat") || lowerName.endsWith(".dat_old") || lowerName.endsWith(".mca") || lowerName.endsWith(".mcr") || lowerName.endsWith(".bmp"))) continue;
				byte[] b;
				int sz = (int)f.getSize();
				if(sz >= 0) {
					b = new byte[sz];
					int j = 0, k;
					while(j < b.length && (k = zis.read(b, j, b.length - j)) != -1) {
						j += k;
					}
				}else {
					b = inputStreamToBytesNoClose(zis);
				}
				String fileName = f.getName().substring(folderPrefixOffset);
				if (fileName.equals("level.dat") || fileName.equals("level.dat_old")) {
					NBTTagCompound worldDatNBT = CompressedStreamTools.readCompressed(new ByteArrayInputStream(b));
					worldDatNBT.getCompoundTag("Data").setString("LevelName", newName);
					worldDatNBT.getCompoundTag("Data").setLong("LastPlayed", System.currentTimeMillis());
					ByteArrayOutputStream bo = new ByteArrayOutputStream();
					CompressedStreamTools.writeCompressed(worldDatNBT, bo);
					b = bo.toByteArray();
					VFile ff = new VFile(worldDir, fileName);
					ff.setAllBytes(b);
					prog += b.length;
				} else if ((fileName.endsWith(".mcr") || fileName.endsWith(".mca")) && (fileName.startsWith("region/") || fileName.startsWith("DIM1/region/") || fileName.startsWith("DIM-1/region/"))) {
					VFile chunkFolder = new VFile(worldDir, fileName.startsWith("DIM1") ? "level1" : (fileName.startsWith("DIM-1") ? "level-1" : "level0"));
					RegionFile mca = new RegionFile(new RandomAccessMemoryFile(b, b.length));
					for(int j = 0; j < 32; ++j) {
						for(int k = 0; k < 32; ++k) {
							if(mca.isChunkSaved(j, k)) {
								NBTTagCompound chunkNBT;
								NBTTagCompound chunkLevel;
								try {
									chunkNBT = CompressedStreamTools.read(mca.getChunkDataInputStream(j, k));
									if(!chunkNBT.hasKey("Level")) {
										throw new IOException("Chunk is missing level data!");
									}
									chunkLevel = chunkNBT.getCompoundTag("Level");
								}catch(Throwable t) {
									System.err.println("Could not read chunk: " + j + ", " + k);
									t.printStackTrace();
									continue;
								}
								int chunkX = chunkLevel.getInteger("xPos");
								int chunkZ = chunkLevel.getInteger("zPos");
								VFile chunkOut = new VFile(chunkFolder, VFSChunkLoader.getChunkPath(chunkX, chunkZ) + ".dat");
								if(chunkOut.exists()) {
									System.err.println("Chunk already exists: " + chunkOut.getPath());
									continue;
								}
								ByteArrayOutputStream bao = new ByteArrayOutputStream();
								CompressedStreamTools.writeCompressed(chunkNBT, bao);
								b = bao.toByteArray();
								chunkOut.setAllBytes(b);
								prog += b.length;
								if (prog - lastProgUpdate > 25000) {
									lastProgUpdate = prog;
									IntegratedServer.updateStatusString("selectWorld.progress.importing.1", prog);
								}
							}
						}
					}
				} else if (fileName.startsWith("data/") || fileName.startsWith("players/")) {
					VFile ff = new VFile(worldDir, fileName);
					ff.setAllBytes(b);
					prog += b.length;
				}
			}
		}
		String[] worldsTxt = SYS.VFS.getFile("worlds.txt").getAllLines();
		if(worldsTxt == null || worldsTxt.length <= 0 || (worldsTxt.length == 1 && worldsTxt[0].trim().length() <= 0)) {
			worldsTxt = new String[] { folderName };
		}else {
			String[] tmp = worldsTxt;
			worldsTxt = new String[worldsTxt.length + 1];
			System.arraycopy(tmp, 0, worldsTxt, 0, tmp.length);
			worldsTxt[worldsTxt.length - 1] = folderName;
		}
		SYS.VFS.getFile("worlds.txt").setAllChars(String.join("\n", worldsTxt));
	}

	public static byte[] exportWorld(String folderName) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		VFile worldFolder;
		try(ZipOutputStream zos = new ZipOutputStream(bao)) {
			zos.setComment("contains backup of world '" + folderName + "'");
			worldFolder =  new VFile("worlds", folderName);
			VFile vf = new VFile(worldFolder, "level.dat");
			byte[] b;
			int lastProgUpdate = 0;
			int prog = 0;
			boolean safe = false;
			if(vf.exists()) {
				zos.putNextEntry(new ZipEntry(folderName + "/level.dat"));
				b = vf.getAllBytes();
				zos.write(b);
				prog += b.length;
				safe = true;
			}
			vf = new VFile(worldFolder, "level.dat_old");
			if(vf.exists()) {
				zos.putNextEntry(new ZipEntry(folderName + "/level.dat_old"));
				b = vf.getAllBytes();
				zos.write(b);
				prog += b.length;
				safe = true;
			}
			if (prog - lastProgUpdate > 25000) {
				lastProgUpdate = prog;
				IntegratedServer.updateStatusString("selectWorld.progress.exporting.2", prog);
			}
			String[] srcFolderNames = new String[] { "level0", "level-1", "level1" };
			String[] dstFolderNames = new String[] { "/region/", "/DIM-1/region/", "/DIM1/region/" };
			List<VFile> fileList;
			for(int i = 0; i < 3; ++i) {
				vf = new VFile(worldFolder, srcFolderNames[i]);
				fileList = SYS.VFS.listVFiles(vf.getPath());
				String regionFolder = folderName + dstFolderNames[i];
				Map<String,RegionFile> regionFiles = new HashMap<>();
				for(int k = 0, l = fileList.size(); k < l; ++k) {
					VFile chunkFile = fileList.get(k);
					NBTTagCompound chunkNBT;
					NBTTagCompound chunkLevel;
					try {
						b = chunkFile.getAllBytes();
						chunkNBT = CompressedStreamTools.readCompressed(new ByteArrayInputStream(b));
						if(!chunkNBT.hasKey("Level")) {
							throw new IOException("Chunk is missing level data!");
						}
						chunkLevel = chunkNBT.getCompoundTag("Level");
					}catch(IOException t) {
						System.err.println("Could not read chunk: " + chunkFile.getPath());
						t.printStackTrace();
						continue;
					}
					int chunkX = chunkLevel.getInteger("xPos");
					int chunkZ = chunkLevel.getInteger("zPos");
					String regionFileName = "r." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".mca";
					RegionFile rf = regionFiles.get(regionFileName);
					if(rf == null) {
						rf = new RegionFile(new RandomAccessMemoryFile(new byte[65536], 0));
						regionFiles.put(regionFileName, rf);
					}
					try(DataOutputStream dos = rf.getChunkDataOutputStream(chunkX & 31, chunkZ & 31)) {
						CompressedStreamTools.write(chunkNBT, dos);
					}catch(IOException t) {
						System.err.println("Could not write chunk to " + regionFileName + ": " + chunkFile.getPath());
						t.printStackTrace();
						continue;
					}
					prog += b.length;
					if (prog - lastProgUpdate > 25000) {
						lastProgUpdate = prog;
						IntegratedServer.updateStatusString("selectWorld.progress.exporting.2", prog);
					}
				}
				if(regionFiles.isEmpty()) {
					System.err.println("No region files were generated");
					continue;
				}
				for(Entry<String,RegionFile> etr : regionFiles.entrySet()) {
					String regionPath = regionFolder + etr.getKey();
					zos.putNextEntry(new ZipEntry(regionPath));
					zos.write(etr.getValue().getFile().getByteArray());
				}
			}
			fileList = SYS.VFS.listVFiles((new VFile(worldFolder, "data")).getPath());
			for(int k = 0, l = fileList.size(); k < l; ++k) {
				VFile dataFile = fileList.get(k);
				zos.putNextEntry(new ZipEntry(folderName + "/data/" + dataFile.getName()));
				b = dataFile.getAllBytes();
				zos.write(b);
				prog += b.length;
				if (prog - lastProgUpdate > 25000) {
					lastProgUpdate = prog;
					IntegratedServer.updateStatusString("selectWorld.progress.exporting.2", prog);
				}
			}
			fileList = SYS.VFS.listVFiles((new VFile(worldFolder, "players")).getPath());
			for(int k = 0, l = fileList.size(); k < l; ++k) {
				VFile dataFile = fileList.get(k);
				zos.putNextEntry(new ZipEntry(folderName + "/players/" + dataFile.getName()));
				b = dataFile.getAllBytes();
				zos.write(b);
				prog += b.length;
				if (prog - lastProgUpdate > 25000) {
					lastProgUpdate = prog;
					IntegratedServer.updateStatusString("selectWorld.progress.exporting.2", prog);
				}
			}
		}
		return bao.toByteArray();
	}

	private static byte[] inputStreamToBytesNoClose(InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buf = new byte[1024];
		int i;
		while ((i = is.read(buf)) != -1) {
			os.write(buf, 0, i);
		}
		return os.toByteArray();
	}

}
