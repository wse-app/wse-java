package wse.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Timer {

	private final Logger logger;
	private final Level level;

	private long beginTime;
	private long endTime;

	private String operation;
	private long shouldTake;

	private long took;

	public Timer(Logger logger, Level level) {
		this.logger = logger;
		this.level = level;
	}

	public void begin(String operation) {
		begin(operation, -1);
	}

	public void begin(String operation, long shouldTake) {
		logger.log(level, operation + " starting");
		this.operation = operation;
		this.shouldTake = shouldTake;

		this.beginTime = System.currentTimeMillis();
	}

	public void end() {
		this.endTime = System.currentTimeMillis();
		this.took = endTime - beginTime;

		if (took > shouldTake) {
			logger.log(level, operation + " took " + took + "ms");
		}
	}
}
