# JobPublisher
Example of a JobPublisher that initiates Work which sleeps for 1-5000 milliseconds


1. HOW TO BUILD

Requirements - At a minimum, Java 1.5.0 is required.  This was developed using 1.8.0_92.

A) With ANT:
	Open setEnv.bat and ensure that your JAVA_HOME is pointing to the correct location.
	Then run the following commands:
	> setEnv.bat
	> ant build

B) Without ANT
	Ensure that "javac" is in your path, then run the following:
	> build.bat


2. HOW TO RUN

Requirements - as with building, Java 1.5.0 is the minimum required version of java.
You have the ability to pass arguments into the program; usage is as provided below:

Usage: <cmd> [-options] [<managers> [<workers>]]
<manager> pertains to the number of managers intended to run;
          must be at least 3
<workers> pertains to the number of worker threads per manager intended to
          perform tasks; must be at least 10
options include:
    -d           activates debug logging
    -h           prints this help message and exits
    -l<logfile>  to specify logging file; default is 'logger.txt'
    -a           indicates that we should append to the logging file

A) With ANT:
	If you wish to have ant run this program, that is well within the realm of possibilities.
	Please note that user input is not displayed until the 'enter' key is hit.
	Simply enter the following command:
	> ant run
	If you wish to pass parameters, you must do so via -Dargs, for example:
	> ant run -Dargs="5 15 -a -d"

B) Without ANT:
	Running without ant is a little more fluid
	> run.bat 
	Arguments work as normal, for example:
	> run.bat 5 15 -llogger2.txt


3. HOW TO USE JobPublisher
	Once you have the program running, it will prompt the user for the number of jobs to request.
	Enter any number, and that many work items will be entered into the queue and processed by all managers and workers.
	The work items will have a random assigned work time length that is between 1 and 5000 milliseconds inclusive.
	There are also two special commands: 'q' and 'd'.
	'q' will shutdown all Managers and their associated Workers but will not interrupt the currently ongoing work.
	'd' will toggle the logger's debug mode so as to become more of less verbose.

