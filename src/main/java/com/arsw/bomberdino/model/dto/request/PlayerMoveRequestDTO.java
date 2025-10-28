package com.arsw.bomberdino.model.dto.request;

import com.arsw.bomberdino.model.enums.Direction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket request DTO for player movement actions.
 * Sent every time player presses movement keys.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerMoveRequestDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Player ID cannot be blank")
    private String playerId;

    @NotNull(message = "Direction cannot be null")
    private Direction direction;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
