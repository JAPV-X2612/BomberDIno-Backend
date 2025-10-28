package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.model.entity.Player;
import com.arsw.bomberdino.model.entity.PowerUp;
import com.arsw.bomberdino.model.entity.Tile;
import com.arsw.bomberdino.model.enums.TileType;
import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for collision detection and movement validation.
 * Handles tile walkability checks, power-up detection, and explosion range
 * calculation.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
public class CollisionService {

    private final TileService tileService;
    private final GameMapService gameMapService;

    public CollisionService(TileService tileService, GameMapService gameMapService) {
        this.tileService = tileService;
        this.gameMapService = gameMapService;
    }

    /**
     * Checks if a player can move to the specified position.
     * Validates tile walkability and occupation status.
     *
     * @param sessionId unique identifier of the session
     * @param position  target position to validate
     * @return true if movement is valid, false if blocked
     * @throws ValidationException if sessionId or position is null
     */
    public boolean canMoveTo(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        if (!isValidPosition(sessionId, position)) {
            return false;
        }

        TileType tileType = gameMapService.getTileType(sessionId, position);

        if (!tileType.isWalkable()) {
            return false;
        }

        if (tileService.isOccupied(sessionId, position)) {
            return false;
        }

        return true;
    }

    /**
     * Detects if there is a power-up at the specified position.
     * Used when player moves to check for collectible items.
     *
     * @param sessionId unique identifier of the session
     * @param position  position to check for power-ups
     * @return PowerUp instance if found, null otherwise
     * @throws ValidationException   if sessionId or position is null
     * @throws IllegalStateException if session not found
     */
    public PowerUp detectPowerUpCollision(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        Tile tile = tileService.getTile(sessionId, position);

        if (tile == null) {
            return null;
        }

        // Note: PowerUp detection logic depends on GameSession having access to
        // availablePowerUps
        // This would typically be handled in PowerUpService which maintains the powerUp
        // list

        return null; // Will be implemented by PowerUpService
    }

    /**
     * Handles bomb explosion and calculates affected tiles and players.
     * Propagates explosion in 4 cardinal directions until hitting solid walls.
     *
     * @param sessionId unique identifier of the session
     * @param bombId    unique identifier of the exploding bomb
     * @param range     explosion range in tiles
     * @return list of Point instances representing affected tiles
     * @throws ValidationException   if sessionId or bombId is null/blank, or range
     *                               invalid
     * @throws IllegalStateException if session not found
     */
    public List<Point> handleBombExplosion(String sessionId, String bombId, int range) {
        validateSessionId(sessionId);
        validateBombId(bombId);
        validateRange(range);

        // Note: Full implementation requires access to bomb position from BombService
        // This method signature matches the diagram but needs coordination with
        // BombService
        List<Point> affectedTiles = new ArrayList<>();

        return affectedTiles;
    }

    /**
     * Gets list of player IDs affected by an explosion.
     * Checks if any players occupy tiles in the explosion range.
     *
     * @param sessionId      unique identifier of the session
     * @param explosionTiles list of tiles affected by explosion
     * @return list of player IDs as Strings
     * @throws ValidationException   if sessionId or explosionTiles is null
     * @throws IllegalStateException if session not found
     */
    public List<String> getAffectedPlayers(String sessionId, List<Point> explosionTiles) {
        validateSessionId(sessionId);

        if (explosionTiles == null) {
            throw new ValidationException("Explosion tiles list cannot be null", "explosionTiles");
        }

        List<String> affectedPlayerIds = new ArrayList<>();

        // Note: This requires access to GameSession to iterate through players
        // Full implementation will be coordinated with GameSessionService

        return affectedPlayerIds;
    }

    /**
     * Validates if a position is within the map boundaries.
     *
     * @param sessionId unique identifier of the session
     * @param position  position to validate
     * @return true if position is within bounds
     * @throws ValidationException if sessionId or position is null
     */
    public boolean isValidPosition(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        GameMap map = gameMapService.getMap(sessionId);
        return map.isValidPosition(position.x, position.y);
    }

    /**
     * Validates session ID parameter.
     *
     * @param sessionId session identifier to validate
     * @throws IllegalArgumentException if sessionId is null or blank
     */
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ValidationException("Session ID cannot be null or blank", "sessionId");
        }
    }

    /**
     * Validates position parameter.
     *
     * @param position Point to validate
     * @throws IllegalArgumentException if position is null
     */
    private void validatePosition(Point position) {
        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }
    }

    /**
     * Validates bomb ID parameter.
     *
     * @param bombId bomb identifier to validate
     * @throws ValidationException if bombId is null or blank
     */
    private void validateBombId(String bombId) {
        if (bombId == null || bombId.isBlank()) {
            throw new ValidationException("Bomb ID cannot be null or blank", "bombId");
        }
    }

    /**
     * Validates explosion range parameter.
     *
     * @param range explosion range to validate
     * @throws ValidationException if range is less than 1
     */
    private void validateRange(int range) {
        if (range < 1) {
            throw new ValidationException("Explosion range must be at least 1", "range");
        }
    }
}
