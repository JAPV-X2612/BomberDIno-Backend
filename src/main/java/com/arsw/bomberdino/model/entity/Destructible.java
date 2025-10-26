package com.arsw.bomberdino.model.entity;

/**
 * Interface for entities that can be damaged and destroyed.
 * Defines lifecycle for destructible game objects.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public interface Destructible {
    
    /**
     * Applies damage to the entity.
     * Reduces health or triggers destruction logic.
     * 
     * @param damage amount of damage to apply (must be positive)
     * @throws IllegalArgumentException if damage is negative
     */
    void takeDamage(int damage);
    
    /**
     * Checks if the entity has been destroyed.
     * @return true if entity is destroyed and should be removed, false otherwise
     */
    boolean isDestroyed();
    
    /**
     * Executes cleanup and destruction logic.
     * Called when entity is confirmed destroyed.
     * Handles resource cleanup, event broadcasting, and state updates.
     */
    void onDestroy();
}
