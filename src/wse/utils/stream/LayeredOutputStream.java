package wse.utils.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.utils.shttp.SKey;

public class LayeredOutputStream extends WseOutputStream {

//	private List<WseOutputStream> streams;

	private WseOutputStream first;
	private WseOutputStream last;

	public LayeredOutputStream(WseOutputStream writeTo) {
		super(writeTo);
		first = writeTo;
		last = null;
	}

	@Override
	public void write(int b) throws IOException {
		super.total_write += 1;
		first.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.total_write += len;
		first.write(b, off, len);
	}

	@Override
	public void disableOutputLogging() {
		first.disableOutputLogging();
	}

	public LayeredOutputStream before(WseOutputStream stream) {
		if (first == writeTo) {
			first = stream;
			last = stream;
			stream.setTarget(writeTo);
		} else {
			stream.setTarget(first);
			first = stream;
		}

		return this;
	}

	public LayeredOutputStream then(WseOutputStream stream) {

		if (first == writeTo) {
			first = stream;
			last = stream;
			stream.setTarget(writeTo);
		} else {
			last.setTarget(stream);
			last = stream;
			stream.setTarget(writeTo);
		}
		return this;
	}

	@Override
	public void setTarget(OutputStream target) {
		super.setTarget(target);
		last.setTarget(target);
	}

	public void addChunked(int chunkSize) {
		this.then(new ChunkedOutputStream(chunkSize));
	}

	public void record(Logger log, Level level, String title) {
		record(log, level, title, false);
	}

	public void record(Logger log, Level level, String title, boolean hex) {
		this.then(new RecordingOutputStream(log, level, title, hex));
	}

	public void sHttpEncrypt(SKey skey) {
		then(new RIMOutputStream(skey.getBlockSize(), skey.getInjectionSize()));
		then(new SHttpEncryptingOutputStream(skey, skey.getBlockSize(), skey.getBlockSize() * 512));
	}

	@Override
	public void flush() throws IOException {
		first.flush();
	}

	public String layerInfo(int level) {
		StringBuilder b = new StringBuilder();
		if (level != 0)
			b.append(layers[level % layers.length]);
		b.append(infoName());
		b.append(first.layerInfo(level + 1));

		return b.toString();
	}
}
