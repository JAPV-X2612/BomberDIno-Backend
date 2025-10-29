package com.arsw.bomberdino.exception;

/**
 * Exception thrown for validation errors.
 * Results in HTTP 400 BAD_REQUEST response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
public class ValidationException extends RuntimeException {
    private final String parameterName;

    /**
     * Constructor with message only.
     * @param parameterName name of the invalid parameter
     */
    public ValidationException(String parameterName) {
        super("Invalid parameter: " + parameterName);
        this.parameterName = parameterName;
    }

    /**
     * Constructor with message and parameter name.
     *
     * @param message       error message
     * @param parameterName name of the invalid parameter
     */
    public ValidationException(String message, String parameterName) {
        super(message);
        this.parameterName = parameterName;
    }

    /**
     * Constructor with message and cause.
     *
     * @param message error message
     * @param cause   root cause
     */
    public ValidationException(String message, String parameterName, Throwable cause) {
        super(message, cause);
        this.parameterName = parameterName;
    }

    /**
     * Gets the name of the invalid parameter, if provided.
     * @return parameter name or null
     */
    public String getParameterName() {
        return parameterName;
    }
}
