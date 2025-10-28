package com.arsw.bomberdino.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when the game state changes.
 * Triggers WebSocket broadcast to synchronize all clients.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateChangedEvent {

    /**
     * Unique identifier of the game session where state changed.
     */
    private String sessionId;

    /**
     * Timestamp when the state change occurred.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a new GameStateChangedEvent with current timestamp.
     *
     * @param sessionId session identifier
     * @return GameStateChangedEvent instance
     */
    public static GameStateChangedEvent of(String sessionId) {
        return GameStateChangedEvent.builder()
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
