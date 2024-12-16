package net.lax1dude.eaglercraft.glemu;

import static net.lax1dude.eaglercraft.EaglerAdapter.*;

public class StreamBuffer {

	public static final int poolSize = 16;

	public final int initialSize;
	public final int initialCount;
	public final int maxCount;

	protected static final PoolInstance[] pool = new PoolInstance[poolSize];
	protected static int poolBufferID = 0;

	static {
		for(int i = 0; i < poolSize; ++i) {
			pool[i] = new PoolInstance();
		}
	}

	protected static class PoolInstance {
		
		protected BufferGL vertexBuffer = null;
		protected int vertexBufferSize = 0;
		
	}

	private static PoolInstance fillPoolInstance() {
		PoolInstance ret = pool[poolBufferID++];
		if(poolBufferID > poolSize - 1) {
			poolBufferID = 0;
		}
		return ret;
	}

	private static void resizeInstance(PoolInstance instance, int requiredMemory) {
		if(instance.vertexBuffer == null) {
			instance.vertexBuffer = _wglCreateBuffer();
		}
		if(instance.vertexBufferSize < requiredMemory) {
			int newSize = (requiredMemory & 0xFFFFF000) + 0x2000;
			_wglBindBuffer(_wGL_ARRAY_BUFFER, instance.vertexBuffer);
			_wglBufferData00(_wGL_ARRAY_BUFFER, newSize, _wGL_STREAM_DRAW);
			instance.vertexBufferSize = newSize;
		}
	}

	protected StreamBufferInstance[] buffers;

	protected int currentBufferId = 0;
	protected int overflowCounter = 0;

	protected final IStreamBufferInitializer initializer;

	public static class StreamBufferInstance {

		protected PoolInstance poolInstance = null;
		protected BufferArrayGL vertexArray = null;

		public boolean bindQuad16 = false;
		public boolean bindQuad32 = false;

		public BufferArrayGL getVertexArray() {
			return vertexArray;
		}

		public BufferGL getVertexBuffer() {
			return poolInstance.vertexBuffer;
		}

	}

	public static interface IStreamBufferInitializer {
		void initialize(BufferArrayGL vertexArray, BufferGL vertexBuffer);
	}

	public StreamBuffer(int initialSize, int initialCount, int maxCount, IStreamBufferInitializer initializer) {
		if(maxCount > poolSize) {
			maxCount = poolSize;
		}
		this.buffers = new StreamBufferInstance[initialCount];
		for(int i = 0; i < this.buffers.length; ++i) {
			StreamBufferInstance j = new StreamBufferInstance();
			j.poolInstance = fillPoolInstance();
			this.buffers[i] = j;
		}
		this.initialSize = initialSize;
		this.initialCount = initialCount;
		this.maxCount = maxCount;
		this.initializer = initializer;
	}

	public StreamBufferInstance getBuffer(int requiredMemory) {
		StreamBufferInstance next = buffers[(currentBufferId++) % buffers.length];
		resizeInstance(next.poolInstance, requiredMemory);
		if(next.vertexArray == null) {
			next.vertexArray = _wglCreateVertexArray();
			initializer.initialize(next.vertexArray, next.poolInstance.vertexBuffer);
		}
		return next;
	}

	public void optimize() {
		overflowCounter += currentBufferId - buffers.length;
		if(overflowCounter < -25) {
			int newCount = buffers.length - 1 + ((overflowCounter + 25) / 5);
			if(newCount < initialCount) {
				newCount = initialCount;
			}
			if(newCount < buffers.length) {
				StreamBufferInstance[] newArray = new StreamBufferInstance[newCount];
				for(int i = 0; i < buffers.length; ++i) {
					if(i < newArray.length) {
						newArray[i] = buffers[i];
					}else {
						if(buffers[i].vertexArray != null) {
							_wglDeleteVertexArray(buffers[i].vertexArray);
						}
					}
				}
				buffers = newArray;
				refill();
			}
			overflowCounter = 0;
		}else if(overflowCounter > 15) {
			int newCount = buffers.length + 1 + ((overflowCounter - 15) / 5);
			if(newCount > maxCount) {
				newCount = maxCount;
			}
			if(newCount > buffers.length) {
				StreamBufferInstance[] newArray = new StreamBufferInstance[newCount];
				for(int i = 0; i < newArray.length; ++i) {
					if(i < buffers.length) {
						newArray[i] = buffers[i];
					}else {
						newArray[i] = new StreamBufferInstance();
					}
				}
				buffers = newArray;
				refill();
			}
			overflowCounter = 0;
		}
		currentBufferId = 0;
	}

	private void refill() {
		for(int i = 0; i < buffers.length; ++i) {
			PoolInstance j = fillPoolInstance();
			StreamBufferInstance k = buffers[i];
			if(j != k.poolInstance) {
				PoolInstance l = k.poolInstance;
				k.poolInstance = j;
				if(k.vertexArray != null) {
					if(j.vertexBuffer == null) {
						resizeInstance(j, l.vertexBufferSize);
					}
					initializer.initialize(k.vertexArray, j.vertexBuffer);
				}
			}
		}
	}

	public void destroy() {
		for(int i = 0; i < buffers.length; ++i) {
			StreamBufferInstance next = buffers[i];
			if(next.vertexArray != null) {
				_wglDeleteVertexArray(next.vertexArray);
			}
		}
		buffers = new StreamBufferInstance[initialCount];
		for(int i = 0; i < initialCount; ++i) {
			StreamBufferInstance j = new StreamBufferInstance();
			j.poolInstance = fillPoolInstance();
			buffers[i] = j;
		}
	}

	public static void destroyPool() {
		for(int i = 0; i < pool.length; ++i) {
			if(pool[i].vertexBuffer != null) {
				_wglDeleteBuffer(pool[i].vertexBuffer);
				pool[i].vertexBuffer = null;
			}
		}
	}

}
