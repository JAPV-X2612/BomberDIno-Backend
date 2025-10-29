package com.arsw.bomberdino.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract base class for all game entities with spatial properties.
 * Provides common attributes for position tracking and collision detection.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class GameEntity {

    /**
     * Unique identifier for the entity
     */
    protected UUID id;

    /**
     * X coordinate position on the game grid
     */
    @NotNull(message = "Position X cannot be null")
    @Min(value = 0, message = "Position X must be non-negative")
    protected Integer posX;

    /**
     * Y coordinate position on the game grid
     */
    @NotNull(message = "Position Y cannot be null")
    @Min(value = 0, message = "Position Y must be non-negative")
    protected Integer posY;

    /**
     * Timestamp when the entity was created
     */
    @NotNull(message = "Creation timestamp cannot be null")
    protected LocalDateTime createdAt;

    /**
     * Initializes entity with generated UUID and current timestamp.
     */
    public void initDefaults() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this entity collides with another game entity.
     * Collision occurs when both entities occupy the same grid position.
     *
     * @param entity the other GameEntity to check collision with
     * @return true if entities occupy the same position, false otherwise
     * @throws IllegalArgumentException if entity parameter is null
     */
    public boolean collidesWith(GameEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity to check collision cannot be null");
        }
        return this.posX.equals(entity.posX) && this.posY.equals(entity.posY);
    }

    /**
     * Calculates the Manhattan distance to another entity.
     * Useful for proximity checks and AI pathfinding.
     *
     * @param entity the target entity
     * @return Manhattan distance (|x1-x2| + |y1-y2|)
     * @throws IllegalArgumentException if entity parameter is null
     */
    public int distanceTo(GameEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Target entity cannot be null");
        }
        return Math.abs(this.posX - entity.posX) + Math.abs(this.posY - entity.posY);
    }

    /**
     * Updates the position of this entity.
     *
     * @param newPosX new X coordinate
     * @param newPosY new Y coordinate
     * @throws IllegalArgumentException if coordinates are negative
     */
    public void updatePosition(int newPosX, int newPosY) {
        if (newPosX < 0 || newPosY < 0) {
            throw new IllegalArgumentException("Position coordinates must be non-negative");
        }
        this.posX = newPosX;
        this.posY = newPosY;
    }
}
