package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.BombState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Bomb entity placed by players on the game grid.
 * Explodes after fixed delay, creating cross-pattern explosion.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Bomb extends GameEntity {
    
    @Min(value = 1, message = "Range must be at least 1")
    private int range;
    
    @NotNull(message = "Bomb state cannot be null")
    private BombState state;
    
    @NotNull(message = "Placed time cannot be null")
    private Long placedTime;
    
    @Min(value = 1000, message = "Explosion delay must be at least 1000ms")
    private long explosionDelay;

    private static final int DEFAULT_EXPLOSION_DELAY = 3000;
    
    /**
     * Triggers bomb explosion.
     * Changes state to EXPLODING and creates Explosion entity.
     * 
     * @return Explosion instance representing blast area
     * @throws IllegalStateException if bomb is not in PLACED state
     */
    public Explosion explode() {
        if (state != BombState.PLACED) {
            throw new IllegalStateException("Bomb must be in PLACED state to explode");
        }
        
        this.state = BombState.EXPLODING;
        
        Explosion explosion = Explosion.builder()
                .posX(this.posX)
                .posY(this.posY)
                .damage(1)
                .duration(500L)
                .build();
        explosion.initDefaults();
        
        this.state = BombState.EXPLODED;
        
        return explosion;
    }
    
    /**
     * Checks if bomb countdown has finished.
     * @return true if current time >= placedTime + explosionDelay
     */
    public boolean isReadyToExplode() {
        return System.currentTimeMillis() >= (placedTime + explosionDelay);
    }
    
    /**
     * Gets remaining time until explosion in milliseconds.
     * @return milliseconds until explosion, 0 if already ready
     */
    public long getTimeUntilExplosion() {
        long remaining = (placedTime + explosionDelay) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
}
