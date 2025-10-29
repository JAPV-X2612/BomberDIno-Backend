package com.arsw.bomberdino.model.dto.response;

import java.util.List;

import com.arsw.bomberdino.model.enums.GameStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for game room information.
 * Used for lobby listing and room details display.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoomDTO {

    @NotBlank(message = "Room ID cannot be blank")
    private String roomId;

    @NotBlank(message = "Room name cannot be blank")
    private String roomName;

    @NotBlank(message = "Room name cannot be blank")
    private String roomCode;

    @NotNull(message = "Status cannot be null")
    private GameStatus status;

    @NotNull(message = "Players list cannot be null")
    @Valid
    private List<PlayerDTO> currentPlayers;

    @Min(value = 2, message = "Max players must be at least 2")
    @Max(value = 8, message = "Max players cannot exceed 8")
    private int maxPlayers;

    private boolean isPrivate;
}
