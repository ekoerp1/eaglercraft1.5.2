package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.lax1dude.eaglercraft.EPKDecompiler;
import net.lax1dude.eaglercraft.EaglerAdapter;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.lax1dude.eaglercraft.adapter.vfs.VFile;
import net.minecraft.client.Minecraft;

public class TexturePackList
{
	/**
	 * An instance of TexturePackDefault for the always available builtin texture pack.
	 */
	private static final ITexturePack defaultTexturePack = new TexturePackDefault();

	/** The Minecraft instance. */
	private final Minecraft mc;

	/** The directory the texture packs will be loaded from. */
	private final VFile texturePackDir;

	/** Folder for the multi-player texturepacks. Returns File. */
	private final VFile mpTexturePackFolder;

	/** The list of the available texture packs. */
	private List availableTexturePacks = new ArrayList();

	/**
	 * A mapping of texture IDs to TexturePackBase objects used by updateAvaliableTexturePacks() to avoid reloading
	 * texture packs that haven't changed on disk.
	 */
	private Map texturePackCache = new HashMap();

	/** The TexturePack that will be used. */
	private ITexturePack selectedTexturePack;

	/** True if a texture pack is downloading in the background. */
	private boolean isDownloading;

	public TexturePackList(Minecraft par2Minecraft)
	{
		this.mc = par2Minecraft;
		this.texturePackDir = new VFile("texturepacks");
		this.mpTexturePackFolder = new VFile("texturepacks-mp-cache");
		this.mpTexturePackFolder.deleteAll();
		this.updateAvaliableTexturePacks();
	}

	/**
	 * Sets the new TexturePack to be used, returning true if it has actually changed, false if nothing changed.
	 */
	public boolean setTexturePack(ITexturePack par1ITexturePack)
	{
		if (par1ITexturePack == this.selectedTexturePack)
		{
			return false;
		}
		else
		{
			this.isDownloading = false;
			this.selectedTexturePack = par1ITexturePack;
			this.mc.gameSettings.skin = par1ITexturePack.getTexturePackFileName();
			this.mc.gameSettings.saveOptions();
			return true;
		}
	}

	/**
	 * filename must end in .zip
	 */
	public void requestDownloadOfTexture(String par1Str)
	{
		String var2 = par1Str.substring(par1Str.lastIndexOf("/") + 1);

		if (var2.contains("?"))
		{
			var2 = var2.substring(0, var2.indexOf("?"));
		}

		if (var2.toLowerCase().endsWith(".zip") || var2.toLowerCase().endsWith(".epk"))
		{
			VFile var3 = new VFile(this.mpTexturePackFolder, var2.replaceAll("[^A-Za-z0-9_]", "_"));
			this.downloadTexture(par1Str, var3);
		}
	}

	private void downloadTexture(String par1Str, VFile par2File)
	{
		this.isDownloading = true;
		try {
			byte[] data = EaglerAdapter.downloadURL(par1Str);
			if (data == null) throw new IOException("Unable to download texture pack!");
			if (par2File.getName().toLowerCase().endsWith(".epk")) {
				EPKDecompiler epkDecompiler = new EPKDecompiler(data);
				EPKDecompiler.FileEntry file;
				while ((file = epkDecompiler.readFile()) != null) {
					new VFile(par2File, file.name).setAllBytes(file.data);
				}
			} else {
				try(ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data))) {
					ZipEntry entry;
					while ((entry = zipInputStream.getNextEntry()) != null) {
						if (entry.isDirectory()) continue;
						new VFile(par2File, entry.getName()).setAllBytes(EaglerInputStream.inputStreamToBytesNoClose(zipInputStream));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (this.isDownloading) {
			setSelectedTexturePack(this, new TexturePackFolder(TexturePackList.generateTexturePackID(this, par2File), par2File, defaultTexturePack));
			this.mc.scheduleTexturePackRefresh();
		}
	}

	/**
	 * Return true if a texture pack is downloading in the background.
	 */
	public boolean getIsDownloading()
	{
		return this.isDownloading;
	}

	/**
	 * Called from Minecraft.loadWorld() if getIsDownloading() returned true to prepare the downloaded texture for
	 * usage.
	 */
	public void onDownloadFinished()
	{
		this.isDownloading = false;
		this.updateAvaliableTexturePacks();
		this.mc.scheduleTexturePackRefresh();
	}

	/**
	 * check the texture packs the client has installed
	 */
	public void updateAvaliableTexturePacks()
	{
		ArrayList var1 = new ArrayList();
		this.selectedTexturePack = defaultTexturePack;
		var1.add(defaultTexturePack);
		Iterator var2 = this.getTexturePackDirContents().iterator();

		while (var2.hasNext())
		{
			VFile var3 = (VFile)var2.next();
			String var4 = this.generateTexturePackID(var3);

			if (var4 != null)
			{
				Object var5 = (ITexturePack)this.texturePackCache.get(var4);

				if (var5 == null)
				{
					var5 = new TexturePackFolder(var4, var3, defaultTexturePack);
					this.texturePackCache.put(var4, var5);
				}

				if (((ITexturePack)var5).getTexturePackFileName().equals(this.mc.gameSettings.skin))
				{
					this.selectedTexturePack = (ITexturePack)var5;
				}

				var1.add(var5);
			}
		}

		this.availableTexturePacks.removeAll(var1);
		var2 = this.availableTexturePacks.iterator();

		while (var2.hasNext())
		{
			ITexturePack var6 = (ITexturePack)var2.next();
			var6.deleteTexturePack(this.mc.renderEngine);
			this.texturePackCache.remove(var6.getTexturePackID());
		}

		this.availableTexturePacks = var1;
	}

	/**
	 * Generate an internal texture pack ID from the file/directory name, last modification time, and file size. Returns
	 * null if the file/directory is not a texture pack.
	 */
	private String generateTexturePackID(VFile par1File)
	{
		return (new VFile(par1File, "pack.txt")).exists() ? par1File.getName() + ":folder" : null;
	}

	/**
	 * Return a List<File> of file/directories in the texture pack directory.
	 */
	private List getTexturePackDirContents()
	{
		if (!GuiTexturePacks.texturePackListFile.exists()) return Collections.emptyList();
		String[] lines = GuiTexturePacks.texturePackListFile.getAllLines();
		List<VFile> files = new ArrayList<>();
		for (String line : lines) {
			files.add(new VFile(this.texturePackDir, line));
		}
		return files;
	}

	/**
	 * Returns a list of the available texture packs.
	 */
	public List availableTexturePacks()
	{
		return Collections.unmodifiableList(this.availableTexturePacks);
	}

	public ITexturePack getSelectedTexturePack()
	{
		return this.selectedTexturePack;
	}

	public boolean func_77300_f()
	{
		if (!this.mc.gameSettings.serverTextures)
		{
			return false;
		}
		else
		{
			ServerData var1 = this.mc.getServerData();
			return var1 == null ? true : var1.func_78840_c();
		}
	}

	public boolean getAcceptsTextures()
	{
		if (!this.mc.gameSettings.serverTextures)
		{
			return false;
		}
		else
		{
			ServerData var1 = this.mc.getServerData();
			return var1 == null ? false : var1.getAcceptsTextures();
		}
	}

	static boolean isDownloading(TexturePackList par0TexturePackList)
	{
		return par0TexturePackList.isDownloading;
	}

	/**
	 * Set the selectedTexturePack field (Inner class static accessor method).
	 */
	static ITexturePack setSelectedTexturePack(TexturePackList par0TexturePackList, ITexturePack par1ITexturePack)
	{
		return par0TexturePackList.selectedTexturePack = par1ITexturePack;
	}

	/**
	 * Generate an internal texture pack ID from the file/directory name, last modification time, and file size. Returns
	 * null if the file/directory is not a texture pack. (Inner class static accessor method).
	 */
	static String generateTexturePackID(TexturePackList par0TexturePackList, VFile par1File)
	{
		return par0TexturePackList.generateTexturePackID(par1File);
	}

	static ITexturePack func_98143_h()
	{
		return defaultTexturePack;
	}

	static Minecraft getMinecraft(TexturePackList par0TexturePackList)
	{
		return par0TexturePackList.mc;
	}
}
