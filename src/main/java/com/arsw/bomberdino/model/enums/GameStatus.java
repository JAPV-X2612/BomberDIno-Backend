package com.arsw.bomberdino.model.enums;

import lombok.Getter;

/**
 * Represents the current status of a game session.
 * Controls the lifecycle and state transitions of active games.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum GameStatus {

    WAITING("Waiting for players"),
    STARTING("Game starting"),
    IN_PROGRESS("In progress"),
    PAUSED("Paused"),
    FINISHED("Finished");

    private final String displayName;

    GameStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Checks if the game is in an active playable state.
     * @return true if game is in progress or starting
     */
    public boolean isActive() {
        return this == IN_PROGRESS || this == STARTING;
    }

    /**
     * Checks if the game has concluded.
     * @return true if game is finished
     */
    public boolean isFinished() {
        return this == FINISHED;
    }

    /**
     * Checks if players can join the game.
     * @return true if game is waiting for players
     */
    public boolean canJoin() {
        return this == WAITING;
    }
}
