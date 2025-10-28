package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.PowerUp;
import com.arsw.bomberdino.model.dto.response.PowerUpEffect;
import com.arsw.bomberdino.model.enums.PowerUpType;

import lombok.RequiredArgsConstructor;

import com.arsw.bomberdino.exception.PowerUpNotFoundException;
import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for power-up spawning, collection, and effect application.
 * Handles power-up lifecycle and expiration scheduling.
 * Thread-safe for concurrent power-up operations.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class PowerUpService {

    private final GameMapService gameMapService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * In-memory storage for active power-ups.
     * Key: powerUpId, Value: PowerUp
     */
    private final ConcurrentHashMap<String, PowerUp> powerUps = new ConcurrentHashMap<>();

    /**
     * Executor service for scheduling power-up expiration.
     */
    private final ScheduledExecutorService expirationScheduler = Executors.newScheduledThreadPool(5);

    private static final long DEFAULT_POWERUP_DURATION = 30000L;

    /**
     * Spawns a power-up at the specified position.
     * Automatically schedules expiration after duration.
     *
     * @param sessionId unique identifier of the session
     * @param type      PowerUpType to spawn
     * @param position  coordinates where power-up spawns
     * @return PowerUp instance if spawn successful
     * @throws ValidationException if sessionId, type, or position is null
     */
    public PowerUp spawnPowerUp(String sessionId, PowerUpType type, Point position) {
        validateSessionId(sessionId);
        validatePowerUpType(type);
        validatePosition(position);

        PowerUp powerUp = PowerUp.builder()
                .posX(position.x)
                .posY(position.y)
                .type(type)
                .value(1)
                .spawnTime(System.currentTimeMillis())
                .duration(DEFAULT_POWERUP_DURATION)
                .build();
        // powerUp.initDefaults(); // TODO: Initialize defaults if needed

        String powerUpId = powerUp.getId().toString();
        powerUps.put(powerUpId, powerUp);

        scheduleExpiration(powerUpId, DEFAULT_POWERUP_DURATION);

        return powerUp;
    }

    /**
     * Applies power-up effect to a player and removes power-up.
     * Creates PowerUpEffect DTO with type-specific parameters.
     *
     * @param playerId  unique identifier of the player
     * @param powerUpId unique identifier of the power-up
     * @return PowerUpEffect describing the applied effect
     * @throws ValidationException      if playerId or powerUpId is null
     * @throws PowerUpNotFoundException if power-up not found
     * @throws IllegalStateException    if power-up expired
     */
    public PowerUpEffect applyPowerUpEffect(String playerId, String powerUpId) {
        validatePlayerId(playerId);
        validatePowerUpId(powerUpId);

        PowerUp powerUp = powerUps.get(powerUpId);

        if (powerUp == null) {
            throw new IllegalStateException("Power-up not found: " + powerUpId);
        }

        if (powerUp.isExpired()) {
            throw new IllegalStateException("Power-up has expired");
        }

        PowerUpEffect effect = createEffect(powerUp);

        removePowerUp(null, powerUpId);

        return effect;
    }

    /**
     * Removes a power-up from the session.
     * Called after collection or expiration.
     *
     * @param sessionId unique identifier of the session (can be null for global
     *                  removal)
     * @param powerUpId unique identifier of the power-up
     * @throws ValidationException if powerUpId is null or blank
     */
    public void removePowerUp(String sessionId, String powerUpId) {
        validatePowerUpId(powerUpId);

        PowerUp removed = powerUps.remove(powerUpId);

        if (removed != null) {
        } else {
        }
    }

    /**
     * Gets all active power-ups for a specific session.
     *
     * @param sessionId unique identifier of the session
     * @return list of PowerUp instances in the session
     * @throws ValidationException if sessionId is null or blank
     */
    public List<PowerUp> getActivePowerUps(String sessionId) {
        validateSessionId(sessionId);

        List<PowerUp> sessionPowerUps = powerUps.values().stream()
                .filter(powerUp -> !powerUp.isExpired())
                .collect(Collectors.toList());

        return sessionPowerUps;
    }

    /**
     * Schedules power-up expiration after specified timeout.
     * Removes power-up from map if not collected.
     *
     * @param powerUpId      unique identifier of the power-up
     * @param timeoutSeconds timeout in seconds until expiration
     * @throws ValidationException if powerUpId is null or timeout invalid
     */
    public void scheduleExpiration(String powerUpId, long timeoutSeconds) {
        validatePowerUpId(powerUpId);

        if (timeoutSeconds < 1) {
            throw new ValidationException("Timeout must be at least 1 second", "timeoutSeconds");
        }

        expirationScheduler.schedule(() -> {
            try {
                PowerUp powerUp = powerUps.get(powerUpId);
                if (powerUp != null && powerUp.isExpired()) {
                    removePowerUp(null, powerUpId);
                }
            } catch (Exception e) {
            }
        }, timeoutSeconds, TimeUnit.MILLISECONDS);

    }

    /**
     * Gets a random PowerUpType for spawning.
     * Uses PowerUpType.getRandomType() for equal probability distribution.
     *
     * @return randomly selected PowerUpType
     */
    public PowerUpType getRandomSpawnType() {
        PowerUpType type = PowerUpType.getRandomType();
        return type;
    }

    /**
     * Creates PowerUpEffect DTO from PowerUp entity.
     *
     * @param powerUp PowerUp to convert
     * @return PowerUpEffect with type-specific parameters
     */
    private PowerUpEffect createEffect(PowerUp powerUp) {
        int duration = powerUp.getType().isTemporary() ? (int) (powerUp.getDuration() / 1000) : 0;
        float multiplier = 1.0f;

        return PowerUpEffect.builder()
                .type(powerUp.getType())
                .duration(duration)
                .multiplier(multiplier)
                .build();
    }

    /**
     * Validates session ID parameter.
     *
     * @param sessionId session identifier to validate
     * @throws ValidationException if sessionId is null or blank
     */
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ValidationException("Session ID cannot be null or blank", "sessionId");
        }
    }

    /**
     * Validates player ID parameter.
     *
     * @param playerId player identifier to validate
     * @throws ValidationException if playerId is null or blank
     */
    private void validatePlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new ValidationException("Player ID cannot be null or blank", "playerId");
        }
    }

    /**
     * Validates power-up ID parameter.
     *
     * @param powerUpId power-up identifier to validate
     * @throws ValidationException if powerUpId is null or blank
     */
    private void validatePowerUpId(String powerUpId) {
        if (powerUpId == null || powerUpId.isBlank()) {
            throw new ValidationException("Power-up ID cannot be null or blank", "powerUpId");
        }
    }

    /**
     * Validates PowerUpType parameter.
     *
     * @param type PowerUpType to validate
     * @throws ValidationException if type is null
     */
    private void validatePowerUpType(PowerUpType type) {
        if (type == null) {
            throw new ValidationException("PowerUpType cannot be null", "type");
        }
    }

    /**
     * Validates position parameter.
     *
     * @param position Point to validate
     * @throws ValidationException if position is null
     */
    private void validatePosition(Point position) {
        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }
    }

    /**
     * Cleanup method to shutdown executor service gracefully.
     * Should be called during application shutdown.
     */
    public void shutdown() {
        expirationScheduler.shutdown();
        try {
            if (!expirationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                expirationScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            expirationScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
