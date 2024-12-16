package net.minecraft.src;

import net.lax1dude.eaglercraft.adapter.vfs.VFile;

import java.io.IOException;
import java.io.InputStream;

public class TexturePackFolder extends TexturePackImplementation {
	public TexturePackFolder(String par1, VFile par2, ITexturePack par3ITexturePack) {
		super(par1, par2, par2.getName(), par3ITexturePack);
	}

	protected InputStream func_98139_b(String par1Str) throws IOException {
		VFile var2 = new VFile(this.texturePackFile, par1Str.substring(1));

		if (!var2.exists()) {
			throw new IOException(par1Str);
		} else {
			return var2.getInputStream();
		}
	}

	public boolean func_98140_c(String par1Str) {
		VFile var2 = new VFile(this.texturePackFile, par1Str);
		return var2.exists();
	}

	public boolean isCompatible() {
		return true;
	}
}
