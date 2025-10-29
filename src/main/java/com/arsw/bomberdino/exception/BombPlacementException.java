package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.awt.Point;

/**
 * Exception thrown when bomb placement fails.
 * Results in HTTP 400 BAD_REQUEST response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BombPlacementException extends RuntimeException {

    private final String playerId;
    private final Point position;
    private final String reason;

    public BombPlacementException(String playerId, Point position, String reason) {
        super("Cannot place bomb for player " + playerId + " at position (" +
                position.x + ", " + position.y + "): " + reason);
        this.playerId = playerId;
        this.position = position;
        this.reason = reason;
    }

    public BombPlacementException(String playerId, Point position, String reason, Throwable cause) {
        super("Cannot place bomb for player " + playerId + " at position (" +
                position.x + ", " + position.y + "): " + reason, cause);
        this.playerId = playerId;
        this.position = position;
        this.reason = reason;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Point getPosition() {
        return position;
    }

    public String getReason() {
        return reason;
    }
}
