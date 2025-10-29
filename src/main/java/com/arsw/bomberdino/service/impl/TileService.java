package com.arsw.bomberdino.service.impl;

import com.arsw.bomberdino.exception.SessionNotFoundException;
import com.arsw.bomberdino.exception.ValidationException;
import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.Tile;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe service for managing tile occupation state across game sessions.
 * Critical for preventing race conditions when multiple players attempt to occupy same tile.
 * Uses synchronized methods to ensure atomic occupation checks and updates.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class TileService {

    private final ConcurrentHashMap<String, ConcurrentHashMap<Point, Tile>> tiles = new ConcurrentHashMap<>();

    /**
     * Checks if a tile at the specified position is currently occupied.
     *
     * @param position coordinates to check
     * @return true if tile is occupied, false otherwise
     * @throws ValidationException if sessionId or position is null/blank
     */
    public boolean isOccupied(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            return false;
        }

        return tile.isOccupied();
    }

    /**
     * Attempts to occupy a tile at the specified position.
     * Thread-safe operation that prevents race conditions.
     *
     * @param position coordinates to occupy
     * @param hasBomb  true if occupation is due to bomb placement (allows player
     *                 on same tile)
     * @return true if occupation successful, false if tile already occupied or not
     *         walkable
     * @throws ValidationException if sessionId or position is null/blank
     */
    public synchronized boolean tryOccupy(String sessionId, Point position, boolean hasBomb) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            return false;
        }

        if (!tile.getType().isWalkable()) {
            return false;
        }

        if (tile.isOccupied() && !hasBomb) {
            return false;
        }

        boolean success = tile.setOccupied(true);

        if (success) {
        }

        return success;
    }

    /**
     * Releases occupation of a tile at the specified position.
     * Thread-safe operation for freeing tiles when entities move or are destroyed.
     *
     * @param position coordinates to release
     * @throws ValidationException if sessionId or position is null/blank
     */
    public synchronized void releaseOccupation(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            return;
        }

        tile.setOccupied(false);
    }

    /**
     * Retrieves the tile at the specified position.
     *
     * @param position coordinates of the tile
     * @return Tile instance at position, or null if not found
     * @throws ValidationException if sessionId or position is null/blank
     */
    public Tile getTile(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        return sessionTiles.get(position);
    }

    /**
     * Marks a tile as having a bomb placed on it.
     * Thread-safe operation for bomb placement tracking.
     *
     * @param position coordinates where bomb is placed
     * @param hasBomb  true to mark bomb, false to remove bomb marker
     * @throws ValidationException if sessionId or position is null/blank
     */
    public synchronized void markBomb(String sessionId, Point position, boolean hasBomb) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            return;
        }

        if (hasBomb) {
            tile.tryPlaceBomb();
        } else {
            tile.removeBomb();
        }
    }

    /**
     * Creates a Point-indexed map for fast tile lookups.
     *
     * @param map GameMap containing the tile matrix
     * @throws ValidationException if sessionId or map is null/blank
     */
    public void initializeTiles(String sessionId, GameMap map) {
        validateSessionId(sessionId);

        if (map == null) {
            throw new ValidationException("GameMap cannot be null", "map");
        }

        if (tiles.containsKey(sessionId)) {
            throw new IllegalStateException("Session tiles already initialized");
        }

        ConcurrentHashMap<Point, Tile> sessionTiles = new ConcurrentHashMap<>();
        Tile[][] tileMatrix = map.getTiles();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Tile tile = tileMatrix[y][x];
                sessionTiles.put(new Point(x, y), tile);
            }
        }

        tiles.put(sessionId, sessionTiles);
    }

    /**
     * Removes all tiles for a game session.
     * Called when session ends to free memory.
     *
     * @param sessionId unique identifier of the game session
     * @throws IllegalArgumentException if sessionId is null or blank
     */
    public void clearSession(String sessionId) {
        validateSessionId(sessionId);
        tiles.remove(sessionId);
    }

    /**
     * Retrieves the tile map for a specific session.
     *
     * @param sessionId session identifier
     * @return ConcurrentHashMap of tiles for the session
     * @throws IllegalStateException if session does not exist
     */
    private ConcurrentHashMap<Point, Tile> getSessionTiles(String sessionId) {
        ConcurrentHashMap<Point, Tile> sessionTiles = tiles.get(sessionId);

        if (sessionTiles == null) {
            throw new SessionNotFoundException(sessionId);
        }

        return sessionTiles;
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
     * @throws ValidationException if position is null
     */
    private void validatePosition(Point position) {
        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }
    }
}
