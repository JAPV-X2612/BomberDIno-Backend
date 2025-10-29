package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a game session is not found.
 * Results in HTTP 404 NOT_FOUND response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends RuntimeException {

    private final String sessionId;

    public SessionNotFoundException(String sessionId) {
        super("Session not found: " + sessionId);
        this.sessionId = sessionId;
    }

    public SessionNotFoundException(String sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
    }

    public SessionNotFoundException(String sessionId, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
