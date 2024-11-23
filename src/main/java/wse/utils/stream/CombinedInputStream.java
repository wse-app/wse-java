package wse.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import wse.utils.ArrayUtils;

public class CombinedInputStream extends WseInputStream {

	private InputStream[] streams;
	private boolean[] close;

	private int current = 0;

	public CombinedInputStream(Collection<InputStream> streams) {
		this(streams.toArray(new InputStream[streams.size()]));
	}

	public CombinedInputStream(InputStream... streamsInOrder) {
		super(streamsInOrder.length > 0 ? streamsInOrder[0] : null);
		this.streams = streamsInOrder;
		this.close = ArrayUtils.makeArray(false, streams.length);
	}

	public CombinedInputStream(byte[]... data) {
		super(data.length > 0 ? new ByteArrayInputStream(data[0]) : null);
		this.streams = new InputStream[data.length];
		for (int i = 0; i < data.length; i++)
			this.streams[i] = new ByteArrayInputStream(data[i]);
		this.close = ArrayUtils.makeArray(false, streams.length);
	}

	public CombinedInputStream closeWhenDone(boolean closeWhenDone) {
		close = ArrayUtils.makeArray(closeWhenDone, streams.length);
		return this;
	}

	public CombinedInputStream closeWhenDone(boolean... closeWhenDone) {
		if (closeWhenDone.length != streams.length) {
			throw new IllegalArgumentException("Must specify boolean closeWhenDone for each stream");
		}

		close = closeWhenDone;
		return this;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		if (current >= streams.length)
			return -1;
		int r = streams[current].read();
		if (r == -1) {
			if (close[current])
				streams[current].close();
			current++;
			return read();
		}
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (current >= streams.length)
			return -1;

		int r = streams[current].read(b, off, len);
		if (r == -1) {
			if (close[current])
				streams[current].close();
			current++;
			return read(b, off, len);
		}
		return r;
	}

	public void close() {
		for (InputStream s : streams) {
			try {
				s.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public int available() throws IOException {
		int res = 0;
		for (InputStream r : streams) {
			res += r.available();
		}
		return res;
	}

	@Override
	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		if (level != 0)
			b.append(layers[level % layers.length]);
		b.append(this.infoName());

		for (InputStream s : streams) {
			if (s instanceof WseInputStream) {
				b.append(((WseInputStream) s).layerInfo(level + 1));
			} else {
				b.append(layers[(level + 1) % layers.length] + s.getClass().getName());
			}
		}
		return b.toString();
	}

	@Override
	public void setTarget(InputStream target) {

		if (streams.length == 0) {
			this.streams = new InputStream[] { target };
			super.setTarget(target);
			return;
		}

		this.streams[0] = target;
		super.setTarget(target);
		current = 0;
	}

}
