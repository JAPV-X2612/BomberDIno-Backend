package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.PowerUpType;
import com.arsw.bomberdino.model.enums.TileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tile entity representing a single grid cell on the game map. Thread-safe for concurrent access
 * during gameplay.
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
public class Tile extends GameEntity implements Destructible {

    @NotNull(message = "Tile type cannot be null")
    private TileType type;

    private boolean occupied;
    private boolean destructible;
    private boolean hasBomb;

    private final ReentrantLock lock = new ReentrantLock();

    private static final Random random = new Random();
    private static final double POWERUP_DROP_RATE = 0.3;

    /**
     * Destroys the tile if destructible. Converts destructible walls to empty tiles.
     *
     * @throws IllegalStateException if tile is not destructible
     */
    public void destroy() {
        lock.lock();
        try {
            if (!destructible) {
                throw new IllegalStateException("Tile is not destructible");
            }
            this.type = TileType.EMPTY;
            this.destructible = false;
            this.occupied = false;
            this.hasBomb = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if tile is currently occupied by any entity.
     *
     * @return true if occupied, false otherwise
     */
    public boolean isOccupied() {
        lock.lock();
        try {
            return occupied;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Attempts to spawn a power-up on this tile.
     *
     * @return PowerUp instance if dropped, null otherwise
     * @throws IllegalStateException if tile is occupied or not empty
     */
    public PowerUp dropPowerUp() {
        lock.lock();
        try {
            if (occupied || type != TileType.EMPTY) {
                throw new IllegalStateException(
                        "Cannot drop power-up on occupied or non-empty tile");
            }

            if (random.nextDouble() > POWERUP_DROP_RATE) {
                return null;
            }

            PowerUp powerUp = PowerUp.builder().posX(this.posX).posY(this.posY)
                    .type(PowerUpType.BOMB_COUNT_UP) // For now, always drop BOMB_COUNT_UP -
                                                     // then PowerUpType.getRandomType()
                    .value(1).spawnTime(System.currentTimeMillis()).duration(30000L).build();
            powerUp.initDefaults();

            this.occupied = true;
            return powerUp;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if tile has a bomb placed on it.
     *
     * @return true if bomb exists on tile
     */
    public boolean hasBomb() {
        lock.lock();
        try {
            return hasBomb;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe method to place a bomb on this tile. Allows player to place bomb on their current
     * position.
     *
     * @return true if bomb placed successfully, false if tile already has bomb
     */
    public boolean tryPlaceBomb() {
        lock.lock();
        try {
            if (hasBomb) {
                return false;
            }
            this.hasBomb = true;
            this.occupied = true;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe method to remove bomb from tile. Called after bomb explodes.
     */
    public void removeBomb() {
        lock.lock();
        try {
            this.hasBomb = false;
            this.occupied = false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe method to occupy tile.
     *
     * @param occupy true to occupy, false to free
     * @return true if state changed successfully
     */
    public boolean setOccupied(boolean occupy) {
        lock.lock();
        try {
            if (occupy && (occupied || !type.isWalkable())) {
                return false;
            }
            this.occupied = occupy;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be non-negative");
        }
        if (destructible) {
            destroy();
        }
    }

    @Override
    public boolean isDestroyed() {
        return type == TileType.EMPTY && !destructible;
    }

    @Override
    public void onDestroy() {
        lock.lock();
        try {
            this.occupied = false;
            this.hasBomb = false;
        } finally {
            lock.unlock();
        }
    }
}
