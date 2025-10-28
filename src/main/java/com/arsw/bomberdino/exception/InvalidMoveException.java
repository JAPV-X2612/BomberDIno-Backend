package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.awt.Point;

/**
 * Exception thrown when a player attempts an invalid movement.
 * Results in HTTP 400 BAD_REQUEST response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMoveException extends RuntimeException {

    private final String playerId;
    private final Point targetPosition;
    private final String reason;

    public InvalidMoveException(String playerId, Point targetPosition, String reason) {
        super("Invalid move for player " + playerId + " to position (" +
                targetPosition.x + ", " + targetPosition.y + "): " + reason);
        this.playerId = playerId;
        this.targetPosition = targetPosition;
        this.reason = reason;
    }

    public InvalidMoveException(String playerId, Point targetPosition, String reason, Throwable cause) {
        super("Invalid move for player " + playerId + " to position (" +
                targetPosition.x + ", " + targetPosition.y + "): " + reason, cause);
        this.playerId = playerId;
        this.targetPosition = targetPosition;
        this.reason = reason;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Point getTargetPosition() {
        return targetPosition;
    }

    public String getReason() {
        return reason;
    }
}
