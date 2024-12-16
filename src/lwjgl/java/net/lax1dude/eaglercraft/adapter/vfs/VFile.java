package net.lax1dude.eaglercraft.adapter.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class VFile extends File {

	public VFile(String pathname) {
		super(pathname);
	}

	public VFile(String parent, String child) {
		super(parent, child);
	}

	public VFile(File parent, String child) {
		super(parent, child);
	}

	public VFile(String parent, String child, String child2) {
		super(new VFile(parent, child), child2);
	}

	public InputStream getInputStream() {
		try {
			return Files.newInputStream(this.toPath());
		} catch (IOException e) {
			return null;
		}
	}

	public String[] getAllLines() {
		try {
			return Files.readAllLines(this.toPath(), StandardCharsets.UTF_8).toArray(new String[0]);
		} catch (IOException e) {
			return null;
		}
	}

	public String getAllChars() {
		try {
			return new String(Files.readAllBytes(this.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	public boolean setAllChars(String chars) {
		return setAllBytes(chars.getBytes(StandardCharsets.UTF_8));
	}

	public boolean setAllBytes(byte[] bytes) {
		try {
			File f = this.getParentFile();
			if (f != null) {
				f.mkdirs();
			}
			Files.write(this.toPath(), bytes);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public int deleteAll() {
		try (Stream<Path> pathStream = Files.walk(this.toPath())) {
			pathStream.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
			return 1;
		} catch (IOException e) {
			return 0;
		}
	}
}
