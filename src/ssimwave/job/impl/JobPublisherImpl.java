package ssimwave.job.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Random;

import ssimwave.job.JobPublisher;
import ssimwave.job.Work;
import ssimwave.util.Logger;

/**
 * Receiver of work needed to be done. Informs Managers and grants assignment
 * of jobs upon request.
 */
public class JobPublisherImpl extends JobPublisher
{
	private static final int MAX_WORK_TIME_MILLIS = 5 * 1000;

	private JobManager[] managers;
	private WorkQueue workQueue;
	private WorkQueue activeWorkQueue;
	private Random random;
	private int workCounter = 0;
	
	/**
	 * Creates JobPublisher with given number of managers and workers per
	 *  managers.
	 * @param numberOfManagers number of managers to create and use
	 * @param numberOfWorkersPerManager number of workers per manager to
	 *	create and use
	 */
	public JobPublisherImpl(int numberOfManagers, int numberOfWorkersPerManager)
	{
		workQueue = new WorkQueue();
		activeWorkQueue = new WorkQueue();
		random = new Random(System.currentTimeMillis());

		managers = new JobManager[numberOfManagers];
		for (int i = managers.length - 1; i >= 0 ; i--)
		{
			managers[i] = new JobManager(this, numberOfWorkersPerManager, i);
		}
	}

	/**
	 * Creates JobPublisher with DEFAULT_NUMBER_OF_MANAGERS number of managers
	 *	and DEFAULT_NUMBER_OF_WORKERS workers per managers.
	 */
	public JobPublisherImpl()
	{
		this(DEFAULT_NUMBER_OF_MANAGERS, DEFAULT_NUMBER_OF_WORKERS);
	}

	/**
	 * Runs a loop that will keep inputting integer values pertaining to the
	 * number of jobs to add to the queue which will take a randomized amount
	 * of time between 1 and 5 seconds. Entering 'q' will result in exiting
	 * the loop and returning
	 */
	public void runLoop()
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(
			System.in));
		String input = "";
		int jobs;

		for (;;)
		{
			// input number of jobs from user
			System.out.println("Please enter jobs to request: ");
			try
			{
				input = br.readLine();
			}
			catch(IOException ioe)
			{
				Logger.throwable(
					"An I/O Exception has occurred during user input: ", ioe);
				break;
			}

			// check if we should quit out nicely
			if ("q".equals(input))
			{
				Logger.info("User requested to quit");
				kill();
				break;
			}
			else if ("d".equals(input))
			{
				Logger.setDebug(!Logger.getDebug());
			}

			// pass on the number of jobs to the publisher
			try
			{
				jobs = Integer.parseInt(input);
				if (jobs <= 0)
				{
					System.out.println("Number must be greater than 0");
					continue;
				}
			}
			catch(NumberFormatException nfe)
			{
				System.out.println(
					"Invalid input, must be a number (base 10).");
				continue;
			}

			// enqueue jobs
			Logger.info("User requested %d jobs", jobs);
			enqueueJobs(jobs);
		}
	}

	/**
	 * @return null if no new work to be had
	 */
	public Work getWork()
	{
		Work work = workQueue.dequeue();
		if (work != null) activeWorkQueue.enqueue(work);
		return work;
	}

	/**
	 * Called by manager when work is done
	 */
	public void workDone(Work work)
	{
		if (!activeWorkQueue.dequeue(work))
		{
			Logger.error("Work done not found: %d", work.getId());
		}
		else
		{
			Logger.info("Work done: %d", work.getId());
		}
	}

	public void workNotDone(Work work, Throwable t)
	{
		if (t != null)
		{
			Logger.throwable("Work has thrown a throwable: ", t);
		}
		else if (!activeWorkQueue.dequeue(work))
		{
			Logger.error("Work not done not found: %d", work.getId());
		}
		else
		{
			Logger.info("Work not done: %d", work.getId());
			workQueue.enqueueFirst(work);
		}
	}

	/**
	 * @param numberOfJobsToEnqueue number of jobs to enqueue with a randomly
	 *	assigned work time between 0 and 5 seconds
	 */
	private void enqueueJobs(int numberOfJobsToEnqueue)
	{
		// if the work queue was empty, notify managers
		boolean notifyManagers = workQueue.isEmpty();
		for (int i = 0 ; i < numberOfJobsToEnqueue ; i++)
		{
			int workId = workCounter++;
			long jobTimeLengthMillis = 1+random.nextInt(MAX_WORK_TIME_MILLIS);
			Logger.info("SleepWork[%d,%d] enqueued", workId,
				jobTimeLengthMillis);
			SleepWork work = new SleepWork(jobTimeLengthMillis);
			work.setId(workId);
			workQueue.enqueue(work);
		}

		// return early if managers don't need to be notified
		if (!notifyManagers) return;
		for (JobManager manager : managers)
		{
			synchronized (manager)
			{
				manager.notify();
			}
		}
	}

	private void kill()
	{
		for (JobManager manager : managers)
		{
			manager.kill();
		}
	}
}