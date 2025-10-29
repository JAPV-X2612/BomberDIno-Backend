package com.arsw.bomberdino.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket request DTO for power-up collection.
 * Sent when player collides with power-up tile.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PowerUpCollectRequestDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Player ID cannot be blank")
    private String playerId;

    @NotBlank(message = "Power-up ID cannot be blank")
    private String powerUpId;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
