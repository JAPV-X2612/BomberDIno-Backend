package com.arsw.bomberdino.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for joining an existing game room. Validates player credentials and room
 * accessibility.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRoomRequestDTO {

    @NotBlank(message = "Room ID cannot be blank")
    private String roomId;

    @NotBlank(message = "Player ID cannot be blank")
    private String playerId;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Size(max = 50, message = "Password cannot exceed 50 characters")
    private String password;
}
