package com.arsw.bomberdino.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to join a full game room.
 * Results in HTTP 409 CONFLICT response.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RoomFullException extends RuntimeException {

    private final String roomId;
    private final int currentPlayers;
    private final int maxPlayers;

    public RoomFullException(String roomId, int currentPlayers, int maxPlayers) {
        super("Room " + roomId + " is full (" + currentPlayers + "/" + maxPlayers + ")");
        this.roomId = roomId;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

    public RoomFullException(String roomId, int currentPlayers, int maxPlayers, String message) {
        super(message);
        this.roomId = roomId;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

    public RoomFullException(String roomId, int currentPlayers, int maxPlayers, String message, Throwable cause) {
        super(message, cause);
        this.roomId = roomId;
        this.currentPlayers = currentPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getRoomId() {
        return roomId;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
