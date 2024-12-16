package net.lax1dude.eaglercraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.jcraft.jzlib.InflaterInputStream;

public class EaglerMisc {

	public static byte[] uncompress(byte[] input) throws IOException {
		return EaglerInputStream.inputStreamToBytes(new InflaterInputStream(new EaglerInputStream(input)));
	}

	public static String bytesToString(byte[] bb) {
		if (bb == null)
			return "";
		return new String(bb, StandardCharsets.UTF_8);
	}

	public static String[] bytesToLines(byte[] bb) {
		String contents = bytesToString(bb);
		if (contents.isEmpty()) {
			return new String[0];
		} else {
			return contents.replace("\r\n", "\n").split("[\r\n]");
		}
	}
}
