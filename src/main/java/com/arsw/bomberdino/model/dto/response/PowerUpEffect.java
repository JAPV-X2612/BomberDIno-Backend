package com.arsw.bomberdino.model.dto.response;

import com.arsw.bomberdino.model.enums.PowerUpType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the effect of a collected power-up.
 * Used to communicate power-up impact to client for UI feedback.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PowerUpEffect {

    @NotNull(message = "Power-up type cannot be null")
    private PowerUpType type;

    @Min(value = 0, message = "Duration cannot be negative")
    private int duration;

    @Min(value = 0, message = "Multiplier cannot be negative")
    private float multiplier;
}
