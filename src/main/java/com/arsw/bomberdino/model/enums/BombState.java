package com.arsw.bomberdino.model.enums;

import lombok.Getter;

/**
 * Represents the lifecycle state of a bomb entity.
 * Controls animation, collision, and explosion triggering.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum BombState {
    
    PLACED("Placed", false),
    EXPLODING("Exploding", true),
    EXPLODED("Exploded", true);
    
    private final String displayName;
    private final boolean isDangerous;
    
    BombState(String displayName, boolean isDangerous) {
        this.displayName = displayName;
        this.isDangerous = isDangerous;
    }
    
    /**
     * Checks if bomb is in countdown phase before explosion.
     * @return true if bomb is placed but not yet exploding
     */
    public boolean isActive() {
        return this == PLACED;
    }
    
    /**
     * Checks if bomb is in explosion phase and can deal damage.
     * @return true if bomb is exploding or has exploded
     */
    public boolean canDealDamage() {
        return isDangerous;
    }
    
    /**
     * Checks if bomb should be removed from the game state.
     * @return true if bomb has finished exploding
     */
    public boolean shouldRemove() {
        return this == EXPLODED;
    }
}
