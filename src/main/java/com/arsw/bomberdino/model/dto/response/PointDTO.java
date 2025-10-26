package com.arsw.bomberdino.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for a Point object.
 * Represents a 2D point on the game grid.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointDTO {
    
    @NotNull(message = "X coordinate cannot be null")
    @Min(value = 0, message = "X coordinate must be non-negative")
    private Integer x;
    
    @NotNull(message = "Y coordinate cannot be null")
    @Min(value = 0, message = "Y coordinate must be non-negative")
    private Integer y;
}
