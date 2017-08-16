package ssimwave.job.impl;

import ssimwave.job.Work;

public class SleepWork implements Work
{
	private int id;
	private long jobTimeLengthMillis;

	/**
	 * @param jobTimeLengthMillis time, in milliseconds, which the job will
	 *	take to complete
	 */
	public SleepWork(long jobTimeLengthMillis)
	{
		this.jobTimeLengthMillis = jobTimeLengthMillis;
	}

	/**
	 * Blocks until work is done
	 */
	public void doWork() throws Exception
	{
		Thread.sleep(this.jobTimeLengthMillis);
	}

	/**
	 * Sets the works id, retrievable by 'getId()'
	 * @param id identifier for Work item
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * @return the Work's ID
	 */
	public int getId()
	{
		return id;
	}

	public long getWorkLength()
	{
		return jobTimeLengthMillis;
	}
}