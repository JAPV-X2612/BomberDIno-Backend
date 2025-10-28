package com.arsw.bomberdino.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Game map containing tile grid and spawn point configuration.
 * Immutable after initialization to ensure map integrity.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMap {

    @NotNull(message = "Map ID cannot be null")
    private UUID mapId;

    @NotBlank(message = "Map name cannot be blank")
    @Size(min = 3, max = 50, message = "Map name must be between 3 and 50 characters")
    private String name;

    @Min(value = 12, message = "Width must be at least 5")
    private int width;

    @Min(value = 12, message = "Height must be at least 5")
    private int height;

    @NotNull(message = "Tiles matrix cannot be null")
    @Valid
    private Tile[][] tiles;

    @NotEmpty(message = "Spawn points cannot be empty")
    @Valid
    private List<Point> spawnPoints;

    /**
     * Retrieves tile at specified coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Tile at position, or null if out of bounds
     */
    public Tile getTile(int x, int y) {
        if (!isValidPosition(x, y)) {
            return null;
        }
        return tiles[y][x];
    }

    /**
     * Validates if coordinates are within map boundaries.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if position is valid
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Gets spawn points that are currently unoccupied.
     * 
     * @return list of available spawn Point instances
     */
    public List<Point> getAvailableSpawnPoints() {
        return spawnPoints.stream()
                .filter(point -> {
                    Tile tile = getTile(point.x, point.y);
                    return tile != null && !tile.isOccupied();
                })
                .toList();
    }

    /**
     * Finds all empty tiles suitable for power-up spawning.
     * 
     * @return list of unoccupied empty tile positions
     */
    public List<Point> getEmptyTilePositions() {
        List<Point> emptyPositions = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = tiles[y][x];
                if (tile.getType().canSpawnPowerUp() && !tile.isOccupied()) {
                    emptyPositions.add(new Point(x, y));
                }
            }
        }

        return emptyPositions;
    }
}
