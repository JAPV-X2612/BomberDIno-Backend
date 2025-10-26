package com.arsw.bomberdino.model.dto.response;

import com.arsw.bomberdino.model.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * WebSocket broadcast DTO containing complete game state snapshot.
 * Sent every frame (60 FPS) to synchronize all clients.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateDTO {
    
    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;
    
    @NotNull(message = "Game status cannot be null")
    private GameStatus status;
    
    @NotNull(message = "Players list cannot be null")
    @Valid
    private List<PlayerDTO> players;
    
    @NotNull(message = "Bombs list cannot be null")
    @Valid
    private List<BombDTO> bombs;
    
    @NotNull(message = "Explosions list cannot be null")
    @Valid
    private List<ExplosionDTO> explosions;
    
    @NotNull(message = "Power-ups list cannot be null")
    @Valid
    private List<PowerUpDTO> powerUps;
    
    @NotNull(message = "Server time cannot be null")
    private Long serverTime;
}
