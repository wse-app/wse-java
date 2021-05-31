package wse.utils.log;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class WseConsoleHandler extends WseStreamHandler {

	private WseConsoleHandler(OutputStream out) {
		this(out, new WseFormatter());
	}

	private WseConsoleHandler(OutputStream out, Formatter formatter) {
		super(out, formatter);
		setLevel(Level.OFF);
	}
	
	public static class Err extends WseConsoleHandler {
		public Err() {
			super(System.err);
		}

		public Err(Formatter formatter) {
			super(System.err, formatter);
		}

		@Override
		public boolean isLoggable(LogRecord record) {
			return record.getLevel().intValue() >= Level.SEVERE.intValue();
		}
	}

	public static class Out extends WseConsoleHandler {
		public Out() {
			super(System.out);
		}

		public Out(Formatter formatter) {
			super(System.out, formatter);
		}

		@Override
		public boolean isLoggable(LogRecord record) {
			return record.getLevel().intValue() < Level.SEVERE.intValue();
		}
	}

	public static void addToLogger(Logger logger) {
		logger.addHandler(new Out());
		logger.addHandler(new Err());
	}

	public static void addToLogger(Logger logger, Formatter formatter) {
		logger.addHandler(new Out(formatter));
		logger.addHandler(new Err(formatter));
	}
}
