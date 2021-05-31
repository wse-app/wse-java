package wse.utils.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class WseFormatter extends Formatter {

	private final static String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

	static String levelName(Level level) {
		int value = level.intValue() / 100;
		if (value < 3)
			value = 3;

		switch (value) {
		case 3:
			return "T";
		case 4:
		case 5:
		case 6:
			return "D";
		case 7:
			return "C";
		case 8:
			return "I";
		case 9:
			return "W";
		default:
			return "E";
		}
	}

	@Override
	public String format(LogRecord record) {
		if (record == null)
			return "";
		if (record.getMessage() == null)
			record.setMessage("");

		try {
			StringBuilder builder = new StringBuilder(DATE_PATTERN.length() + record.getMessage().length() + 64);

			builder.append(DATE_FORMAT.format(new Date(record.getMillis())));
			builder.append(' ').append(Thread.currentThread().getName()).append(' ');
			builder.append(levelName(record.getLevel())).append('/');
			if (record.getLoggerName() != null)
				builder.append(record.getLoggerName());
			else
				builder.append('?');
			builder.append(": ");
			builder.append(record.getMessage());

			builder.append('\n');

			Throwable cause = record.getThrown();
			if (cause != null) {
				StringWriter sw = new StringWriter();
				cause.printStackTrace(new PrintWriter(sw));
				String st = sw.toString();
				builder.append(st);
			}

			return builder.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
	}
}
