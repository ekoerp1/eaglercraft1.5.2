package net.lax1dude.eaglercraft.sp;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.platform.Platform;
import org.teavm.platform.PlatformRunnable;

public class SysUtil {

	private static final JSObject steadyTimeFunc = getSteadyTimeFunc();

	@JSBody(params = { }, script = "return ((typeof performance !== \"undefined\") && (typeof performance.now === \"function\"))"
			+ "? performance.now.bind(performance)"
			+ ": (function(epochStart){ return function() { return Date.now() - epochStart; }; })(Date.now());")
	private static native JSObject getSteadyTimeFunc();

	@JSBody(params = { "steadyTimeFunc" }, script = "return steadyTimeFunc();")
	private static native double steadyTimeMillis0(JSObject steadyTimeFunc);

	public static long steadyTimeMillis() {
		return (long)steadyTimeMillis0(steadyTimeFunc);
	}

	public static long nanoTime() {
		return (long)(steadyTimeMillis0(steadyTimeFunc) * 1000000.0);
	}

	@Async
	public static native void sleep(int millis);

	private static void sleep(int millis, final AsyncCallback<Void> callback) {
		Platform.schedule(new DumbSleepHandler(callback), millis);
	}

	private static class DumbSleepHandler implements PlatformRunnable {
		private final AsyncCallback<Void> callback;
		private DumbSleepHandler(AsyncCallback<Void> callback) {
			this.callback = callback;
		}
		@Override
		public void run() {
			callback.complete(null);
		}
	}

	private static boolean hasCheckedImmediateContinue = false;
	private static MessageChannel immediateContinueChannel = null;
	private static Runnable currentContinueHack = null;
	private static final JSString emptyJSString = JSString.valueOf("");

	public static void immediateContinue() {
		if(!hasCheckedImmediateContinue) {
			hasCheckedImmediateContinue = true;
			checkImmediateContinueSupport();
		}
		if(immediateContinueChannel != null) {
			immediateContinueTeaVM();
		}else {
			sleep(0);
		}
	}

	@Async
	private static native void immediateContinueTeaVM();

	private static void immediateContinueTeaVM(final AsyncCallback<Void> cb) {
		if(currentContinueHack != null) {
			cb.error(new IllegalStateException("Worker thread is already waiting for an immediate continue callback!"));
			return;
		}
		currentContinueHack = () -> {
			cb.complete(null);
		};
		try {
			immediateContinueChannel.getPort2().postMessage(emptyJSString);
		}catch(Throwable t) {
			System.err.println("Caught error posting immediate continue, using setTimeout instead");
			Window.setTimeout(() -> cb.complete(null), 0);
		}
	}

	private static void checkImmediateContinueSupport() {
		try {
			immediateContinueChannel = null;
			if(!MessageChannel.supported()) {
				System.err.println("Fast immediate continue will be disabled for server context due to MessageChannel being unsupported");
				return;
			}
			immediateContinueChannel = new MessageChannel();
			immediateContinueChannel.getPort1().addEventListener("message", new EventListener<MessageEvent>() {
				@Override
				public void handleEvent(MessageEvent evt) {
					Runnable toRun = currentContinueHack;
					currentContinueHack = null;
					if(toRun != null) {
						toRun.run();
					}
				}
			});
			immediateContinueChannel.getPort1().start();
			immediateContinueChannel.getPort2().start();
			final boolean[] checkMe = new boolean[1];
			checkMe[0] = false;
			currentContinueHack = () -> {
				checkMe[0] = true;
			};
			immediateContinueChannel.getPort2().postMessage(emptyJSString);
			if(checkMe[0]) {
				currentContinueHack = null;
				if(immediateContinueChannel != null) {
					safeShutdownChannel(immediateContinueChannel);
				}
				immediateContinueChannel = null;
				System.err.println("Fast immediate continue will be disabled for server context due to actually continuing immediately");
				return;
			}
			sleep(10);
			currentContinueHack = null;
			if(!checkMe[0]) {
				if(immediateContinueChannel != null) {
					safeShutdownChannel(immediateContinueChannel);
				}
				immediateContinueChannel = null;
				System.err.println("Fast immediate continue will be disabled for server context due to startup check failing");
			}
		}catch(Throwable t) {
			System.err.println("Fast immediate continue will be disabled for server context due to exceptions");
			if(immediateContinueChannel != null) {
				safeShutdownChannel(immediateContinueChannel);
			}
			immediateContinueChannel = null;
		}
	}

	private static void safeShutdownChannel(MessageChannel chan) {
		try {
			chan.getPort1().close();
		}catch(Throwable tt) {
		}
		try {
			chan.getPort2().close();
		}catch(Throwable tt) {
		}
	}

}
