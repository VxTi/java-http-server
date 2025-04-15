package nl.getgood.api;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A simple logger class that logs messages to the console.
 * Created on 03/08/2024 at 23:26
 * by Luca Warmenhoven.
 */
public class Logger
{

    private PrintStream errorStream = System.err;
    private PrintStream outputStream = System.out;

    private static final Logger instance = new Logger();

    private static boolean verbose = false;

    private Logger() {}

    /**
     * Returns the default logger instance.
     */
    public static Logger getLogger()
    {
        return instance;
    }

    /**
     * Sets the verbose flag for the logger.
     * If verbose is enabled, more detailed logging will be done.
     *
     * @param doVerbose The flag to set.
     */
    public static void setVerbose( boolean doVerbose )
    {
        verbose = doVerbose;
    }

    /**
     * Returns the prefix for the logger.
     */
    private String getPrefix()
    {
        return String.format("[%s]: ", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()));
    }

    /**
     * Logs a message to the default logger.
     *
     * @param message The message to log.
     */
    public void info( String message, Object ... args)
    {
        this.outputStream.printf(getPrefix().concat(message).concat("\n"), args);
    }

    /**
     * Logs a message to the default logger if verbose is enabled.
     * This allows for more detailed logging.
     *
     * @param message The message to log.
     */
    public void verbose( String message, Object ... args)
    {
        if (verbose)
            this.outputStream.printf(getPrefix().concat(message).concat("\n"), args);
    }

    /**
     * Logs an error message to the default logger.
     *
     * @param message The error message to log.
     */
    public void error(String message, Object ... args)
    {
        this.errorStream.printf(getPrefix().concat(message).concat("\n"), args);
    }

    /**
     * Prints the error stack trace to the registered error loggers.
     *
     * @param error The error to log.
     */
    public void errorStack(Exception error)
    {
        error.printStackTrace(this.errorStream);
    }
}
