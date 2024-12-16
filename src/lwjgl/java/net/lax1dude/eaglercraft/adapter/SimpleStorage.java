package net.lax1dude.eaglercraft.adapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleStorage {
	private static final Path directory;
	private static final boolean available = true;

	public static boolean isAvailable() {
		return available;
	}

	static {
		File file = new File("eagstorage");
		file.mkdirs();
		directory = file.toPath().toAbsolutePath();
	}

	public static byte[] get(String key) {
		try {
			return Files.readAllBytes(directory.resolve(key));
		} catch (IOException e) {
			return null;
		}
	}

	public static Boolean set(String key, byte[] value) {
		try {
			if (value == null) {
				Files.deleteIfExists(directory.resolve(key));
			} else {
				Files.write(directory.resolve(key), value);
			}
			return Boolean.TRUE;
		} catch (IOException e) {
			return Boolean.FALSE;
		}
	}

	public static String[] list() {
		try {
			return Files.list(directory).map(Path::getFileName).toArray(String[]::new);
		} catch (IOException e) {
			return new String[0];
		}
	}
}