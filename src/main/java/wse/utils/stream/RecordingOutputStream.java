package wse.utils.stream;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingOutputStream extends SplittingOutputStream {

	public RecordingOutputStream(Logger logger, Level level, String title) {
		this(null, logger, level, title, false);
	}

	public RecordingOutputStream(Logger logger, Level level, String title, boolean hex) {
		this(null, logger, level, title, hex);
	}

	public RecordingOutputStream(OutputStream original, Logger logger, Level level, String title) {
		this(original, logger, level, title, false);
	}

	public RecordingOutputStream(OutputStream original, Logger logger, Level level, String title, boolean hex) {
		super(original, new LoggingOutputStream(logger, level, title, hex));
	}
}
