package com.arsw.bomberdino.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new game room.
 * Used by REST API to initialize lobby before game session starts.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequestDTO {

    @NotBlank(message = "Room name cannot be blank")
    @Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    private String roomName;

    @Min(value = 2, message = "Maximum players must be at least 2")
    @Max(value = 8, message = "Maximum players cannot exceed 8")
    private int maxPlayers;

    private boolean isPrivate;

    @Size(max = 50, message = "Password cannot exceed 50 characters")
    private String password;

    @NotBlank(message = "Map ID cannot be blank")
    private String mapId;
}
