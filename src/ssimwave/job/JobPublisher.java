package ssimwave.job;

import ssimwave.job.impl.JobPublisherImpl;

/**
 * Abstracted class that will provide a publisher via getJobPublisher().
 * Assign work to publisher via 'assignWork()' function.
 * WorkDoneListener can be set to null.
 */
public abstract class JobPublisher
{
	/**
	 * Default number of managers created
	 */
	public static final int DEFAULT_NUMBER_OF_MANAGERS = 3;

	/**
	 * Default number of workers created
	 */
	public static final int DEFAULT_NUMBER_OF_WORKERS = 10;

	/**
	 * Waits for user input to specify how many SleepWork objects to inject
	 * into the work queue.
	 */
	public abstract void runLoop();

	/**
	 * Retrieves new generic job publisher.
	 */
	public static JobPublisher getJobPublisher()
	{
		return new JobPublisherImpl();
	}

	/**
	 * Retrieves new generic job publisher that meets the parameter criteria.
	 * @param numberOfManagers the number of managers to assign to publisher
	 * @param numberOfWorkersPerManager the number of worker that each manager
	 *	should have.
	 * @param useExecutorService set to true if use of java.util.concurrent.ExecutorService is desired
	 */
	public static JobPublisher getJobPublisher(int numberOfManagers,
		int numberOfWorkersPerManager, boolean useExecutorService)
	{
		return new JobPublisherImpl(numberOfManagers, 
			numberOfWorkersPerManager);
	}
}