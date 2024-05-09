package es.us.lsi.botica.utils.logging;

import javax.management.RuntimeErrorException;

import org.apache.logging.log4j.Logger;

public class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * Handles an exception by logging an error message and printing the exception
     * stack trace.
     *
     * @param logger  The logger to use for logging the error message.
     * @param message The error message to log.
     * @param e       The exception to handle.
     */
    public static void handleException(Logger logger, String message, Exception e) {
        logger.error(message, e);
        throw new RuntimeErrorException(new Error(message, e));
    }

    public static void throwRuntimeErrorException(String message, Exception e) {
        throw new RuntimeErrorException(new Error(message, e));
    }
}
