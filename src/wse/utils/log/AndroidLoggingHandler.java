package wse.utils.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import android.util.Log;

public class AndroidLoggingHandler extends Handler {

	@Override
	public void publish(LogRecord logRecord) {
		if (!super.isLoggable(logRecord))
			return;

		String TAG = String.valueOf(logRecord.getLoggerName());
		if (TAG.length() > 23)
			TAG = TAG.substring(0, 23);

		try {
			int level = getAndroidLevel(logRecord.getLevel());
			Log.println(level, TAG, logRecord.getMessage());
			if (logRecord.getThrown() != null) {
				Log.println(level, TAG, Log.getStackTraceString(logRecord.getThrown()));
			}
		} catch (Exception e) {
			Log.e("AndroidLoggingHandler", "Failed to log message", e);
		}
	}

	private static int getAndroidLevel(Level level) {
		int value = level.intValue();

		if (value >= Level.SEVERE.intValue()) {
			return Log.ERROR;
		} else if (value >= Level.WARNING.intValue()) {
			return Log.WARN;
		} else if (value >= Level.INFO.intValue()) {
			return Log.INFO;
		} else {
			return Log.DEBUG;
		}
	}

	public static void addToLogger(Logger logger) {
		logger.addHandler(new AndroidLoggingHandler());
	}

	public void flush() {
	}

	public void close() {
	}
}
