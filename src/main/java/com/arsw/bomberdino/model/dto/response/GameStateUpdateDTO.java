package com.arsw.bomberdino.model.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WebSocket broadcast DTO containing incremental game state updates.
 * Alternative to GameStateDTO for delta-based synchronization.
 * Currently using full state broadcast, but structure prepared for future
 * optimization.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateUpdateDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotNull(message = "Players list cannot be null")
    @Valid
    private List<PlayerDTO> players;

    @NotNull(message = "Bombs list cannot be null")
    @Valid
    private List<BombDTO> bombs;

    @NotNull(message = "Power-ups list cannot be null")
    @Valid
    private List<PowerUpDTO> powerUps;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
