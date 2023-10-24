package com.botica.utils;

import org.apache.logging.log4j.Logger;

public class Utils {

    private Utils() {
    }

    public static void handleException(Logger logger, String message, Exception e) {
        logger.error(message);
        e.printStackTrace();
    }
}
