package com.arsw.bomberdino.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Explosion entity representing bomb blast area.
 * Transient object used for damage calculation and animation.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Explosion extends GameEntity {

    @NotEmpty(message = "Affected tiles cannot be empty")
    @Valid
    private List<Tile> affectedTiles;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 100, message = "Duration must be at least 100ms")
    private Long duration;

    @Min(value = 1, message = "Damage must be positive")
    private int damage;

    /**
     * Expands explosion range to calculate affected tiles.
     * Propagates in 4 cardinal directions until hitting solid walls.
     *
     * @param gameMap the map containing all tiles
     * @param range   explosion range in tiles
     */
    public void expand(Tile[][] gameMap, int range) {
        if (affectedTiles == null) {
            affectedTiles = new ArrayList<>();
        }

        affectedTiles.clear();
        affectedTiles.add(gameMap[posX][posY]);

        expandInDirection(gameMap, range, 0, -1); // Up
        expandInDirection(gameMap, range, 0, 1); // Down
        expandInDirection(gameMap, range, -1, 0); // Left
        expandInDirection(gameMap, range, 1, 0); // Right
    }

    /**
     * Helper method to expand explosion in specific direction.
     *
     * @param gameMap tile matrix
     * @param range   remaining range
     * @param x       current X position
     * @param y       current Y position
     * @param dx      X direction delta (-1, 0, 1)
     * @param dy      Y direction delta (-1, 0, 1)
     */
    private void expandInDirection(Tile[][] gameMap, int range, int dx, int dy) {
        for (int i = 1; i <= range; i++) {
            int newX = posX + (dx * i);
            int newY = posY + (dy * i);

            if (newX < 0 || newX >= gameMap[0].length || newY < 0 || newY >= gameMap.length) {
                break;
            }

            Tile tile = gameMap[newY][newX];

            if (!tile.getType().allowsExplosion()) {
                break;
            }

            affectedTiles.add(tile);

            if (tile.getType().isDestructible()) {
                break;
            }
        }
    }

    /**
     * Checks if explosion affects the specified tile.
     *
     * @param tile tile to check
     * @return true if tile is in affected area
     * @throws IllegalArgumentException if tile is null
     */
    public boolean affectsTile(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null");
        }
        return affectedTiles != null && affectedTiles.contains(tile);
    }

    /**
     * Applies damage to all affected tiles and entities.
     * Destroys destructible tiles and damages players.
     */
    public void dealDamage() {
        if (affectedTiles == null || affectedTiles.isEmpty()) {
            return;
        }

        affectedTiles.forEach(tile -> {
            if (tile.isDestructible()) {
                tile.takeDamage(damage);
            }
        });
    }
}
