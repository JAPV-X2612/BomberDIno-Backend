package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a power-up is not found or has expired.
 * Results in HTTP 404 NOT_FOUND response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PowerUpNotFoundException extends RuntimeException {

    private final String powerUpId;
    private final String sessionId;

    public PowerUpNotFoundException(String powerUpId) {
        super("Power-up not found: " + powerUpId);
        this.powerUpId = powerUpId;
        this.sessionId = null;
    }

    public PowerUpNotFoundException(String powerUpId, String sessionId) {
        super("Power-up not found: " + powerUpId + " in session: " + sessionId);
        this.powerUpId = powerUpId;
        this.sessionId = sessionId;
    }

    public PowerUpNotFoundException(String powerUpId, String sessionId, String message) {
        super(message);
        this.powerUpId = powerUpId;
        this.sessionId = sessionId;
    }

    public PowerUpNotFoundException(String powerUpId, String sessionId, String message, Throwable cause) {
        super(message, cause);
        this.powerUpId = powerUpId;
        this.sessionId = sessionId;
    }

    public String getPowerUpId() {
        return powerUpId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
