package wse.utils.stream;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RecordingInputStream extends SplittingInputStream{

	public RecordingInputStream(Logger logger, Level level, String title) {
		this(logger, level, title, false);
	}
	
	public RecordingInputStream(InputStream readFrom, Logger logger, Level level, String title) {
		this(readFrom, logger, level, title, false);
	}
	
	public RecordingInputStream(Logger logger, Level level, String title, boolean hexdump) {
		this(null, logger, level, title, hexdump);
	}
	
	public RecordingInputStream(InputStream readFrom, Logger logger, Level level, String title, boolean hexdump) {
		super(readFrom, new LoggingOutputStream(logger, level, title, hexdump));
		super.propagateClose(false);
	}
}
