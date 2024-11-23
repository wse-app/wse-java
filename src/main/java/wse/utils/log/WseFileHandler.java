package wse.utils.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import wse.utils.exception.WseException;

public class WseFileHandler extends WseStreamHandler {

	private final static SimpleDateFormat SUBDIR_FORMAT = new SimpleDateFormat("yyyy-MM");
	private final static SimpleDateFormat FILENAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final long DAY_MILLIS = 60 * 60 * 24;
	
	private final File logDir;

	private final File current;
	private int lastDayOfYear = -1;
	
	private int daysKeep = -1;

	public WseFileHandler(File logDir) throws SecurityException, FileNotFoundException {
		this(logDir, -1);
	}

	public WseFileHandler(File logDir, int daysKeep) throws SecurityException, FileNotFoundException {
		this(logDir, daysKeep, new WseFormatter());
	}
	
	public WseFileHandler(File logDir, int daysKeep, Formatter formatter) throws SecurityException, FileNotFoundException {
		this.lastDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		this.logDir = logDir;
		this.daysKeep = daysKeep;
		this.current = new File(logDir, "current.log");
		setFormatter(formatter);
		init();
		setCurrentOutput();
	}

	private boolean ensureTarget() {
		Calendar c = Calendar.getInstance();
		Date now = new Date();
		c.setTime(now);

		int today = c.get(Calendar.DAY_OF_YEAR);

		if (today != lastDayOfYear) {
			lastDayOfYear = today;
			try {
				moveToHistory();
			} catch (SecurityException | FileNotFoundException e) {
				reportError(null, e, ErrorManager.OPEN_FAILURE);
				return false;
			}
		}
		return true;
	}

	private void init() throws SecurityException, FileNotFoundException {
		if (!logDir.exists()) {
			if (!logDir.mkdirs()) {
				throw new WseException("Failed to create necessesary log directories");
			}
		}

		if (current.exists()) {
			String c = FILENAME_FORMAT.format(new Date());
			String h = FILENAME_FORMAT.format(new Date(current.lastModified()));

			if (!c.equals(h)) {
				moveToHistory();
			}
		}
	}

	public void setCurrentOutput() {
		close();
		try {
			current.setReadable(true, false);
			current.setWritable(true, false);
			setOutputStream(new PrintStream(new FileOutputStream(current, true)));
		} catch (SecurityException | FileNotFoundException e) {
			// e.printStackTrace();
		}
	}

	static long start = System.currentTimeMillis();

	private void moveToHistory() throws SecurityException, FileNotFoundException {
		cleanOld();
		if (!current.exists())
			return;
		close();
		ensureSubDir(current.lastModified());
		File moveTo = history(current.lastModified());
		current.renameTo(moveTo);
		setCurrentOutput();
	}
	
	private void cleanOld() {
		if (daysKeep <= 0)
			return;
		
		File old = history(System.currentTimeMillis() - DAY_MILLIS * daysKeep);
		if (old.exists()) {
			old.delete();
		}
	}

	private void ensureSubDir(long time) {
		File subDir = new File(logDir, SUBDIR_FORMAT.format(new Date(time)));
		if (!subDir.exists()) {
			subDir.mkdirs();
		}
		subDir.setReadable(true, false);
		subDir.setWritable(true, false);
	}

	private File history(long when) {
		File subDir = new File(logDir, SUBDIR_FORMAT.format(new Date(when)));
		File file = new File(subDir, FILENAME_FORMAT.format(new Date(when)) + ".log");
		return file;
	}

	@Override
	public synchronized void publish(LogRecord record) {
		if (!ensureTarget())
			return;
		super.publish(record);
	}
}
