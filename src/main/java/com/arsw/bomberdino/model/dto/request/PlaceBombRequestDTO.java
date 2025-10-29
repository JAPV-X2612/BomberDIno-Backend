package com.arsw.bomberdino.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.Point;

/**
 * WebSocket request DTO for bomb placement actions.
 * Sent when player presses bomb placement key.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceBombRequestDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Player ID cannot be blank")
    private String playerId;

    @NotNull(message = "Position cannot be null")
    @Valid
    private Point position;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
