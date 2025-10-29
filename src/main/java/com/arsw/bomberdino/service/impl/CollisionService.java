package com.arsw.bomberdino.service.impl;

import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.model.entity.PowerUp;
import com.arsw.bomberdino.model.entity.Tile;
import com.arsw.bomberdino.model.enums.TileType;

import lombok.RequiredArgsConstructor;

import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for collision detection and movement validation. Handles tile walkability checks,
 * power-up detection, and explosion range calculation.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class CollisionService {

    private final TileService tileService;
    private final GameMapService gameMapService;
    private final GameSessionService gameSessionService;

    /**
     * Checks if a player can move to the specified position. Validates tile walkability and
     * occupation status.
     *
     * @param sessionId unique identifier of the session
     * @param position target position to validate
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

        return !tileService.isOccupied(sessionId, position);
    }

    /**
     * Detects if there is a power-up at the specified position. Used when player moves to check for
     * collectible items.
     *
     * @param sessionId unique identifier of the session
     * @param position position to check for power-ups
     * @return PowerUp instance if found, null otherwise
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException if session not found
     */
    public PowerUp detectPowerUpCollision(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        GameSession session = gameSessionService.getSession(sessionId);

        return session.getAvailablePowerUps().stream()
                .filter(powerUp -> powerUp.getPosX() == position.x
                        && powerUp.getPosY() == position.y)
                .filter(powerUp -> !powerUp.isExpired()).findFirst().orElse(null);
    }

    /**
     * Handles bomb explosion and calculates affected tiles and players. Propagates explosion in 4
     * cardinal directions until hitting solid walls.
     *
     * @param sessionId unique identifier of the session
     * @param bombId unique identifier of the exploding bomb
     * @param range explosion range in tiles
     * @return list of Point instances representing affected tiles
     * @throws IllegalArgumentException if sessionId or bombId is null, or range invalid
     * @throws IllegalStateException if session not found
     */
    public List<Point> handleBombExplosion(String sessionId, String bombId, int range) {
        validateSessionId(sessionId);
        validateBombId(bombId);
        validateRange(range);

        GameMap map = gameMapService.getMap(sessionId);
        List<Point> affectedTiles = new ArrayList<>();

        Point bombPosition = getBombPosition(sessionId, bombId);
        if (bombPosition == null) {
            return affectedTiles;
        }

        affectedTiles.add(new Point(bombPosition.x, bombPosition.y));

        affectedTiles.addAll(propagateExplosion(map, bombPosition, 0, -1, range)); // UP
        affectedTiles.addAll(propagateExplosion(map, bombPosition, 0, 1, range)); // DOWN
        affectedTiles.addAll(propagateExplosion(map, bombPosition, -1, 0, range)); // LEFT
        affectedTiles.addAll(propagateExplosion(map, bombPosition, 1, 0, range)); // RIGHT

        return affectedTiles;
    }

    /**
     * Propagates explosion in a specific direction until hitting obstacle.
     *
     * @param map game map
     * @param origin explosion origin point
     * @param dx X direction delta (-1, 0, 1)
     * @param dy Y direction delta (-1, 0, 1)
     * @param maxRange maximum tiles to propagate
     * @return list of affected tiles in this direction
     */
    private List<Point> propagateExplosion(GameMap map, Point origin, int dx, int dy,
            int maxRange) {
        List<Point> tiles = new ArrayList<>();

        for (int i = 1; i <= maxRange; i++) {
            int newX = origin.x + (dx * i);
            int newY = origin.y + (dy * i);

            if (!map.isValidPosition(newX, newY)) {
                break;
            }

            Point currentTile = new Point(newX, newY);
            Tile tile = map.getTile(newX, newY);

            if (tile == null) {
                break;
            }

            tiles.add(currentTile);
            TileType tileType = tile.getType();

            if (tileType == TileType.SOLID_WALL) {
                break;
            }

            if (tileType == TileType.DESTRUCTIBLE_WALL) {
                break;
            }
        }

        return tiles;
    }

    /**
     * Gets bomb position from session.
     *
     * @param sessionId session identifier
     * @param bombId bomb identifier
     * @return Point with bomb coordinates, or null if not found
     */
    private Point getBombPosition(String sessionId, String bombId) {
        GameSession session = gameSessionService.getSession(sessionId);

        return session.getActiveBombs().stream()
                .filter(bomb -> bomb.getId().toString().equals(bombId)).findFirst()
                .map(bomb -> new Point(bomb.getPosX(), bomb.getPosY())).orElse(null);
    }

    /**
     * Validates if a position is within the map boundaries.
     *
     * @param sessionId unique identifier of the session
     * @param position position to validate
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
