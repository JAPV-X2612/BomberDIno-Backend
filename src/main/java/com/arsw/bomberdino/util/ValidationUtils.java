package com.arsw.bomberdino.util;

import java.awt.Point;
import java.util.UUID;

/**
 * Validation utility for common parameter validation patterns. Provides reusable validation methods
 * to reduce code duplication.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that a string ID is not null or blank.
     *
     * @param id ID to validate
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if ID is null or blank
     */
    public static void requireNonBlank(String id, String fieldName) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }

    /**
     * Validates that an object is not null.
     *
     * @param object object to validate
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if object is null
     */
    public static void requireNonNull(Object object, String fieldName) {
        if (object == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a point is not null.
     *
     * @param point point to validate
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if point is null
     */
    public static void requireValidPoint(Point point, String fieldName) {
        if (point == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Validates that a number is positive.
     *
     * @param value value to validate
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if value is not positive
     */
    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Validates that a number is non-negative.
     *
     * @param value value to validate
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if value is negative
     */
    public static void requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validates that a number is within a range.
     *
     * @param value value to validate
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @param fieldName name of the field for error message
     * @throws IllegalArgumentException if value is out of range
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + min + " and " + max);
        }
    }

    /**
     * Validates that a string is a valid UUID.
     *
     * @param id string to validate
     * @param fieldName name of the field for error message
     * @return UUID instance
     * @throws IllegalArgumentException if string is not a valid UUID
     */
    public static UUID requireValidUUID(String id, String fieldName) {
        requireNonBlank(id, fieldName);

        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid UUID", e);
        }
    }

    /**
     * Validates that a point is within map bounds.
     *
     * @param point point to validate
     * @param width map width
     * @param height map height
     * @throws IllegalArgumentException if point is out of bounds
     */
    public static void requireInMapBounds(Point point, int width, int height) {
        requireValidPoint(point, "Position");

        if (point.x < 0 || point.x >= width || point.y < 0 || point.y >= height) {
            throw new IllegalArgumentException("Position (" + point.x + ", " + point.y
                    + ") is out of map bounds (" + width + "x" + height + ")");
        }
    }
}
