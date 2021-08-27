package wse.utils.log;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class WseStreamHandler extends Handler {
	private OutputStream output;
	protected boolean doneHeader;
	private volatile Writer writer;

	public WseStreamHandler() {
	}

	public WseStreamHandler(OutputStream out, Formatter formatter) {
		setFormatter(formatter);
		setOutputStream(out);
	}

	protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
		if (out == null) {
			throw new NullPointerException();
		}
		flushAndClose();
		output = out;
		doneHeader = false;
		String encoding = getEncoding();
		if (encoding == null) {
			writer = new OutputStreamWriter(output);
		} else {
			try {
				writer = new OutputStreamWriter(output, encoding);
			} catch (UnsupportedEncodingException e) {
				throw new Error("Unexpected exception " + e);
			}
		}
	}

	@Override
	public synchronized void setEncoding(String encoding)
			throws SecurityException, java.io.UnsupportedEncodingException {
		super.setEncoding(encoding);
		if (output == null) {
			return;
		}
		flush();
		if (encoding == null) {
			writer = new OutputStreamWriter(output);
		} else {
			writer = new OutputStreamWriter(output, encoding);
		}
	}

	@Override
	public synchronized void publish(LogRecord record) {
		if (!isLoggable(record)) {
			return;
		}
		String msg;
		try {
			msg = getFormatter().format(record);
		} catch (Exception e) {
			reportError(null, e, ErrorManager.FORMAT_FAILURE);
			return;
		}

		try {
			if (!doneHeader) {
				writer.write(getFormatter().getHead(this));
				doneHeader = true;
			}
			writer.write(msg);
			writer.flush();
		} catch (Exception e) {
			reportError(null, e, ErrorManager.WRITE_FAILURE);
		}
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		if (writer == null || record == null) {
			return false;
		}
		return true;
	}

	@Override
	public synchronized void flush() {
		if (writer != null) {
			try {
				writer.flush();
			} catch (Exception e) {
				reportError(null, e, ErrorManager.FLUSH_FAILURE);
			}
		}
	}

	private synchronized void flushAndClose() throws SecurityException {
		if (writer != null) {
			try {
				if (!doneHeader) {
					writer.write(getFormatter().getHead(this));
					doneHeader = true;
				}
				writer.write(getFormatter().getTail(this));
				writer.flush();
				writer.close();
			} catch (Exception e) {
				reportError(null, e, ErrorManager.CLOSE_FAILURE);
			}
			writer = null;
			output = null;
		}
	}

	@Override
	public synchronized void close() throws SecurityException {
		flushAndClose();
	}
}
