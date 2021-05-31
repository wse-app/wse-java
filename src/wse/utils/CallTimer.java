package wse.utils;

import java.util.logging.Logger;

public class CallTimer
{
	
	private Logger logger;
	
	private long beginTime;
	private long endTime;
	
	private String operation;
	private long shouldTake;
	
	private long took;
	
	public CallTimer(Logger logger)
	{
		this.logger = logger;
	}
	
	public void begin(String operation)
	{
		begin(operation, -1);
	}

	public void begin(String operation, long shouldTake)
	{
		this.operation = operation;
		this.shouldTake = shouldTake;
		
		this.beginTime = System.currentTimeMillis();
	}
	
	
	public void end()
	{
		this.endTime = System.currentTimeMillis();
		this.took = endTime - beginTime;
		
		if (took > shouldTake)
		{
			logger.finer(operation + " took " + took + "ms");
		}
	}
}
