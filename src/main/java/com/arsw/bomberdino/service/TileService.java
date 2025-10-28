package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe service for managing tile occupation state across game sessions.
 * Critical for preventing race conditions when multiple players attempt to
 * occupy same tile.
 * Uses synchronized methods to ensure atomic occupation checks and updates.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
public class TileService {

    private static final Logger logger = LoggerFactory.getLogger(TileService.class);

    /**
     * Thread-safe storage for tile grids per session.
     * Key: sessionId, Value: Map of Point -> Tile
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<Point, Tile>> tiles = new ConcurrentHashMap<>();

    /**
     * Checks if a tile at the specified position is currently occupied.
     *
     * @param sessionId unique identifier of the game session
     * @param position  coordinates to check
     * @return true if tile is occupied, false otherwise
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException    if session does not exist
     */
    public boolean isOccupied(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            logger.warn("Tile not found at position ({}, {}) in session {}", position.x, position.y, sessionId);
            return false;
        }

        return tile.isOccupied();
    }

    /**
     * Attempts to occupy a tile at the specified position.
     * Thread-safe operation that prevents race conditions.
     *
     * @param sessionId unique identifier of the game session
     * @param position  coordinates to occupy
     * @param hasBomb   true if occupation is due to bomb placement (allows player
     *                  on same tile)
     * @return true if occupation successful, false if tile already occupied or not
     *         walkable
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException    if session does not exist
     */
    public synchronized boolean tryOccupy(String sessionId, Point position, boolean hasBomb) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            logger.error("Tile not found at position ({}, {}) in session {}", position.x, position.y, sessionId);
            return false;
        }

        if (!tile.getType().isWalkable()) {
            logger.debug("Tile at ({}, {}) is not walkable in session {}", position.x, position.y, sessionId);
            return false;
        }

        if (tile.isOccupied() && !hasBomb) {
            logger.debug("Tile at ({}, {}) is already occupied in session {}", position.x, position.y, sessionId);
            return false;
        }

        boolean success = tile.setOccupied(true);

        if (success) {
            logger.info("Tile at ({}, {}) occupied successfully in session {} (hasBomb: {})",
                    position.x, position.y, sessionId, hasBomb);
        }

        return success;
    }

    /**
     * Releases occupation of a tile at the specified position.
     * Thread-safe operation for freeing tiles when entities move or are destroyed.
     *
     * @param sessionId unique identifier of the game session
     * @param position  coordinates to release
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException    if session does not exist
     */
    public synchronized void releaseOccupation(String sessionId, Point position) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            logger.warn("Tile not found at position ({}, {}) in session {} during release",
                    position.x, position.y, sessionId);
            return;
        }

        tile.setOccupied(false);
        logger.info("Tile at ({}, {}) released in session {}", position.x, position.y, sessionId);
    }

    /**
     * Retrieves the tile at the specified position.
     *
     * @param sessionId unique identifier of the game session
     * @param position  coordinates of the tile
     * @return Tile instance at position, or null if not found
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException    if session does not exist
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
     * @param sessionId unique identifier of the game session
     * @param position  coordinates where bomb is placed
     * @param hasBomb   true to mark bomb, false to remove bomb marker
     * @throws IllegalArgumentException if sessionId or position is null
     * @throws IllegalStateException    if session does not exist
     */
    public synchronized void markBomb(String sessionId, Point position, boolean hasBomb) {
        validateSessionId(sessionId);
        validatePosition(position);

        ConcurrentHashMap<Point, Tile> sessionTiles = getSessionTiles(sessionId);
        Tile tile = sessionTiles.get(position);

        if (tile == null) {
            logger.error("Tile not found at position ({}, {}) in session {} during bomb marking",
                    position.x, position.y, sessionId);
            return;
        }

        if (hasBomb) {
            tile.tryPlaceBomb();
            logger.info("Bomb marked at tile ({}, {}) in session {}", position.x, position.y, sessionId);
        } else {
            tile.removeBomb();
            logger.info("Bomb removed from tile ({}, {}) in session {}", position.x, position.y, sessionId);
        }
    }

    /**
     * Initializes tile grid for a new game session from a GameMap.
     * Creates a Point-indexed map for fast tile lookups.
     *
     * @param sessionId unique identifier of the game session
     * @param map       GameMap containing the tile matrix
     * @throws IllegalArgumentException if sessionId or map is null
     * @throws IllegalStateException    if session already has initialized tiles
     */
    public void initializeTiles(String sessionId, GameMap map) {
        validateSessionId(sessionId);

        if (map == null) {
            throw new IllegalArgumentException("GameMap cannot be null");
        }

        if (tiles.containsKey(sessionId)) {
            logger.warn("Tiles already initialized for session {}", sessionId);
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
        logger.info("Initialized {} tiles for session {}", sessionTiles.size(), sessionId);
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

        ConcurrentHashMap<Point, Tile> removed = tiles.remove(sessionId);

        if (removed != null) {
            logger.info("Cleared {} tiles for session {}", removed.size(), sessionId);
        } else {
            logger.warn("No tiles found to clear for session {}", sessionId);
        }
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
            throw new IllegalStateException("Session not found: " + sessionId);
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
            throw new IllegalArgumentException("Session ID cannot be null or blank");
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
            throw new IllegalArgumentException("Position cannot be null");
        }
    }
}
