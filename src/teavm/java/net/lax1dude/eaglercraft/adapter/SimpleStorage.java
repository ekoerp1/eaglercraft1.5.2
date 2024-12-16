package net.lax1dude.eaglercraft.adapter;

import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.core.JSString;
import org.teavm.jso.indexeddb.IDBDatabase;
import org.teavm.jso.indexeddb.IDBFactory;
import org.teavm.jso.indexeddb.IDBGetRequest;
import org.teavm.jso.indexeddb.IDBObjectStore;
import org.teavm.jso.indexeddb.IDBOpenDBRequest;
import org.teavm.jso.indexeddb.IDBRequest;
import org.teavm.jso.typedarrays.ArrayBuffer;

import net.lax1dude.eaglercraft.adapter.teavm.TeaVMUtils;

public class SimpleStorage {
	private static IDBDatabase database;
	private static boolean available;

	public static boolean isAvailable() {
		return available;
	}

	static {
		IDBOpenDBRequest request = IDBFactory.getInstance().open("eagstorage", 1);
		request.setOnUpgradeNeeded(evt -> {
			database = request.getResult();
			database.createObjectStore("store");
		});
		request.setOnSuccess(() -> {
			database = request.getResult();
			available = true;
		});
		request.setOnError(() -> {
			database = request.getResult();
			available = false;
		});
	}

	private static IDBObjectStore getStore() {
		return database.transaction(new String[] { "store" }, "readwrite").objectStore("store");
	}

	@Async
	public static native byte[] get(String key);

	private static void get(String key, final AsyncCallback<byte[]> cb) {
		if (key.equals("__LIST__") || key.contains("\n")) {
			cb.complete(null);
			return;
		}
		IDBGetRequest request = getStore().get(JSString.valueOf(key));
		request.setOnSuccess(() -> {
			cb.complete(TeaVMUtils.wrapByteArrayBuffer((ArrayBuffer) request.getResult()));
		});
		request.setOnError(() -> {
			cb.complete(null);
		});
	}

	@Async
	public static native Boolean set(String key, byte[] value);

	private static void set(String key, byte[] value, final AsyncCallback<Boolean> cb) {
		if (key.equals("__LIST__") || key.contains("\n")) {
			cb.complete(false);
			return;
		}
		if (value == null) {
			IDBGetRequest request3 = getStore().get(JSString.valueOf("__LIST__"));
			request3.setOnSuccess(() -> {
				String listVal;
				if (JSString.isInstance(request3.getResult()) && !(listVal = ((JSString) request3.getResult().cast()).stringValue()).isEmpty()) {
					String[] list = listVal.replaceAll("[^a-zA-Z0-9_\n]", "").split("\n");
					String[] newList = new String[list.length - 1];
					int a = 0;
					for (int i = 0; i < list.length; ++i) {
						if (list[i].equals(key)) {
							--a;
						} else {
							newList[i + a] = list[i];
						}
					}
					IDBRequest request2 = getStore().put(JSString.valueOf(String.join("\n", newList)), JSString.valueOf("__LIST__"));
					request2.setOnSuccess(() -> {
						IDBRequest request = getStore().delete(JSString.valueOf(key));
						request.setOnSuccess(() -> {
							cb.complete(Boolean.TRUE);
						});
						request.setOnError(() -> {
							cb.complete(Boolean.FALSE);
						});
					});
					request2.setOnError(() -> {
						cb.complete(Boolean.FALSE);
					});
				} else {
					IDBRequest request = getStore().delete(JSString.valueOf(key));
					request.setOnSuccess(() -> {
						cb.complete(Boolean.TRUE);
					});
					request.setOnError(() -> {
						cb.complete(Boolean.FALSE);
					});
				}
			});
			request3.setOnError(() -> {
				IDBRequest request = getStore().delete(JSString.valueOf(key));
				request.setOnSuccess(() -> {
					cb.complete(Boolean.TRUE);
				});
				request.setOnError(() -> {
					cb.complete(Boolean.FALSE);
				});
			});
		} else {
			ArrayBuffer arr = TeaVMUtils.unwrapArrayBuffer(value);
			IDBRequest request2 = getStore().put(arr, JSString.valueOf(key));
			request2.setOnSuccess(() -> {
				IDBGetRequest request3 = getStore().get(JSString.valueOf("__LIST__"));
				request3.setOnSuccess(() -> {
					String listVal;
					if (JSString.isInstance(request3.getResult()) && !(listVal = ((JSString) request3.getResult().cast()).stringValue()).isEmpty()) {
						String[] list = listVal.replaceAll("[^a-zA-Z0-9_\n]", "").split("\n");
						boolean alrHas = false;
						for (String s : list) {
							if (s.equals(key)) {
								alrHas = true;
								break;
							}
						}
						String[] newList;
						if (alrHas) {
							newList = list;
						} else {
							newList = new String[list.length + 1];
							System.arraycopy(list, 0, newList, 0, list.length);
							newList[list.length] = key;
						}
						IDBRequest request = getStore().put(JSString.valueOf(String.join("\n", newList).replaceAll("[^a-zA-Z0-9_\n]", "")), JSString.valueOf("__LIST__"));
						request.setOnSuccess(() -> {
							cb.complete(Boolean.TRUE);
						});
						request.setOnError(() -> {
							cb.complete(Boolean.FALSE);
						});
					} else {
						IDBRequest request = getStore().put(JSString.valueOf(key), JSString.valueOf("__LIST__"));
						request.setOnSuccess(() -> {
							cb.complete(Boolean.TRUE);
						});
						request.setOnError(() -> {
							cb.complete(Boolean.FALSE);
						});
					}
				});
				request3.setOnError(() -> {
					IDBRequest request = getStore().put(JSString.valueOf(key), JSString.valueOf("__LIST__"));
					request.setOnSuccess(() -> {
						cb.complete(Boolean.TRUE);
					});
					request.setOnError(() -> {
						cb.complete(Boolean.FALSE);
					});
				});
			});
			request2.setOnError(() -> {
				cb.complete(Boolean.FALSE);
			});
		}
	}

	@Async
	public static native String[] list();

	private static void list(final AsyncCallback<String[]> cb) {
		IDBGetRequest request = getStore().get(JSString.valueOf("__LIST__"));
		request.setOnSuccess(() -> {
			String listVal;
			if (JSString.isInstance(request.getResult()) && !(listVal = ((JSString) request.getResult().cast()).stringValue()).isEmpty()) {
				cb.complete(listVal.replaceAll("[^a-zA-Z0-9_\n]", "").split("\n"));
			} else {
				cb.complete(new String[0]);
			}
		});
		request.setOnError(() -> {
			cb.complete(new String[0]);
		});
	}
}
