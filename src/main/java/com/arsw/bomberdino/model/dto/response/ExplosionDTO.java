package com.arsw.bomberdino.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Data Transfer Object for Explosion event.
 * Represents the visual and damage information of a bomb explosion.
 * Transient object used only for real-time WebSocket broadcasting.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplosionDTO {
    
    @NotBlank(message = "Explosion ID cannot be blank")
    private String id;
    
    @NotEmpty(message = "Affected tiles list cannot be empty")
    @Valid
    private List<PointDTO> tiles;
    
    @NotNull(message = "Duration cannot be null")
    @Min(value = 100, message = "Duration must be at least 100ms")
    private Long duration;   
}
