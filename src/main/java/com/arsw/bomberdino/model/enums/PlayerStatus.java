package com.arsw.bomberdino.model.enums;

import lombok.Getter;

/**
 * Represents the current status of a player within a game session.
 * Determines player's interaction capabilities and rendering state.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum PlayerStatus {
    
    ALIVE("Alive"),
    DEAD("Dead"),
    SPECTATING("Spectating"),
    DISCONNECTED("Disconnected");
    
    private final String displayName;
    
    PlayerStatus(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Checks if player can perform actions in the game.
     * @return true if player is alive
     */
    public boolean canPlay() {
        return this == ALIVE;
    }
    
    /**
     * Checks if player can respawn after death.
     * @return true if player is dead (not spectating or disconnected)
     */
    public boolean canRespawn() {
        return this == DEAD;
    }
    
    /**
     * Checks if player is still connected to the session.
     * @return true if player is not disconnected
     */
    public boolean isConnected() {
        return this != DISCONNECTED;
    }
}
