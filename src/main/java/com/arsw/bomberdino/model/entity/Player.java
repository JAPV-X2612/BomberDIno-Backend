package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.Direction;
import com.arsw.bomberdino.model.enums.PlayerStatus;
import com.arsw.bomberdino.model.enums.PowerUpType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Player entity representing a game participant. Implements movement, bomb placement, and power-up
 * collection.
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
public class Player extends GameEntity implements Movable, Destructible {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Min(value = 0, message = "Life count cannot be negative")
    private int lifeCount;

    @Min(value = 1, message = "Bomb count must be at least 1")
    private int bombCount;

    @Min(value = 1, message = "Bomb range must be at least 1")
    private int bombRange;

    @Min(value = 1, message = "Speed must be positive")
    private int speed;

    @NotNull(message = "Player status cannot be null")
    private PlayerStatus status;

    @Valid
    @Default
    private List<PowerUp> activePowerUps = new ArrayList<>();

    @Min(value = 0, message = "Kills cannot be negative")
    private int kills;

    @Min(value = 0, message = "Deaths cannot be negative")
    private int deaths;

    @NotNull(message = "Spawn point cannot be null")
    private Point spawnPoint;

    private static final int DEFAULT_LIFE_COUNT = 3;
    private static final int DEFAULT_BOMB_COUNT = 1;
    private static final int DEFAULT_BOMB_RANGE = 2;
    private static final int DEFAULT_SPEED = 1;

    /**
     * Places a bomb at player's current position.
     *
     * @param tile the tile where bomb is placed
     * @return Bomb instance if placement successful, null otherwise
     * @throws IllegalArgumentException if tile is null
     * @throws IllegalStateException if player cannot place bombs
     */
    public Bomb placeBomb(Tile tile) {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null");
        }
        if (status != PlayerStatus.ALIVE) {
            throw new IllegalStateException("Dead players cannot place bombs");
        }
        if (!tile.tryPlaceBomb()) {
            return null;
        }

        Bomb bomb = Bomb.builder().posX(this.posX).posY(this.posY).range(this.bombRange)
                .state(com.arsw.bomberdino.model.enums.BombState.PLACED)
                .placedTime(System.currentTimeMillis()).explosionDelay(3000L).build();
        bomb.initDefaults();

        return bomb;
    }

    /**
     * Applies power-up effect to player.
     *
     * @param powerUp power-up to apply
     * @throws IllegalArgumentException if powerUp is null
     */
    public void applyPowerUp(PowerUp powerUp) {
        if (powerUp == null) {
            throw new IllegalArgumentException("PowerUp cannot be null");
        }

        powerUp.applyTo(this);
    }

    /**
     * Kills the player and increments death counter. Changes status to DEAD or SPECTATING based on
     * remaining lives.
     */
    public void die() {
        this.deaths++;

        if (isAlive()) {
            this.status = PlayerStatus.DEAD;
        } else {
            this.status = PlayerStatus.SPECTATING;
        }
    }

    /**
     * Increments kills counter for this player. Used when player eliminates another player.
     */
    public void incrementKills() {
        this.kills++;
    }

    /**
     * Checks if player can place another bomb.
     *
     * @param currentBombsPlaced number of bombs currently active for this player
     * @return true if player has not reached bomb limit
     */
    public boolean canPlaceBomb(int currentBombsPlaced) {
        return currentBombsPlaced < bombCount;
    }

    /**
     * Respawns player at spawn point. Resets position and status to ALIVE.
     *
     * @throws IllegalStateException if player has no lives remaining
     */
    public void respawn() {
        if (!isAlive()) {
            throw new IllegalStateException("Cannot respawn player with no lives remaining");
        }

        this.posX = spawnPoint.x;
        this.posY = spawnPoint.y;
        this.status = PlayerStatus.ALIVE;

        cleanupExpiredPowerUps();
    }

    /**
     * Checks if player has lives remaining.
     *
     * @return true if lifeCount - deaths > 0
     */
    public boolean isAlive() {
        return (lifeCount - deaths) > 0;
    }

    /**
     * Checks if player has active shield power-up.
     *
     * @return true if TEMPORARY_SHIELD is active and not expired
     */
    public boolean hasActiveShield() {
        if (activePowerUps == null || activePowerUps.isEmpty()) {
            return false;
        }

        return activePowerUps.stream()
                .anyMatch(pu -> pu.getType() == PowerUpType.TEMPORARY_SHIELD && !pu.isExpired());
    }

    /**
     * Removes expired power-ups from active list. Called during player update cycle.
     */
    public void cleanupExpiredPowerUps() {
        if (activePowerUps != null) {
            activePowerUps.removeIf(PowerUp::isExpired);
        }
    }

    @Override
    public void move(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot ser null");
        }
        if (status != PlayerStatus.ALIVE) {
            throw new IllegalStateException("Dead players cannot move");
        }

        Point newPos = direction.applyTo(posX, posY);

        if (newPos == null) {
            throw new IllegalStateException("Direction.applyTo returned null");
        }

        if (canMoveTo(newPos.x, newPos.y)) {
            this.posX = newPos.x;
            this.posY = newPos.y;
        }
    }

    @Override
    public boolean canMoveTo(int posX, int posY) {
        return posX >= 0 && posY >= 0;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public void takeDamage(int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be non-negative");
        }

        if (hasActiveShield()) {
            activePowerUps.removeIf(p -> p.getType() == PowerUpType.TEMPORARY_SHIELD);
            return;
        }

        die();
    }

    @Override
    public boolean isDestroyed() {
        return status == PlayerStatus.SPECTATING;
    }

    @Override
    public void onDestroy() {
        this.status = PlayerStatus.DISCONNECTED;
        if (activePowerUps != null) {
            activePowerUps.clear();
        }
    }
}
