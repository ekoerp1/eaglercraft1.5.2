package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

import net.lax1dude.eaglercraft.EaglerMisc;
import net.minecraft.client.Minecraft;

public class AchievementMap {
	/** Holds the singleton instance of AchievementMap. */
	public static AchievementMap instance = new AchievementMap();

	/** Maps a achievement id with it's unique GUID. */
	private Map guidMap = new HashMap();

	private AchievementMap() {
		try {
			String[] strs = EaglerMisc.bytesToLines(Minecraft.getMinecraft().texturePackList.getSelectedTexturePack().getResourceAsBytes("/achievement/map.txt"));
			for(String str : strs) {
				String[] var3 = str.split(",");
				int var4 = Integer.parseInt(var3[0]);
				this.guidMap.put(Integer.valueOf(var4), var3[1]);
			}
		} catch (Exception var5) {
			var5.printStackTrace();
		}
	}

	/**
	 * Returns the unique GUID of a achievement id.
	 */
	public static String getGuid(int par0) {
		return (String) instance.guidMap.get(Integer.valueOf(par0));
	}
}
