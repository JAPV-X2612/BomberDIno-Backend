package com.arsw.bomberdino.model.enums;

import lombok.Getter;

/**
 * Types of power-ups that can spawn on the game map.
 * Each type provides different temporary or permanent enhancements.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum PowerUpType {
    
    EXTRA_LIFE("Extra Life", 1, false),
    SPEED_UP("Speed Up", 2, false),
    BOMB_COUNT_UP("Bomb Count Up", 3, false),
    BOMB_RANGE_UP("Bomb Range Up", 4, false),
    TEMPORARY_SHIELD("Temporary Shield", 5, true);
    
    private final String displayName;
    private final int priority;
    private final boolean isTemporary;
    
    PowerUpType(String displayName, int priority, boolean isTemporary) {
        this.displayName = displayName;
        this.priority = priority;
        this.isTemporary = isTemporary;
    }
    
    /**
     * Gets a random PowerUpType with equal probability distribution.
     * 
     * @return randomly selected PowerUpType
     */
    public static PowerUpType getRandomType() {
        PowerUpType[] values = values();
        int randomIndex = (int) (Math.random() * values.length);
        return values[randomIndex];
    }
    
    /**
     * Checks if this power-up effect expires after a duration.
     * @return true if power-up is temporary
     */
    public boolean isTemporary() {
        return isTemporary;
    }
}
