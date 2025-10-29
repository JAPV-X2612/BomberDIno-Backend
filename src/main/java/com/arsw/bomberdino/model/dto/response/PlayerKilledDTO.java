package com.arsw.bomberdino.model.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event DTO broadcast when a player is killed.
 * Used for kill notifications and scoreboard updates.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerKilledDTO {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    @NotBlank(message = "Killer ID cannot be blank")
    private String killerId;

    @NotBlank(message = "Victim ID cannot be blank")
    private String victimId;

    @NotNull(message = "Timestamp cannot be null")
    private Long timestamp;
}
