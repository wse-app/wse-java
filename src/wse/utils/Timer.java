package wse.utils;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import wse.WSE;

public class Timer {

	private Logger logger;
	private Level level;

	private long beginTime;
	private long endTime;

	private String operation;
	private long shouldTake;

	private long took;

	public Timer() {
		this(WSE.getLogger(), Level.FINE);
	}
	
	public Timer(Logger logger, Level level) {
		setLogger(logger, level);
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
	
	public void setLogger(Logger logger) {
		this.logger = Objects.requireNonNull(logger);
	}
	
	public void setLogger(Logger logger, Level level) {
		setLogger(logger);
		this.level = Objects.requireNonNull(level);
	}
}
