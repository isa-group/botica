package com.botica.utils;

import org.apache.logging.log4j.Logger;

public class Utils {

    private Utils() {
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
        logger.error(message);
        e.printStackTrace();
    }
}
