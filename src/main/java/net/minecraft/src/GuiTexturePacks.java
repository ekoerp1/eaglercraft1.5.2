package net.minecraft.src;

import net.lax1dude.eaglercraft.EPKDecompiler;
import net.lax1dude.eaglercraft.EaglerAdapter;
import net.lax1dude.eaglercraft.EaglerInputStream;
import net.lax1dude.eaglercraft.adapter.vfs.VFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GuiTexturePacks extends GuiScreen {
	protected GuiScreen guiScreen;
	private int refreshTimer = -1;

	/** the absolute location of this texture pack */
	private String fileLocation = "";

	private boolean isSelectingPack = false;

	/**
	 * the GuiTexturePackSlot that contains all the texture packs and their
	 * descriptions
	 */
	private GuiTexturePackSlot guiTexturePackSlot;
	private GameSettings field_96146_n;

	protected static final VFile texturePackListFile = new VFile("__LIST__");

	public GuiTexturePacks(GuiScreen par1, GameSettings par2) {
		this.guiScreen = par1;
		this.field_96146_n = par2;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	public void initGui() {
		StringTranslate var1 = StringTranslate.getInstance();
		this.buttonList.add(new GuiSmallButton(5, this.width / 2 - 154, this.height - 48, var1.translateKey("texturePack.openFolder")));
		this.buttonList.add(new GuiSmallButton(6, this.width / 2 + 4, this.height - 48, var1.translateKey("gui.done")));
		this.mc.texturePackList.updateAvaliableTexturePacks();
		this.fileLocation = "texturepacks";
		this.guiTexturePackSlot = new GuiTexturePackSlot(this);
		this.guiTexturePackSlot.registerScrollButtons(this.buttonList, 7, 8);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (par1GuiButton.enabled) {
			if (par1GuiButton.id == 5) {
				isSelectingPack = true;
				EaglerAdapter.openFileChooser("epk,.zip", null);
			} else if (par1GuiButton.id == 6) {
				// this.mc.renderEngine.refreshTextures();
				this.mc.displayGuiScreen(guiScreen);
			} else {
				this.guiTexturePackSlot.actionPerformed(par1GuiButton);
			}
		}
	}

	/**
	 * Called when the mouse is clicked.
	 */
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
	}

	/**
	 * Called when the mouse is moved or a mouse button is released. Signature:
	 * (mouseX, mouseY, which) which==-1 is mouseMove, which==0 or which==1 is
	 * mouseUp
	 */
	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		super.mouseMovedOrUp(par1, par2, par3);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.guiTexturePackSlot.drawScreen(par1, par2, par3);

		if (this.refreshTimer <= 0) {
			this.mc.texturePackList.updateAvaliableTexturePacks();
			this.refreshTimer += 20;
		}

		StringTranslate var4 = StringTranslate.getInstance();
		this.drawCenteredString(this.fontRenderer, var4.translateKey("texturePack.title"), this.width / 2, 16, 16777215);
		this.drawCenteredString(this.fontRenderer, var4.translateKey("texturePack.folderInfo"), this.width / 2 - 77, this.height - 26, 8421504);
		super.drawScreen(par1, par2, par3);
	}

	/**
	 * Called from the main game loop to update the screen.
	 */
	public void updateScreen() {
		super.updateScreen();
		--this.refreshTimer;
		if (isSelectingPack && EaglerAdapter.getFileChooserResultAvailable()) {
			isSelectingPack = false;
			String name = EaglerAdapter.getFileChooserResultName();
			String safeName = name.replaceAll("[^A-Za-z0-9_]", "_");
			if (texturePackListFile.exists()) {
				texturePackListFile.setAllChars(texturePackListFile.getAllChars() + "\n" + safeName);
			} else {
				texturePackListFile.setAllChars(safeName);
			}
			try {
				if (name.toLowerCase().endsWith(".zip")) {
					try(ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(EaglerAdapter.getFileChooserResult()))) {
						ZipEntry entry;
						while ((entry = zipInputStream.getNextEntry()) != null) {
							if (entry.isDirectory()) continue;
							new VFile(fileLocation, safeName, entry.getName()).setAllBytes(EaglerInputStream.inputStreamToBytesNoClose(zipInputStream));
						}
					}
				} else {
					EPKDecompiler epkDecompiler = new EPKDecompiler(EaglerAdapter.getFileChooserResult());
					EPKDecompiler.FileEntry file;
					while ((file = epkDecompiler.readFile()) != null) {
						new VFile(fileLocation, safeName, file.name).setAllBytes(file.data);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			EaglerAdapter.clearFileChooserResult();
			this.mc.displayGuiScreen(this);
		}
	}

	@Override
	public void confirmClicked(boolean par1, int par2) {
		this.mc.displayGuiScreen(this);

		List var3 = this.mc.texturePackList.availableTexturePacks();

		if (par1) {
			this.mc.texturePackList.setTexturePack((ITexturePack) var3.get(0));
			this.mc.renderEngine.refreshTextures();
			this.mc.renderGlobal.loadRenderers();
			String safeName = ((ITexturePack) var3.get(par2)).getTexturePackFileName();
			new VFile(fileLocation, safeName).deleteAll();
			if (texturePackListFile.exists()) {
				String res = texturePackListFile.getAllChars().replaceFirst(safeName, "").replace("\n\n", "\n");
				if (res.isEmpty()) {
					texturePackListFile.delete();
				} else {
					texturePackListFile.setAllChars(res);
				}
			}
		} else {
			try {
				this.mc.texturePackList.setTexturePack((ITexturePack) var3.get(par2));
				this.mc.renderEngine.refreshTextures();
				this.mc.renderGlobal.loadRenderers();
			} catch (Exception var5) {
				var5.printStackTrace();
				this.mc.texturePackList.setTexturePack((ITexturePack) var3.get(0));
				this.mc.renderEngine.refreshTextures();
				this.mc.renderGlobal.loadRenderers();
				String safeName = ((ITexturePack) var3.get(par2)).getTexturePackFileName();
				new VFile(fileLocation, safeName).deleteAll();
				if (texturePackListFile.exists()) {
					String res = texturePackListFile.getAllChars().replaceFirst(safeName, "").replace("\n\n", "\n");
					if (res.isEmpty()) {
						texturePackListFile.delete();
					} else {
						texturePackListFile.setAllChars(res);
					}
				}
			}
		}
	}
}
