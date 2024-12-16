package net.lax1dude.eaglercraft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.EnumChatFormatting;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.StatCollector;

public class GuiScreenVSyncWarning extends GuiScreen {

	private final GuiScreen cont;
	private final List<String> messages = new ArrayList<>();
	private int top = 0;

	public GuiScreenVSyncWarning(GuiScreen cont) {
		this.cont = cont;
	}

	public void initGui() {
		messages.clear();
		messages.add(EnumChatFormatting.RED + StatCollector.translateToLocal("options.vsyncWarning.title"));
		messages.add(null);
		messages.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("options.vsyncWarning.0"));
		messages.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("options.vsyncWarning.1"));
		messages.add(null);
		messages.add(StatCollector.translateToLocal("options.vsyncWarning.2"));
		messages.add(StatCollector.translateToLocal("options.vsyncWarning.3"));
		messages.add(StatCollector.translateToLocal("options.vsyncWarning.4"));
		messages.add(StatCollector.translateToLocal("options.vsyncWarning.5"));
		messages.add(StatCollector.translateToLocal("options.vsyncWarning.6"));
		int j = 0;
		for(int i = 0, l = messages.size(); i < l; ++i) {
			if(messages.get(i) != null) {
				j += 9;
			}else {
				j += 5;
			}
		}
		top = this.height / 6 + j / -12;
		j += top;
		buttonList.clear();
		buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 16, StatCollector.translateToLocal("options.vsyncWarning.fixSettings")));
		buttonList.add(new GuiButton(1, this.width / 2 - 100, j + 40, StatCollector.translateToLocal("options.vsyncWarning.continueAnyway")));
		buttonList.add(new GuiButton(2, this.width / 2 - 100, j + 64, StatCollector.translateToLocal("options.vsyncWarning.doNotShowAgain")));
	}

	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int j = 0;
		for(int i = 0, l = messages.size(); i < l; ++i) {
			String str = messages.get(i);
			if(str != null) {
				this.drawCenteredString(fontRenderer, str, this.width / 2, top + j, 16777215);
				j += 9;
			}else {
				j += 5;
			}
		}
		super.drawScreen(par1, par2, par3);
	}

	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			mc.gameSettings.enableVsync = true;
			mc.gameSettings.saveOptions();
			mc.displayGuiScreen(cont);
		}else if(par1GuiButton.id == 1) {
			mc.displayGuiScreen(cont);
		}else if(par1GuiButton.id == 2) {
			mc.gameSettings.hideVsyncWarning = true;
			mc.gameSettings.saveOptions();
			mc.displayGuiScreen(cont);
		}
	}

}
