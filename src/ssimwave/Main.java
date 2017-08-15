package ssimwave;

import java.io.PrintStream;

import ssimwave.job.JobPublisher;
import ssimwave.util.Logger;

public class Main
{
	/**
	 * Attempts to input numberOfManagers and numberOfWorkersPerManager.
	 * If only one numeric argument is provided, numberOfManagers is set.
	 * Anything not set is defaulted to JobPublisher.DEFAULT_*
	 */
	public static void main(String[] args) throws Throwable
	{
		Logger.init(new PrintStream("logger.txt"));
		//Logger.setDebug(true);

		int numberOfManagers = JobPublisher.DEFAULT_NUMBER_OF_MANAGERS;
		int numberOfWorkersPerManager = JobPublisher.DEFAULT_NUMBER_OF_WORKERS;
		int argsAccepted = 0;
		int number;

		// input arguments, quit if first two are invalid, ignore extras
		for (int i = 0 ; i < args.length ; i++)
		{
			// warn user of ignored arguments
			if (argsAccepted >= 2)
			{
				Logger.error("Ignoring argument: " + args[i]);
				continue;
			}

			try
			{
				// input argument
				number = Integer.parseInt(args[i]);
			}
			catch(NumberFormatException nfe)
			{
				Logger.error("Invalid argument: " + args);
				return;
			}

			// assign value
			if (argsAccepted == 0)
			{
				if (number < 3) // check range
				{
					Logger.error("Number of managers must be at least 3: " +
						args[i]);
					return;
				}
				numberOfManagers = number;
				argsAccepted++;
			}
			else
			{
				if (number < 10) // check range
				{
					Logger.error(
						"Number of workers per manager must be at least 10: "
						+ args[i]);
					return;
				}
				numberOfWorkersPerManager = number;
				argsAccepted++;
			}
		}

		// initialize and call runLoop for JobPublisher
		try
		{
			JobPublisher.getJobPublisher(numberOfManagers, 
				numberOfWorkersPerManager).runLoop();
		}
		catch (Throwable t)
		{
			Logger.throwable("Throwable caught in main: ", t);
		}
	}
}