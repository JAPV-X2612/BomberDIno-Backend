package com.arsw.bomberdino.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for Bomb entity.
 * Used for WebSocket real-time communication and REST API responses.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BombDTO {
    
    /**
     * Unique identifier of the bomb (UUID format)
     */
    @NotBlank(message = "Bomb ID cannot be blank")
    private String id;
    
    /**
     * ID of the player who placed the bomb
     */
    @NotBlank(message = "Owner ID cannot be blank")
    private String ownerId;
    
    /**
     * X coordinate position on the game grid
     */
    @NotNull(message = "Position X cannot be null")
    @Min(value = 0, message = "Position X must be non-negative")
    private Integer posX;
    
    /**
     * Y coordinate position on the game grid
     */
    @NotNull(message = "Position Y cannot be null")
    @Min(value = 0, message = "Position Y must be non-negative")
    private Integer posY;
    
    /**
     * Explosion range in tiles (affected radius)
     */
    @NotNull(message = "Range cannot be null")
    @Min(value = 1, message = "Range must be at least 1")
    private Integer range;
    
    /**
     * Timestamp when bomb will explode (epoch milliseconds)
     */
    @NotNull(message = "Time to explode cannot be null")
    @Min(value = 0, message = "Time to explode must be non-negative")
    private Long timeToExplode;
}
