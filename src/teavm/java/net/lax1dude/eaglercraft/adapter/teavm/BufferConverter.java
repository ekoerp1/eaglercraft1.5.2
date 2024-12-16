package net.lax1dude.eaglercraft.adapter.teavm;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.typedarrays.Uint8Array;

public class BufferConverter {
	
	public static final Int8Array convertByteBuffer(ByteBuffer b) {
		if(b.hasArray()) {
			int p = b.position();
			int l = b.remaining();
			return new Int8Array(TeaVMUtils.unwrapArrayBuffer(b.array()), p, l);
		}else {
			byte[] ret = new byte[b.remaining()];
			b.get(ret);
			return TeaVMUtils.unwrapByteArray(ret);
		}
	}
	
	public static final Uint8Array convertByteBufferUnsigned(ByteBuffer b) {
		if(b.hasArray()) {
			int p = b.position();
			int l = b.remaining();
			return new Uint8Array(TeaVMUtils.unwrapArrayBuffer(b.array()), p, l);
		}else {
			byte[] ret = new byte[b.remaining()];
			b.get(ret);
			return TeaVMUtils.unwrapUnsignedByteArray(ret);
		}
	}
	
	public static final Int8Array convertIntBuffer(IntBuffer b) {
		if(b.hasArray()) {
			int p = b.position() << 2;
			int l = b.remaining() << 2;
			return new Int8Array(TeaVMUtils.unwrapArrayBuffer(b.array()), p, l);
		}else {
			int[] ret = new int[b.remaining()];
			b.get(ret);
			return new Int8Array(TeaVMUtils.unwrapArrayBuffer(ret));
		}
	}
	
	public static final Uint8Array convertIntBufferUnsigned(IntBuffer b) {
		if(b.hasArray()) {
			int p = b.position() << 2;
			int l = b.remaining() << 2;
			return new Uint8Array(TeaVMUtils.unwrapArrayBuffer(b.array()), p, l);
		}else {
			int[] ret = new int[b.remaining()];
			b.get(ret);
			return new Uint8Array(TeaVMUtils.unwrapArrayBuffer(ret));
		}
	}

}
