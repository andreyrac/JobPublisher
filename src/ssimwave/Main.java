package ssimwave;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import ssimwave.job.JobPublisher;
import ssimwave.util.Logger;

public class Main
{
	private static void printUsage()
	{
		System.out.println("Usage: <cmd> [-options] [<managers> [<workers>]]");
		System.out.println("<manager> pertains to the number of managers intended to run;");
		System.out.println("          must be at least "+ JobPublisher.DEFAULT_NUMBER_OF_MANAGERS);
		System.out.println("<workers> pertains to the number of worker threads per manager intended to");
		System.out.println("          perform tasks; must be at least " + JobPublisher.DEFAULT_NUMBER_OF_WORKERS);
		System.out.println("options include:");
		System.out.println("    -d           activates debug logging");
		System.out.println("    -h           prints this help message and exits");
		System.out.println("    -l<logfile>  to specify logging file; default is 'logger.txt'");
		System.out.println("    -a           indicates that we should append to the logging file");
	}

	/**
	 * Attempts to input numberOfManagers and numberOfWorkersPerManager.
	 * If only one numeric argument is provided, numberOfManagers is set.
	 * Anything not set is defaulted to JobPublisher.DEFAULT_*
	 */
	public static void main(String[] args) throws Throwable
	{
		String logfile = "logger.txt";
		boolean append = false;
		boolean setDebug = false;

		int numberOfManagers = JobPublisher.DEFAULT_NUMBER_OF_MANAGERS;
		int numberOfWorkersPerManager = JobPublisher.DEFAULT_NUMBER_OF_WORKERS;
		int argsAccepted = 0;
		int number;

		// input arguments, quit if first two are invalid, ignore extras
		for (String arg : args)
		{
			if ("-h".equals(arg))
			{
				printUsage();
				return;
			}

			if ("-d".equals(arg))
			{
				setDebug = true;
				continue;
			}

			if ("-a".equals(arg))
			{
				append = true;
				continue;
			}

			if (arg.startsWith("-l"))
			{
				if (arg.length() == 1)
				{
					System.out.println("Invalid log file specified: " + arg);
					printUsage();
					return;
				}
				logfile = arg.substring(2);
				continue;
			}

			// warn user of ignored arguments
			if (argsAccepted >= 2)
			{
				System.out.println("Ignoring argument: " + arg);
				continue;
			}

			try
			{
				// input argument
				number = Integer.parseInt(arg);
			}
			catch(NumberFormatException nfe)
			{
				System.out.println("Invalid argument: " + arg);
				printUsage();
				return;
			}

			// assign value
			if (argsAccepted == 0)
			{
				if (number < JobPublisher.DEFAULT_NUMBER_OF_MANAGERS) // check range
				{
					System.out.println(
						"Number of managers must be at least 3: " + arg);
					printUsage();
					return;
				}
				numberOfManagers = number;
				argsAccepted++;
			}
			else
			{
				if (number < JobPublisher.DEFAULT_NUMBER_OF_WORKERS) // check range
				{
					System.out.println(
						"Number of workers per manager must be at least 10: " +
						arg);
					printUsage();
					return;
				}
				numberOfWorkersPerManager = number;
				argsAccepted++;
			}
		}

		// initialize logger
		try
		{
			Logger.init(new PrintStream(
				new FileOutputStream(logfile, append)));
			if (setDebug) Logger.setDebug(true);
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("Failed to open logfile: " + fnfe.getMessage());
			return;
		}
		catch(SecurityException se)
		{
			System.out.println("Access Denied for specified logfile: " +
				se.getMessage());
			return;
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
			System.out.println("Throwable caught in main: " + t.getMessage());
		}
	}
}