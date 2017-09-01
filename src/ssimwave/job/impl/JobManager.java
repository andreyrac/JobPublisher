package ssimwave.job.impl;

public abstract class JobManager implements Runnable
{
	public abstract void kill();
	/**
	 * @return Manager's assigned ID
	 */
	public abstract int getId();
}