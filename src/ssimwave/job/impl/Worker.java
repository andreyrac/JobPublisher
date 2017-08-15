package ssimwave.job.impl;

import ssimwave.job.Work;
import ssimwave.util.Logger;

/**
 * Worker class that receives assigned work from JobManager, executes work and
 * and reports the work done back to the JobManager.
 */
public class Worker implements Runnable
{
	private JobManager manager;
	private int id;
	private String workerLabel;

	// member variables to be accessed when synchonized
	private Work work;
	private boolean kill = false;

	/**
	 * Create a Worker with an associated index and manager
	 * @param manager the manager/owner of this worker
	 * @param id an associated ID assigned by the manager
	 */
	public Worker(JobManager manager, int id)
	{
		this.manager = manager;
		this.id = id;
		this.workerLabel = String.format("Worker[%d,%d]", manager.getId(), id);
		new Thread(this).start();
	}

	/**
	 * Assign work to worker to be executed on current thread.
	 * @param work the work to be done (cannot be null)
	 * @throws RuntimeException if work is null; currently working on new work;
	 *	or Worker has previously receive kill command
	 */
	public synchronized void assign(Work work)
	{
		if (kill)
		{
			throw new RuntimeException(workerLabel +
				" instructed to die while being assigned new work");
		}
		if (work == null)
		{
			throw new RuntimeException(workerLabel + " assigned null work");
		}
		if (this.work != null)
		{
			throw new RuntimeException(workerLabel +
				" work assigned before previous work completed");
		}

		Logger.debug("Assigning Work[%d] to %s", work.getId(), workerLabel);

		this.work = work;
		this.notify();
	}

	/**
	 * Kills the worker thread so as to no longer be used.
	 * Will not interrupt work ongoing work or previously assigned work.
	 */
	public synchronized void kill()
	{
		this.kill = true;
		this.notify();
	}

	/**
	 * Runs continuously waiting for more work. Dies when kill command is sent.
	 * @overrides Runnable.run
	 */
	public void run()
	{
		try
		{
			Work work = null;
			for (;;)
			{
				Logger.debug("%s synchronizing", workerLabel);
				synchronized (this)
				{
					// clear work, then inform manager to avoid race condition
					if (work != null)
					{
						this.work = null;
						manager.workDone(work, id);
						work = null;
					}

					while (work == null)
					{
						// check if we should die
						if (kill)
						{
							if (this.work != null)
							{
								manager.workNotDone(this.work, id, null);
							}
							Logger.debug("%s killed", workerLabel);
							return;
						}

						// check if work was assigned before wait initiated to
						// avoid another race condition
						work = this.work;
						if (work == null)
						{
							// BLOCKING: wait for more work
							this.wait();
						}
					} // while
				} // synchronized

				Logger.debug("%s doing work", workerLabel);
				try
				{
					work.doWork(); // BLOCKING: doing work
					Logger.debug("%s completed Work[%d]", workerLabel,
						work.getId());
				}
				catch(Throwable t)
				{
					manager.workNotDone(this.work, id, t);
					Logger.debug("%s failed to complete Work[%d]", workerLabel,
						work.getId());
				}
			} // for (;;)
		}
		catch (Throwable t)
		{
			Logger.throwable("%s.run throwable: ", t, workerLabel);
		}
	}
}
