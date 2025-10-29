package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.dto.response.BombDTO;
import com.arsw.bomberdino.model.dto.response.ExplosionDTO;
import com.arsw.bomberdino.model.dto.response.PointDTO;
import com.arsw.bomberdino.model.dto.response.GameStateDTO;
import com.arsw.bomberdino.model.dto.response.PlayerDTO;
import com.arsw.bomberdino.model.dto.response.PowerUpDTO;
import com.arsw.bomberdino.model.dto.response.TileDTO;
import com.arsw.bomberdino.model.enums.GameStatus;
import com.arsw.bomberdino.model.enums.PlayerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.awt.Point;

/**
 * Active game session managing gameplay state and entity lifecycle. Thread-safe for concurrent
 * player actions and game loop updates.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @NotNull(message = "Session ID cannot be null")
    private UUID sessionId;

    @NotNull(message = "Game status cannot be null")
    private GameStatus status;

    @NotNull(message = "Game map cannot be null")
    @Valid
    private GameMap map;

    @NotNull(message = "Players list cannot be null")
    @Valid
    private List<Player> players;

    @NotNull(message = "Active bombs list cannot be null")
    @Valid
    private List<Bomb> activeBombs;

    @NotNull(message = "Active explosions list cannot be null")
    @Valid
    private List<Explosion> activeExplosions;

    @NotNull(message = "Available power-ups list cannot be null")
    @Valid
    private List<PowerUp> availablePowerUps;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 60, message = "Round duration must be at least 60 seconds")
    private int roundDuration;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static final int DEFAULT_ROUND_DURATION = 180;

    /**
     * Starts the game session. Initializes start time and changes status to IN_PROGRESS.
     *
     * @throws IllegalStateException if session is not in WAITING or STARTING status
     */
    public void start() {
        lock.writeLock().lock();
        try {
            if (status != GameStatus.WAITING && status != GameStatus.STARTING) {
                throw new IllegalStateException(
                        "Cannot start session in current status: " + status);
            }

            this.status = GameStatus.IN_PROGRESS;
            this.startTime = LocalDateTime.now();

            players.forEach(player -> {
                if (player.getStatus() == com.arsw.bomberdino.model.enums.PlayerStatus.ALIVE) {
                    player.respawn();
                }
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds player to the session. Assigns spawn point and initializes player state.
     *
     * @param player player to add
     * @throws IllegalArgumentException if player is null
     * @throws IllegalStateException if session is full or not in WAITING status
     */
    public void addPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        lock.writeLock().lock();
        try {
            if (status != GameStatus.WAITING) {
                throw new IllegalStateException("Cannot add players after session started");
            }

            if (players == null) {
                players = new ArrayList<>();
            }

            players.add(player);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes player from the session.
     *
     * @param player player to remove
     * @throws IllegalArgumentException if player is null
     */
    public void removePlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        lock.writeLock().lock();
        try {
            players.remove(player);

            if (players.isEmpty() || getAlivePlayersCount() <= 1) {
                endSession();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates game state for current frame. Processes bomb explosions, power-up spawning, and win
     * conditions.
     *
     * @param delta time elapsed since last update in seconds
     */
    public void update(float delta) {
        lock.writeLock().lock();
        try {
            if (status != GameStatus.IN_PROGRESS) {
                return;
            }

            processExpiredBombs();
            processExpiredExplosions();
            processExpiredPowerUps();
            checkWinCondition();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Finds winner when only one player remains alive.
     *
     * @return winning Player, or null if no clear winner
     */
    public Player getWinner() {
        lock.readLock().lock();
        try {
            List<Player> alivePlayers = players.stream().filter(Player::isAlive).toList();

            return alivePlayers.size() == 1 ? alivePlayers.get(0) : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets current game state as DTO for client broadcasting.
     *
     * @return GameStateDTO with all relevant session data
     */
    public GameStateDTO getCurrentState() {
        lock.readLock().lock();
        try {
            return GameStateDTO.builder().sessionId(sessionId.toString()).status(status)
                    .tiles(mapTilesToDTO()).players(mapPlayersToDTO()).bombs(mapBombsToDTO())
                    .explosions(mapExplosionsToDTO()).powerUps(mapPowerUpsToDTO())
                    .serverTime(System.currentTimeMillis()).build();
        } finally {
            lock.readLock().unlock();
        }
    }

    private TileDTO[][] mapTilesToDTO() {
        Tile[][] tiles = map.getTiles();
        int height = tiles.length;
        int width = height > 0 ? tiles[0].length : 0;

        TileDTO[][] out = new TileDTO[height][width];
        for (int y = 0; y < height; y++) {
            Tile[] row = tiles[y];
            for (int x = 0; x < width; x++) {
                out[y][x] = TileDTO.builder().x(x).y(y).type(row[x].getType()).build();
            }
        }
        return out;
    }

    private List<PlayerDTO> mapPlayersToDTO() {
        return players.stream()
                .map(p -> PlayerDTO.builder().id(p.getId().toString()).username(p.getUsername())
                        .posX(p.getPosX()).posY(p.getPosY()).lifeCount(p.getLifeCount())
                        .status(p.getStatus()).kills(p.getKills()).deaths(p.getDeaths())
                        .hasShield(p.hasActiveShield()).build())
                .toList();
    }

    private List<BombDTO> mapBombsToDTO() {
        return activeBombs.stream()
                .map(b -> BombDTO.builder().id(b.getId().toString()).ownerId(b.getId().toString())
                        .posX(b.getPosX()).posY(b.getPosY()).range(b.getRange())
                        .timeToExplode(b.getTimeUntilExplosion()).build())
                .toList();
    }

    private List<ExplosionDTO> mapExplosionsToDTO() {
        return activeExplosions.stream().map(e -> {
            List<PointDTO> affectedPoints = e.getAffectedTiles().stream()
                    .map(t -> new PointDTO(t.getPosX(), t.getPosY())).toList();

            return ExplosionDTO.builder().id(e.getId().toString()).tiles(affectedPoints)
                    .duration(e.getDuration()).build();
        }).toList();
    }

    private List<PowerUpDTO> mapPowerUpsToDTO() {
        return availablePowerUps.stream().map(p -> PowerUpDTO.builder().id(p.getId().toString())
                .type(p.getType()).posX(p.getPosX()).posY(p.getPosY()).build()).toList();
    }

    /**
     * Processes bombs ready to explode. Triggers explosion logic and cleans up exploded bombs.
     */
    private void processExpiredBombs() {
        List<Bomb> explodedBombs = new ArrayList<>();

        activeBombs.forEach(bomb -> {
            if (bomb.isReadyToExplode()) {
                Tile tile = map.getTile(bomb.getPosX(), bomb.getPosY());
                if (tile != null) {
                    tile.removeBomb();
                }

                Explosion explosion = bomb.explode();
                explosion.expand(map.getTiles(), bomb.getRange());
                explosion.dealDamage();

                damagePlayersInExplosion(explosion);

                activeExplosions.add(explosion);
                explodedBombs.add(bomb);
            }
        });

        activeBombs.removeAll(explodedBombs);
    }

    private void processExpiredExplosions() {
        activeExplosions.removeIf(explosion -> System.currentTimeMillis() > (explosion
                .getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                + explosion.getDuration()));
    }

    private void processExpiredPowerUps() {
        availablePowerUps.removeIf(PowerUp::isExpired);
    }

    /**
     * Applies damage to players caught in explosion range. Respects shield power-up protection.
     *
     * @param explosion Explosion entity with affected tiles
     */
    private void damagePlayersInExplosion(Explosion explosion) {
        if (explosion.getAffectedTiles() == null || explosion.getAffectedTiles().isEmpty()) {
            return;
        }

        players.forEach(player -> {
            Point playerPos = new Point(player.getPosX(), player.getPosY());

            boolean isInExplosion = explosion.getAffectedTiles().stream().anyMatch(
                    tile -> tile.getPosX() == playerPos.x && tile.getPosY() == playerPos.y);

            if (isInExplosion && player.getStatus() == PlayerStatus.ALIVE) {
                player.takeDamage(explosion.getDamage());
            }
        });
    }

    /**
     * Checks win condition and ends session if met. Win occurs when only one player remains alive.
     */
    private void checkWinCondition() {
        long alivePlayers = getAlivePlayersCount();

        if (alivePlayers <= 1) {
            endSession();
        }
    }

    private long getAlivePlayersCount() {
        lock.readLock().lock();
        try {
            return players.stream().filter(Player::isAlive).count();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void endSession() {
        this.status = GameStatus.FINISHED;
        this.endTime = LocalDateTime.now();
    }


}
