package ssimwave.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Synchronized logging API, uses PrintStream to output information.
 * It is advisable to call Logger.init() at beginning of application.
 */
public class Logger
{
	private static Logger logger;

	private PrintStream ps;
	private boolean printDebug = false;
	private SimpleDateFormat dateFormatter;

	private Logger(PrintStream ps)
	{
		if (ps == null) this.ps = System.out;
		else this.ps = ps;
		dateFormatter = new SimpleDateFormat("[yyyy-MM-dd:HH:mm:ss.SSSS] ");
	}

	private static Logger getLogger()
	{
		if (logger == null) logger = new Logger(System.out);
		return logger;
	}

	private void log(String format, Object... args)
	{
		logger.ps.print(dateFormatter.format(new Date()));
		logger.ps.println(String.format(format, args));
	}

	/**
	 * Initializes Logger.  PrintStream can be null.
	 * @param ps the print stream to use for Logger message;
	 *	if null, defaults to System.out
	 */
	public static void init(PrintStream ps)
	{
		logger = new Logger(ps);
	}

	/**
	 * Sets the debug mode.
	 * @param debug if true, debug messages will be printed
	 */
	public static void setDebug(boolean debug)
	{
		synchronized (getLogger())
		{
			logger.printDebug = debug;
		}
	}

	/**
	 * @return true if debug mode is set
	 */
	public static boolean getDebug()
	{
		synchronized (getLogger())
		{
			return logger.printDebug;
		}
	}

	/**
	 * @param msg message to be printed
	 */
	public static void debug(String format, Object... args)
	{
		synchronized (getLogger())
		{
			if (!logger.printDebug) return;
			logger.log("DBG: " + format, args);
		}
	}

	public static void info(String format, Object... args)
	{
		synchronized (getLogger())
		{
			logger.log(format, args);
		}
	}

	public static void error(String format, Object... args)
	{
		synchronized (getLogger())
		{
			logger.log("ERR: " + format, args);
		}
	}

	public static void throwable(String format, Throwable t, Object... args)
	{
		synchronized (getLogger())
		{
			logger.log("ERR: " + format + t.getMessage(), args );
			t.printStackTrace(logger.ps);
		}
	}
}