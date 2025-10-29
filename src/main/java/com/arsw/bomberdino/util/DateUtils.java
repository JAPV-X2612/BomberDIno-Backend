package com.arsw.bomberdino.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for date and time operations. Provides formatting and duration calculation methods.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Formats LocalDateTime to default pattern.
     *
     * @param dateTime LocalDateTime to format
     * @return formatted string (yyyy-MM-dd HH:mm:ss)
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Calculates seconds between two timestamps.
     *
     * @param start start timestamp
     * @param end end timestamp
     * @return seconds elapsed
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).getSeconds();
    }

    /**
     * Converts milliseconds to seconds.
     *
     * @param milliseconds milliseconds value
     * @return seconds value
     */
    public static long millisToSeconds(long milliseconds) {
        return milliseconds / 1000;
    }

    /**
     * Converts seconds to milliseconds.
     *
     * @param seconds seconds value
     * @return milliseconds value
     */
    public static long secondsToMillis(long seconds) {
        return seconds * 1000;
    }

    /**
     * Checks if a timestamp has expired based on duration.
     *
     * @param startTime start timestamp in milliseconds
     * @param duration duration in milliseconds
     * @return true if current time exceeds startTime + duration
     */
    public static boolean hasExpired(long startTime, long duration) {
        return System.currentTimeMillis() >= (startTime + duration);
    }

    /**
     * Calculates remaining time until expiration.
     *
     * @param startTime start timestamp in milliseconds
     * @param duration duration in milliseconds
     * @return remaining milliseconds, or 0 if expired
     */
    public static long remainingTime(long startTime, long duration) {
        long expirationTime = startTime + duration;
        long remaining = expirationTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
