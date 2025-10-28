package com.arsw.bomberdino.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a player is killed.
 * Used for kill notifications, scoreboard updates, and respawn logic.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerKilledEvent {

    /**
     * Unique identifier of the game session where kill occurred.
     */
    private String sessionId;

    /**
     * Unique identifier of the player who killed (nullable if suicide/environment).
     */
    private String killerId;

    /**
     * Unique identifier of the player who was killed.
     */
    private String victimId;

    /**
     * Timestamp when the kill occurred.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Checks if the kill was a suicide (player killed themselves).
     *
     * @return true if killerId equals victimId or killerId is null
     */
    public boolean isSuicide() {
        return killerId == null || killerId.equals(victimId);
    }

    /**
     * Creates a new PlayerKilledEvent with current timestamp.
     *
     * @param sessionId session identifier
     * @param killerId  killer player identifier (nullable)
     * @param victimId  victim player identifier
     * @return PlayerKilledEvent instance
     */
    public static PlayerKilledEvent of(String sessionId, String killerId, String victimId) {
        return PlayerKilledEvent.builder()
                .sessionId(sessionId)
                .killerId(killerId)
                .victimId(victimId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
