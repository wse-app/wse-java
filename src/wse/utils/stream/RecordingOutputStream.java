package wse.utils.stream;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingOutputStream extends SplittingOutputStream {

	public RecordingOutputStream(Logger logger, Level level, int logSize, String title) {
		this(null, logger, level, logSize, title, false);
	}

	public RecordingOutputStream(Logger logger, Level level, int logSize, String title, boolean hex) {
		this(null, logger, level, logSize, title, hex);
	}

	public RecordingOutputStream(OutputStream original, Logger logger, Level level, int logSize, String title) {
		this(original, logger, level, logSize, title, false);
	}

	public RecordingOutputStream(OutputStream original, Logger logger, Level level, int logSize, String title,
			boolean hex) {
		super(original, new LoggingOutputStream(logger, level, logSize, title, hex));
	}
}
