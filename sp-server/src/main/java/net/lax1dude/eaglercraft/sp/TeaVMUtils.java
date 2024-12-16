package net.lax1dude.eaglercraft.sp;

import org.teavm.backend.javascript.spi.GeneratedBy;
import org.teavm.backend.javascript.spi.InjectedBy;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.typedarrays.Int16Array;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.typedarrays.Uint8Array;

public class TeaVMUtils {

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native Int8Array unwrapByteArray(byte[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapArrayBuffer.class)
	public static native ArrayBuffer unwrapArrayBuffer(byte[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native ArrayBufferView unwrapArrayBufferView(byte[] buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapTypedArray.class)
	public static native byte[] wrapByteArray(Int8Array buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBuffer.class)
	public static native byte[] wrapByteArrayBuffer(ArrayBuffer buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBufferView.class)
	public static native byte[] wrapByteArrayBufferView(ArrayBufferView buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapUnsignedTypedArray.class)
	public static native Uint8Array unwrapUnsignedByteArray(byte[] buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBufferView.class)
	public static native byte[] wrapUnsignedByteArray(Uint8Array buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native Int32Array unwrapIntArray(int[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapArrayBuffer.class)
	public static native ArrayBuffer unwrapArrayBuffer(int[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native ArrayBufferView unwrapArrayBufferView(int[] buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapTypedArray.class)
	public static native int[] wrapIntArray(Int32Array buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBuffer.class)
	public static native int[] wrapIntArrayBuffer(ArrayBuffer buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBufferView.class)
	public static native int[] wrapIntArrayBufferView(ArrayBufferView buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native Float32Array unwrapFloatArray(float[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapArrayBuffer.class)
	public static native ArrayBuffer unwrapArrayBuffer(float[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native ArrayBufferView unwrapArrayBufferView(float[] buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapTypedArray.class)
	public static native float[] wrapFloatArray(Float32Array buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBuffer.class)
	public static native float[] wrapFloatArrayBuffer(ArrayBuffer buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBufferView.class)
	public static native float[] wrapFloatArrayBufferView(ArrayBufferView buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native Int16Array unwrapShortArray(short[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapArrayBuffer.class)
	public static native ArrayBuffer unwrapArrayBuffer(short[] buf);

	@InjectedBy(TeaVMUtilsUnwrapGenerator.UnwrapTypedArray.class)
	public static native ArrayBufferView unwrapArrayBufferView(short[] buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapTypedArray.class)
	public static native short[] wrapShortArray(Int16Array buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBuffer.class)
	public static native short[] wrapShortArrayBuffer(ArrayBuffer buf);

	@GeneratedBy(TeaVMUtilsUnwrapGenerator.WrapArrayBufferView.class)
	public static native short[] wrapShortArrayBuffer(ArrayBufferView buf);

}
