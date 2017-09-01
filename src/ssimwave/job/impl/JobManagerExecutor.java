package ssimwave.job.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ssimwave.job.Work;
import ssimwave.util.Logger;

public class JobManagerExecutor extends JobManager
{
	private JobPublisherImpl jobPublisher;
	private ExecutorService executor;
	private int id;
	private int noWorkers;

	// member variables to only be accessed on synchronized
	private int busyWorkers;
	private boolean kill;

	/**
	 * 
	 * @param jobPublisher the owner and publisher to receive Work from and
	 *	report to Work done.
	 * @param numberOfWorkers the number workers the manager should create
	 * @param id the associated ID for this manager
	 */
	public JobManagerExecutor(JobPublisherImpl jobPublisher, int numberOfWorkers, int id)
	{
		this.jobPublisher = jobPublisher;
		this.id = id;
		noWorkers = numberOfWorkers;
		executor = Executors.newFixedThreadPool(numberOfWorkers);
		busyWorkers = 0;
		kill = false;
		new Thread(this).start();
	}

	/**
	 * Kills manager and all worker threads.
	 */
	public synchronized void kill()
	{
		kill = true;
		executor.shutdown();
		this.notify();
	}

	/**
	 * Thread runs, assigning work to worker, until killed.
	 * @overrides Runnable.run
	 */
	public synchronized void run()
	{
		try
		{
			for (;;)
			{
				for (;;)
				{
					// check if no free workers
					if (busyWorkers == noWorkers) break;

					// check if there is more work
					Work work = jobPublisher.getWork();
					if (work == null) break;

					noWorkers++;

					// find non-busy worker and assign it work
					executor.execute(new Runnable()
					{
						public void run()
						{
							try
							{
								work.doWork();
								workDone(work);
							}
							catch(Throwable t)
							{
								workNotDone(work, t);
							}
						}
					});
				}

				Logger.debug(String.format("Manager[%d] waiting", id));
				this.wait(); // BLOCKING: waiting for new work or free workers
				Logger.debug(String.format("Manager[%d] woke", id));
				if (kill) return; // kill thread
			}
		}
		catch(Throwable t)
		{
			Logger.throwable("Manager[%d]", t, id);
		}
	}

	/**
	 * Called by Workers to indicate last assigned job is complete.
	 * @param work the work that has been completed
	 * @param workerId the ID of the worker
	 */
	private void workDone(Work work)
	{
		jobPublisher.workDone(work);
		freeWorker();
	}

	/**
	 * Called by Workers to indicate last assigned job could not be completed.
	 * @param work the work that has been completed
	 * @param t the thrown throwable if thrown during execution
	 */
	private void workNotDone(Work work, Throwable t)
	{
		jobPublisher.workNotDone(work, t);
		freeWorker();
	}

	private synchronized void freeWorker()
	{
		if (busyWorkers == 0)
		{
			Logger.error("JobManagerExecutor.freeWorker found no busy workers");
		}
		else if (noWorkers == busyWorkers--)
		{
			notify();
		}
	}

	/**
	 * @return Manager's assigned ID
	 */
	public int getId()
	{
		return id;
	}
}