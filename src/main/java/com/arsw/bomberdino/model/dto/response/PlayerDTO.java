package com.arsw.bomberdino.model.dto.response;

import com.arsw.bomberdino.model.enums.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Player state DTO for real-time synchronization.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {
    
    @NotBlank(message = "Player ID cannot be blank")
    private String id;
    
    @NotBlank(message = "Username cannot be blank")
    private String username;
    
    @NotNull(message = "Position X cannot be null")
    @Min(value = 0, message = "Position X must be non-negative")
    private Integer posX;
    
    @NotNull(message = "Position Y cannot be null")
    @Min(value = 0, message = "Position Y must be non-negative")
    private Integer posY;
    
    @NotNull(message = "Life count cannot be null")
    @Min(value = 0, message = "Life count cannot be negative")
    private Integer lifeCount;
    
    @NotNull(message = "Status cannot be null")
    private PlayerStatus status;
    
    @Min(value = 0, message = "Kills cannot be negative")
    private int kills;
    
    @Min(value = 0, message = "Deaths cannot be negative")
    private int deaths;
    
    private boolean hasShield;
}
