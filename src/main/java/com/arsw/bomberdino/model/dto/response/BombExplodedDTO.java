package com.arsw.bomberdino.model.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Event DTO broadcast when a bomb explodes.
 * Contains explosion range and affected entities for client-side effects.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BombExplodedDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Bomb ID cannot be blank")
    private String bombId;

    @NotEmpty(message = "Affected tiles cannot be empty")
    @Valid
    private List<PointDTO> affectedTiles;

    @NotNull(message = "Affected players list cannot be null")
    private List<String> affectedPlayers;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
