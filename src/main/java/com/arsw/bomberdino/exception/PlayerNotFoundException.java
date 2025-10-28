package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a player is not found in a session.
 * Results in HTTP 404 NOT_FOUND response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlayerNotFoundException extends RuntimeException {

    private final String playerId;
    private final String sessionId;

    public PlayerNotFoundException(String playerId) {
        super("Player not found: " + playerId);
        this.playerId = playerId;
        this.sessionId = null;
    }

    public PlayerNotFoundException(String playerId, String sessionId) {
        super("Player not found: " + playerId + " in session: " + sessionId);
        this.playerId = playerId;
        this.sessionId = sessionId;
    }

    public PlayerNotFoundException(String playerId, String sessionId, String message) {
        super(message);
        this.playerId = playerId;
        this.sessionId = sessionId;
    }

    public PlayerNotFoundException(String playerId, String sessionId, String message, Throwable cause) {
        super(message, cause);
        this.playerId = playerId;
        this.sessionId = sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
