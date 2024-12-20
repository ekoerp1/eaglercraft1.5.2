package net.minecraft.src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.lax1dude.eaglercraft.EaglerAdapter;
import net.lax1dude.eaglercraft.EaglerImage;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.lax1dude.eaglercraft.adapter.vfs.VFile;

public abstract class TexturePackImplementation implements ITexturePack
{
	/**
	 * Texture pack ID as returnd by generateTexturePackID(). Used only internally and not visible to the user.
	 */
	private final String texturePackID;

	/**
	 * The name of the texture pack's zip file/directory or "Default" for the builtin texture pack. Shown in the GUI.
	 */
	private final String texturePackFileName;

	/**
	 * File object for the texture pack's zip file in TexturePackCustom or the directory in TexturePackFolder.
	 */
	protected final VFile texturePackFile;

	/**
	 * First line of texture pack description (from /pack.txt) displayed in the GUI
	 */
	protected String firstDescriptionLine;

	/**
	 * Second line of texture pack description (from /pack.txt) displayed in the GUI
	 */
	protected String secondDescriptionLine;
	private final ITexturePack field_98141_g;

	/** The texture pack's thumbnail image loaded from the /pack.png file. */
	protected EaglerImage thumbnailImage;

	/** The texture id for this pcak's thumbnail image. */
	private int thumbnailTextureName = -1;

	protected TexturePackImplementation(String par1, VFile par2File, String par3Str, ITexturePack par4ITexturePack)
	{
		this.texturePackID = par1;
		this.texturePackFileName = par3Str;
		this.texturePackFile = par2File;
		this.field_98141_g = par4ITexturePack;
		this.loadThumbnailImage();
		this.loadDescription();
	}

	/**
	 * Truncate strings to at most 34 characters. Truncates description lines
	 */
	private static String trimStringToGUIWidth(String par0Str)
	{
		if (par0Str != null && par0Str.length() > 34)
		{
			par0Str = par0Str.substring(0, 34);
		}

		return par0Str;
	}

	/**
	 * Load and initialize thumbnailImage from the the /pack.png file.
	 */
	private void loadThumbnailImage()
	{
		InputStream var1 = null;

		try
		{
			var1 = this.func_98137_a("/pack.png", false);
			if (var1 != null) {
				this.thumbnailImage = EaglerImage.loadImage(EaglerInputStream.inputStreamToBytes(var1));
			}
		}
		catch (IOException var11)
		{
			;
		}
		finally
		{
			try
			{
				if (var1 != null)
				{
					var1.close();
				}
			}
			catch (IOException var10)
			{
				;
			}
		}
	}

	/**
	 * Load texture pack description from /pack.txt file in the texture pack
	 */
	protected void loadDescription()
	{
		InputStream var1 = null;
		BufferedReader var2 = null;

		try
		{
			var1 = this.func_98139_b("/pack.txt");
			var2 = new BufferedReader(new InputStreamReader(var1));
			this.firstDescriptionLine = trimStringToGUIWidth(var2.readLine());
			this.secondDescriptionLine = trimStringToGUIWidth(var2.readLine());
		}
		catch (IOException var12)
		{
			;
		}
		finally
		{
			try
			{
				if (var2 != null)
				{
					var2.close();
				}

				if (var1 != null)
				{
					var1.close();
				}
			}
			catch (IOException var11)
			{
				;
			}
		}
	}

	public InputStream func_98137_a(String par1Str, boolean par2) throws IOException
	{
		try
		{
			return this.func_98139_b(par1Str);
		}
		catch (IOException var4)
		{
			if (this.field_98141_g != null && par2)
			{
				return this.field_98141_g.func_98137_a(par1Str, true);
			}
			else
			{
				throw var4;
			}
		}
	}

	/**
	 * Gives a texture resource as InputStream.
	 */
	public byte[] getResourceAsBytes(String par1Str)
	{
		InputStream is = null;
		byte[] res = null;
		try {
			is = this.func_98137_a(par1Str, true);
			if (is == null) return null;
			res = EaglerInputStream.inputStreamToBytes(is);
		} catch (IOException e) {
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignored) {
					//
				}
			}
		}
		return res;
	}

	protected abstract InputStream func_98139_b(String var1) throws IOException;

	/**
	 * Delete the OpenGL texture id of the pack's thumbnail image, and close the zip file in case of TexturePackCustom.
	 */
	public void deleteTexturePack(RenderEngine par1RenderEngine)
	{
		if (this.thumbnailImage != null && this.thumbnailTextureName != -1)
		{
			par1RenderEngine.deleteTexture(this.thumbnailTextureName);
		}
	}

	/**
	 * Bind the texture id of the pack's thumbnail image, loading it if necessary.
	 */
	public void bindThumbnailTexture(RenderEngine par1RenderEngine)
	{
		if (this.thumbnailImage != null)
		{
			if (this.thumbnailTextureName == -1)
			{
				this.thumbnailTextureName = par1RenderEngine.allocateAndSetupTexture(this.thumbnailImage);
			}

			EaglerAdapter.glBindTexture(EaglerAdapter.GL_TEXTURE_2D, this.thumbnailTextureName);
			par1RenderEngine.resetBoundTexture();
		}
		else
		{
			par1RenderEngine.bindTexture("/gui/unknown_pack.png");
		}
	}

	public boolean func_98138_b(String par1Str, boolean par2)
	{
		boolean var3 = this.func_98140_c(par1Str);
		return !var3 && par2 && this.field_98141_g != null ? this.field_98141_g.func_98138_b(par1Str, par2) : var3;
	}

	public abstract boolean func_98140_c(String var1);

	/**
	 * Get the texture pack ID
	 */
	public String getTexturePackID()
	{
		return this.texturePackID;
	}

	/**
	 * Get the file name of the texture pack, or Default if not from a custom texture pack
	 */
	public String getTexturePackFileName()
	{
		return this.texturePackFileName;
	}

	/**
	 * Get the first line of the texture pack description (read from the pack.txt file)
	 */
	public String getFirstDescriptionLine()
	{
		return this.firstDescriptionLine;
	}

	/**
	 * Get the second line of the texture pack description (read from the pack.txt file)
	 */
	public String getSecondDescriptionLine()
	{
		return this.secondDescriptionLine;
	}
}
