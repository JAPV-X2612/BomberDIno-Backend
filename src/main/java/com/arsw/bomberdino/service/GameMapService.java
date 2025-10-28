package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.Tile;
import com.arsw.bomberdino.model.enums.TileType;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.arsw.bomberdino.exception.ValidationException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for creating and managing game maps.
 * Generates procedural maps with spawn points and destructible walls.
 * Stores maps in-memory for fast retrieval during gameplay.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class GameMapService {

    private static final Random random = new Random();

    /**
     * In-memory storage for game maps.
     * Key: sessionId, Value: GameMap
     */
    private final ConcurrentHashMap<String, GameMap> maps = new ConcurrentHashMap<>();

    private static final int DEFAULT_WIDTH = 12;
    private static final int DEFAULT_HEIGHT = 12;
    private static final double DESTRUCTIBLE_WALL_PROBABILITY = 0.4;

    /**
     * Creates a new game map for the specified session.
     * Generates a grid with solid walls, destructible walls, and spawn points.
     *
     * @param sessionId unique identifier of the game session
     * @param width     map width in tiles
     * @param height    map height in tiles
     * @return newly created GameMap instance
     * @throws ValidationException   if sessionId is null/blank, or dimensions
     *                               invalid
     * @throws IllegalStateException if map already exists for session
     */
    public GameMap createMap(String sessionId, int width, int height) {
        validateSessionId(sessionId);
        validateDimensions(width, height);

        if (maps.containsKey(sessionId)) {
            throw new IllegalStateException("Map already exists for session: " + sessionId);
        }

        Tile[][] tiles = generateTileMatrix(width, height);
        List<Point> spawnPoints = generateSpawnPoints(width, height);

        GameMap map = GameMap.builder()
                .mapId(UUID.randomUUID())
                .name("Map_" + sessionId)
                .width(width)
                .height(height)
                .tiles(tiles)
                .spawnPoints(spawnPoints)
                .build();

        maps.put(sessionId, map);

        return map;
    }

    /**
     * Retrieves the game map for the specified session.
     *
     * @param sessionId unique identifier of the game session
     * @return GameMap instance for the session
     * @throws ValidationException   if sessionId is null or blank
     * @throws IllegalStateException if map does not exist for session
     */
    public GameMap getMap(String sessionId) {
        validateSessionId(sessionId);

        GameMap map = maps.get(sessionId);

        if (map == null) {
            throw new IllegalStateException("Map not found for session: " + sessionId);
        }

        return map;
    }

    /**
     * Gets all valid spawn points for a session that are currently unoccupied.
     *
     * @param sessionId unique identifier of the game session
     * @return list of available spawn Point instances
     * @throws IllegalArgumentException if sessionId is null or blank
     * @throws IllegalStateException    if map does not exist for session
     */
    public List<Point> getValidSpawnPoints(String sessionId) {
        validateSessionId(sessionId);

        GameMap map = getMap(sessionId);
        return map.getAvailableSpawnPoints();
    }

    /**
     * Validates if a position is within map boundaries and of correct type.
     *
     * @param position coordinates to validate
     * @param width    map width
     * @param height   map height
     * @return true if position is valid
     * @throws ValidationException if position is null
     */
    public boolean isPositionValid(Point position, int width, int height) {
        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }

        return position.x >= 0 && position.x < width && position.y >= 0 && position.y < height;
    }

    /**
     * Gets the tile type at the specified position in a session's map.
     *
     * @param sessionId unique identifier of the game session
     * @param position  coordinates of the tile
     * @return TileType at the position
     * @throws ValidationException   if sessionId or position is null
     * @throws IllegalStateException if map or tile does not exist
     */
    public TileType getTileType(String sessionId, Point position) {
        validateSessionId(sessionId);

        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }

        GameMap map = getMap(sessionId);
        Tile tile = map.getTile(position.x, position.y);

        if (tile == null) {
            throw new IllegalStateException("Tile not found at position: " + position);
        }

        return tile.getType();
    }

    /**
     * Gets a random empty position suitable for power-up spawning.
     *
     * @param sessionId unique identifier of the game session
     * @return random empty Point, or null if no empty tiles available
     * @throws IllegalArgumentException if sessionId is null or blank
     * @throws IllegalStateException    if map does not exist for session
     */
    public Point getRandomEmptyPosition(String sessionId) {
        validateSessionId(sessionId);

        GameMap map = getMap(sessionId);
        List<Point> emptyPositions = map.getEmptyTilePositions();

        if (emptyPositions.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(emptyPositions.size());
        Point position = emptyPositions.get(randomIndex);

        return position;
    }

    /**
     * Removes the map for a session to free memory.
     *
     * @param sessionId unique identifier of the game session
     * @throws IllegalArgumentException if sessionId is null or blank
     */
    public void clearSession(String sessionId) {
        validateSessionId(sessionId);

        GameMap removed = maps.remove(sessionId);

        if (removed != null) {
        } else {
        }
    }

    /**
     * Generates a tile matrix with walls and empty spaces following Bomberman
     * pattern.
     *
     * @param width  map width
     * @param height map height
     * @return 2D array of Tile instances
     */
    private Tile[][] generateTileMatrix(int width, int height) {
        Tile[][] tiles = new Tile[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TileType type = determineTileType(x, y, width, height);

                tiles[y][x] = Tile.builder()
                        .posX(x)
                        .posY(y)
                        .type(type)
                        .occupied(false)
                        .destructible(type == TileType.DESTRUCTIBLE_WALL)
                        .hasBomb(false)
                        .build();

                // tiles[y][x].initDefaults(); //TODO: Initialize defaults if needed
            }
        }

        return tiles;
    }

    /**
     * Determines the tile type for a position following Bomberman classic layout.
     * - Border: SOLID_WALL
     * - Even row/col intersections: SOLID_WALL
     * - Other: DESTRUCTIBLE_WALL (with probability) or EMPTY
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  map width
     * @param height map height
     * @return TileType for the position
     */
    private TileType determineTileType(int x, int y, int width, int height) {
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return TileType.SOLID_WALL;
        }

        if (x % 2 == 0 && y % 2 == 0) {
            return TileType.SOLID_WALL;
        }

        if (isSpawnZone(x, y, width, height)) {
            return TileType.EMPTY;
        }

        return random.nextDouble() < DESTRUCTIBLE_WALL_PROBABILITY
                ? TileType.DESTRUCTIBLE_WALL
                : TileType.EMPTY;
    }

    /**
     * Checks if position is in a spawn zone (corners and adjacent tiles).
     * Ensures spawn points are clear of obstacles.
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  map width
     * @param height map height
     * @return true if position is in spawn zone
     */
    private boolean isSpawnZone(int x, int y, int width, int height) {
        return (x <= 2 && y <= 2) ||
                (x >= width - 3 && y <= 2) ||
                (x <= 2 && y >= height - 3) ||
                (x >= width - 3 && y >= height - 3);
    }

    /**
     * Generates spawn points at map corners.
     * Classic Bomberman layout: top-left, top-right, bottom-left, bottom-right.
     *
     * @param width  map width
     * @param height map height
     * @return list of spawn Point instances
     */
    private List<Point> generateSpawnPoints(int width, int height) {
        List<Point> spawnPoints = new ArrayList<>();

        spawnPoints.add(new Point(1, 1));
        spawnPoints.add(new Point(width - 2, 1));
        spawnPoints.add(new Point(1, height - 2));
        spawnPoints.add(new Point(width - 2, height - 2));

        return spawnPoints;
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
     * Validates map dimensions.
     *
     * @param width  map width
     * @param height map height
     * @throws IllegalArgumentException if dimensions are too small
     */
    private void validateDimensions(int width, int height) {
        if (width < 12 || height < 12) {
            throw new ValidationException("Map dimensions must be at least 12x12", "dimensions");
        }
    }
}
