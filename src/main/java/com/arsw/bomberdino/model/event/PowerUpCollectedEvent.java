package com.arsw.bomberdino.model.event;

import com.arsw.bomberdino.model.dto.response.PowerUpEffect;
import com.arsw.bomberdino.model.enums.PowerUpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain event published when a player collects a power-up.
 * Used for UI notifications and effect application confirmation.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PowerUpCollectedEvent {

    /**
     * Unique identifier of the game session where collection occurred.
     */
    private String sessionId;

    /**
     * Unique identifier of the player who collected the power-up.
     */
    private String playerId;

    /**
     * Unique identifier of the collected power-up.
     */
    private String powerUpId;

    /**
     * Effect applied to the player.
     */
    private PowerUpEffect effect;

    /**
     * Timestamp when the collection occurred.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Gets the type of power-up collected.
     *
     * @return PowerUpType from the effect
     */
    public PowerUpType getPowerUpType() {
        return effect != null ? effect.getType() : null;
    }

    /**
     * Checks if the power-up effect is temporary.
     *
     * @return true if effect has duration > 0
     */
    public boolean isTemporary() {
        return effect != null && effect.getDuration() > 0;
    }

    /**
     * Creates a new PowerUpCollectedEvent with current timestamp.
     *
     * @param sessionId session identifier
     * @param playerId  player identifier
     * @param powerUpId power-up identifier
     * @param effect    power-up effect applied
     * @return PowerUpCollectedEvent instance
     */
    public static PowerUpCollectedEvent of(String sessionId, String playerId,
            String powerUpId, PowerUpEffect effect) {
        return PowerUpCollectedEvent.builder()
                .sessionId(sessionId)
                .playerId(playerId)
                .powerUpId(powerUpId)
                .effect(effect)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
