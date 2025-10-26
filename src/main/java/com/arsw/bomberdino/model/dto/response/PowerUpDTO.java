package com.arsw.bomberdino.model.dto.response;

import com.arsw.bomberdino.model.enums.PowerUpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for PowerUp entity.
 * Used for WebSocket spawn notifications and client-side rendering.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PowerUpDTO {
    
    /**
     * Unique identifier of the power-up (UUID format)
     */
    @NotBlank(message = "PowerUp ID cannot be blank")
    private String id;
    
    /**
     * Type of power-up effect
     */
    @NotNull(message = "PowerUp type cannot be null")
    private PowerUpType type;
    
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
}
