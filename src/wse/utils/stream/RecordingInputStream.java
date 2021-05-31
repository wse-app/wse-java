package wse.utils.stream;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RecordingInputStream extends SplittingInputStream{

	public RecordingInputStream(Logger logger, Level level, int logSize, String title) {
		this(logger, level, logSize, title, false);
	}
	
	public RecordingInputStream(InputStream readFrom, Logger logger, Level level, int logSize, String title) {
		this(readFrom, logger, level, logSize, title, false);
	}
	
	public RecordingInputStream(Logger logger, Level level, int logSize, String title, boolean hexdump) {
		this(null, logger, level, logSize, title, hexdump);
	}
	
	public RecordingInputStream(InputStream readFrom, Logger logger, Level level, int logSize, String title, boolean hexdump) {
		super(readFrom, new LoggingOutputStream(logger, level, logSize, title, hexdump));
		super.propagateClose(false);
	}
}
