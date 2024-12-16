package net.lax1dude.eaglercraft.sp;

import org.teavm.backend.javascript.codegen.SourceWriter;
import org.teavm.backend.javascript.spi.Generator;
import org.teavm.backend.javascript.spi.GeneratorContext;
import org.teavm.backend.javascript.spi.Injector;
import org.teavm.backend.javascript.spi.InjectorContext;
import org.teavm.model.MethodReference;

public class TeaVMUtilsUnwrapGenerator {

	// WARNING: This code uses internal TeaVM APIs that may not have
	// been intended for end users of the compiler to program with

	public static class UnwrapArrayBuffer implements Injector {

		@Override
		public void generate(InjectorContext context, MethodReference methodRef) {
			context.writeExpr(context.getArgument(0));
			context.getWriter().append(".data.buffer");
		}

	}

	public static class UnwrapTypedArray implements Injector {

		@Override
		public void generate(InjectorContext context, MethodReference methodRef) {
			context.writeExpr(context.getArgument(0));
			context.getWriter().append(".data");
		}

	}

	public static class WrapArrayBuffer implements Generator {

		@Override
		public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) {
	        String parName = context.getParameterName(1);
			switch (methodRef.getName()) {
			case "wrapByteArrayBuffer":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_bytecls").append(',').ws();
				writer.append("new Int8Array(").append(parName).append("))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapIntArrayBuffer":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_intcls").append(',').ws();
				writer.append("new Int32Array(").append(parName).append("))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapFloatArrayBuffer":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_floatcls").append(',').ws();
				writer.append("new Float32Array(").append(parName).append("))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapShortArrayBuffer":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_shortcls").append(',').ws();
				writer.append("new Int16Array(").append(parName).append("))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			default:
				break;
			}
		}

	}

	public static class WrapArrayBufferView implements Generator {

		@Override
		public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) {
	        String parName = context.getParameterName(1);
			switch (methodRef.getName()) {
			case "wrapByteArrayBufferView":
			case "wrapUnsignedByteArray":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_bytecls").append(',').ws();
				writer.append("new Int8Array(").append(parName).append(".buffer))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapIntArrayBufferView":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_intcls").append(',').ws();
				writer.append("new Int32Array(").append(parName).append(".buffer))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapFloatArrayBufferView":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_floatcls").append(',').ws();
				writer.append("new Float32Array(").append(parName).append(".buffer))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapShortArrayBufferView":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_shortcls").append(',').ws();
				writer.append("new Int16Array(").append(parName).append(".buffer))").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			default:
				break;
			}
		}

	}

	public static class WrapTypedArray implements Generator {

		@Override
		public void generate(GeneratorContext context, SourceWriter writer, MethodReference methodRef) {
	        String parName = context.getParameterName(1);
			switch (methodRef.getName()) {
			case "wrapByteArray":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_shortcls").append(',').ws();
				writer.append(parName).append(")").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapIntArray":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_intcls").append(',').ws();
				writer.append(parName).append(")").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapFloatArray":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_floatcls").append(',').ws();
				writer.append(parName).append(")").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			case "wrapShortArray":
				writer.append("return ").append(parName).ws().append('?').ws();
				writer.appendFunction("$rt_wrapArray").append('(').appendFunction("$rt_shortcls").append(',').ws();
				writer.append(parName).append(")").ws();
				writer.append(':').ws().append("null;").softNewLine();
				break;
			default:
				break;
			}
		}

	}

	public static class UnwrapUnsignedTypedArray implements Injector {

		@Override
		public void generate(InjectorContext context, MethodReference methodRef) {
			context.getWriter().append("new Uint8Array(");
			context.writeExpr(context.getArgument(0));
			context.getWriter().append(".data.buffer)");
		}

	}

}
