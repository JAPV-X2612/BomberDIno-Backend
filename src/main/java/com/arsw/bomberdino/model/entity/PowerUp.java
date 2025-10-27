package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.PowerUpType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Power-up entity that enhances player capabilities.
 * Spawns randomly on empty tiles and expires after duration.
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
public class PowerUp extends GameEntity {
    
    @NotNull(message = "PowerUp type cannot be null")
    private PowerUpType type;
    
    @Min(value = 1, message = "PowerUp value must be positive")
    private int value;
    
    @NotNull(message = "Spawn time cannot be null")
    private Long spawnTime;
    
    @Min(value = 0, message = "Duration must be non-negative")
    private long duration;
    
    /**
     * Applies power-up effect to the target player.
     * Modifies player attributes based on PowerUpType.
     * 
     * @param player target player to receive effect
     * @throws IllegalArgumentException if player is null
     * @throws IllegalStateException if power-up is expired
     */
    public void applyTo(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot apply expired power-up");
        }
        
        switch (type) {
            case EXTRA_LIFE -> player.setLifeCount(player.getLifeCount() + value);
            case SPEED_UP -> player.setSpeed(player.getSpeed() + value);
            case BOMB_COUNT_UP -> player.setBombCount(player.getBombCount() + value);
            case BOMB_RANGE_UP -> player.setBombRange(player.getBombRange() + value);
            case TEMPORARY_SHIELD -> player.getActivePowerUps().add(this);
        }
    }
    
    /**
     * Checks if power-up has exceeded its lifetime.
     * @return true if current time exceeds spawnTime + duration
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > (spawnTime + duration);
    }
}
