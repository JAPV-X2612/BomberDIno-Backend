package com.arsw.bomberdino.exception;

import com.arsw.bomberdino.model.enums.GameStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an action is invalid for the current game state.
 * Results in HTTP 409 CONFLICT response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class GameStateException extends RuntimeException {

    private final String sessionId;
    private final GameStatus currentStatus;
    private final String attemptedAction;

    public GameStateException(String sessionId, GameStatus currentStatus, String attemptedAction) {
        super("Cannot perform action '" + attemptedAction + "' in session " + sessionId +
                " with status " + currentStatus);
        this.sessionId = sessionId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public GameStateException(String sessionId, GameStatus currentStatus, String attemptedAction, String message) {
        super(message);
        this.sessionId = sessionId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public GameStateException(String sessionId, GameStatus currentStatus, String attemptedAction,
            String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }

    public String getSessionId() {
        return sessionId;
    }

    public GameStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}
