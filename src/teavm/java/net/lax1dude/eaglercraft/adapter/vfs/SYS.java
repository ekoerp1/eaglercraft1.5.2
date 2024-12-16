package net.lax1dude.eaglercraft.adapter.vfs;

public class SYS {

	public static final VirtualFilesystem VFS;

	static {

		VirtualFilesystem.VFSHandle vh = VirtualFilesystem.openVFS("eagStorage2");

		if(vh.vfs == null) {
			System.err.println("Could not init filesystem!");
			throw new RuntimeException("Could not init filesystem: VFSHandle.vfs was null");
		}

		VFS = vh.vfs;

	}
}
