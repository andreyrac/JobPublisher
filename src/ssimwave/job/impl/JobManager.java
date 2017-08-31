package ssimwave.job.impl;

import ssimwave.job.Work;
import ssimwave.util.Logger;

public class JobManager implements Runnable
{
	private JobPublisherImpl jobPublisher;
	private Worker[] workers;
	private int id;

	// member variables to only be accessed on synchronized
	private boolean[] workersBusy;
	private int busyWorkers;
	private boolean kill;

	/**
	 * 
	 * @param jobPublisher the owner and publisher to receive Work from and
	 *	report to Work done.
	 * @param numberOfWorkers the number workers the manager should create
	 * @param id the associated ID for this manager
	 */
	public JobManager(JobPublisherImpl jobPublisher, int numberOfWorkers, int id)
	{
		this.jobPublisher = jobPublisher;
		this.id = id;
		workers = new Worker[numberOfWorkers];
		workersBusy = new boolean[workers.length];
		busyWorkers = 0;
		kill = false;
		for (int i = 0 ; i < workers.length ; i++)
		{
			workers[i] = new Worker(this, i);
			workersBusy[i] = false;
		}
		new Thread(this).start();
	}

	/**
	 * Kills manager and all worker threads.
	 */
	public synchronized void kill()
	{
		kill = true;
		for (int i = 0 ; i < workers.length ; i++)
		{
			workers[i].kill();
		}
		this.notify();
	}

	/**
	 * Thread runs, assigning work to worker, until killed.
	 * @overrides Runnable.run
	 */
	public synchronized void run()
	{
		int i;
		try
		{
			for (;;)
			{
				for (;;)
				{
					// check if there is more work
					Work work = jobPublisher.getWork();
					if (work == null) break;

					// check if no free workers
					if (busyWorkers == workers.length) break;

					// find non-busy worker and assign it work
					for (i = 0 ; i < workers.length ; i ++ )
					{
						if (workersBusy[i]) continue;
						workersBusy[i] = true;
						busyWorkers++;
						workers[i].assign(work);
						break;
					}

					if (i >= workers.length)
					{
						Logger.error(String.format(
							"Manager[%d] busyWorkers count out of sync", id));
						jobPublisher.workNotDone(work, null);
					}
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
	public void workDone(Work work, int workerId)
	{
		jobPublisher.workDone(work);
		freeWorker(workerId);
	}

	public void workNotDone(Work work, int workerId, Throwable t)
	{
		jobPublisher.workNotDone(work, t);
		freeWorker(workerId);
	}

	private void freeWorker(int workerId)
	{
		// check for a valid worker ID
		if (workerId < 0 || workerId > workers.length)
		{
			Logger.error("Manager[%d] worker ID is invalid: %d", id, workerId);
			return;
		}

		Logger.debug("Manager[%d,%d] synchronizing", id, workerId);
		synchronized (this)
		{
			// check that assignment implmentation isn't falling over
			if (!workersBusy[workerId])
			{
				Logger.error("Worker[%d,%d] not busy", id, workerId);
				return;
			}
			workersBusy[workerId] = false;

			Logger.debug("busyworkers[%d,%d]: %d/%d", id, workerId,
				busyWorkers, workers.length);

			// notify Manager's thread if all Workers were busy until now
			if (busyWorkers == workers.length) this.notify();
			busyWorkers--;
		}
		Logger.debug("Manager[%d,%d] unsychronizing", id, workerId);
	}

	/**
	 * @return Manager's assigned ID
	 */
	public int getId()
	{
		return id;
	}
}