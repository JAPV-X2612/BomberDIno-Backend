package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.Player;

import lombok.RequiredArgsConstructor;

import com.arsw.bomberdino.model.dto.response.PowerUpEffect;
import com.arsw.bomberdino.exception.PlayerNotFoundException;
import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing player state, movement, and lifecycle.
 * Handles player damage, respawning, and power-up effects.
 * Thread-safe for concurrent player actions.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * In-memory storage for players.
     * Key: playerId, Value: Player
     */
    private final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

    /**
     * Creates a new player with default attributes.
     *
     * @param playerId   unique identifier for the player
     * @param username   display name for the player
     * @param spawnPoint initial spawn position
     * @return newly created Player instance
     * @throws ValidationException   if parameters are null or blank
     * @throws IllegalStateException if player already exists
     */
    public Player createPlayer(String playerId, String username, Point spawnPoint) {
        validatePlayerId(playerId);
        validateUsername(username);
        validateSpawnPoint(spawnPoint);

        if (players.containsKey(playerId)) {
            throw new IllegalStateException("Player already exists: " + playerId);
        }

        Player player = Player.builder()
                .username(username)
                .posX(spawnPoint.x)
                .posY(spawnPoint.y)
                .lifeCount(3)
                .bombCount(1)
                .bombRange(2)
                .speed(1)
                .status(com.arsw.bomberdino.model.enums.PlayerStatus.ALIVE)
                .kills(0)
                .deaths(0)
                .spawnPoint(spawnPoint)
                .build();
        // player.initDefaults(); // TODO: Initialize default properties if needed

        players.put(playerId, player);

        return player;
    }

    /**
     * Moves a player to a new position.
     * Updates player coordinates after movement validation.
     *
     * @param playerId    unique identifier of the player
     * @param newPosition target position for movement
     * @return updated Player instance
     * @throws ValidationException     if playerId or newPosition is null
     * @throws PlayerNotFoundException if player not found
     * @throws IllegalStateException   if player not alive
     */
    public Player movePlayer(String playerId, Point newPosition) {
        validatePlayerId(playerId);
        validatePosition(newPosition);

        Player player = getPlayer(playerId);

        if (!player.getStatus().canPlay()) {
            throw new IllegalStateException("Player cannot move in current status: " + player.getStatus());
        }

        player.setPosX(newPosition.x);
        player.setPosY(newPosition.y);

        return player;
    }

    /**
     * Kills a player and records the killer.
     * Increments death counter and updates killer's kill count.
     *
     * @param killerId unique identifier of the killer player
     * @param victimId unique identifier of the victim player
     * @throws ValidationException     if killerId or victimId is null or blank
     * @throws PlayerNotFoundException if killer or victim not found
     */
    public void killPlayer(String killerId, String victimId) {
        validatePlayerId(killerId);
        validatePlayerId(victimId);

        Player killer = getPlayer(killerId);
        Player victim = getPlayer(victimId);

        victim.die();
        incrementKills(killerId);

    }

    /**
     * Respawns a player at their spawn point.
     * Resets player position and status to ALIVE.
     *
     * @param playerId unique identifier of the player
     * @return respawned Player instance
     * @throws ValidationException     if playerId is null or blank
     * @throws PlayerNotFoundException if player not found
     * @throws IllegalStateException   if player cannot respawn
     */
    public Player respawnPlayer(String playerId) {
        validatePlayerId(playerId);

        Player player = getPlayer(playerId);

        if (!player.isAlive()) {
            throw new IllegalStateException("Player has no lives remaining");
        }

        player.respawn();

        return player;
    }

    /**
     * Applies a power-up effect to a player.
     * Modifies player attributes based on PowerUpEffect.
     *
     * @param playerId unique identifier of the player
     * @param effect   PowerUpEffect to apply
     * @throws ValidationException     if playerId or effect is null
     * @throws PlayerNotFoundException if player not found
     */
    public void applyPowerUpEffect(String playerId, PowerUpEffect effect) {
        validatePlayerId(playerId);

        if (effect == null) {
            throw new ValidationException("PowerUpEffect cannot be null", "effect");
        }

        Player player = getPlayer(playerId);

        // TODO: Code already exists in Player.applyPowerUp/PowerUp.applyTo - refactor
        // to avoid duplication
        switch (effect.getType()) {
            case EXTRA_LIFE -> player.setLifeCount(player.getLifeCount() + 1);
            case SPEED_UP -> player.setSpeed(player.getSpeed() + 1);
            case BOMB_COUNT_UP -> player.setBombCount(player.getBombCount() + 1);
            case BOMB_RANGE_UP -> player.setBombRange(player.getBombRange() + 1);
        }

    }

    /**
     * Retrieves a player by ID.
     *
     * @param playerId unique identifier of the player
     * @return Player instance
     * @throws ValidationException     if playerId is null or blank
     * @throws PlayerNotFoundException if player not found
     */
    public Player getPlayer(String playerId) {
        validatePlayerId(playerId);

        Player player = players.get(playerId);

        if (player == null) {
            throw new PlayerNotFoundException(playerId);
        }

        return player;
    }

    /**
     * Checks if a player is alive.
     *
     * @param playerId unique identifier of the player
     * @return true if player has lives remaining
     * @throws ValidationException if playerId is null or blank
     */
    public boolean isAlive(String playerId) {
        validatePlayerId(playerId);

        Player player = players.get(playerId);

        if (player == null) {
            return false;
        }

        return player.isAlive();
    }

    /**
     * Increments the kill count for a player.
     *
     * @param playerId unique identifier of the player
     * @throws ValidationException if playerId is null or blank
     */
    public void incrementKills(String playerId) {
        validatePlayerId(playerId);

        Player player = getPlayer(playerId);
        player.setKills(player.getKills() + 1);

    }

    /**
     * Increments the death count for a player.
     *
     * @param playerId unique identifier of the player
     * @throws ValidationException if playerId is null or blank
     */
    public void incrementDeaths(String playerId) {
        validatePlayerId(playerId);

        Player player = getPlayer(playerId);
        player.setDeaths(player.getDeaths() + 1);

    }

    /**
     * Removes a player from the service.
     * Called when player leaves session.
     *
     * @param playerId unique identifier of the player
     * @throws ValidationException if playerId is null or blank
     */
    public void removePlayer(String playerId) {
        validatePlayerId(playerId);

        Player removed = players.remove(playerId);

        if (removed != null) {
        } else {
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
     * Validates username parameter.
     *
     * @param username username to validate
     * @throws ValidationException if username is null or blank
     */
    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be null or blank", "username");
        }
    }

    /**
     * Validates spawn point parameter.
     *
     * @param spawnPoint Point to validate
     * @throws ValidationException if spawnPoint is null
     */
    private void validateSpawnPoint(Point spawnPoint) {
        if (spawnPoint == null) {
            throw new ValidationException("Spawn point cannot be null", "spawnPoint");
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
}
