package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.entity.Bomb;
import com.arsw.bomberdino.model.enums.BombState;

import lombok.RequiredArgsConstructor;

import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for managing bomb placement, explosion scheduling, and lifecycle.
 * Handles bomb countdown timers using scheduled executors.
 * Thread-safe for concurrent bomb placements.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class BombService {

    private final CollisionService collisionService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * In-memory storage for active bombs.
     * Key: bombId, Value: Bomb
     */
    private final ConcurrentHashMap<String, Bomb> bombs = new ConcurrentHashMap<>();

    /**
     * Executor service for scheduling bomb explosions.
     */
    private final ScheduledExecutorService explosionScheduler = Executors.newScheduledThreadPool(10);

    private static final long DEFAULT_EXPLOSION_DELAY = 3000L;

    /**
     * Places a bomb at the specified position for a player.
     * Automatically schedules explosion after delay.
     *
     * @param sessionId unique identifier of the session
     * @param playerId  unique identifier of the player placing bomb
     * @param position  coordinates where bomb is placed
     * @return Bomb instance if placement successful, null if position invalid
     * @throws IllegalArgumentException if sessionId, playerId, or position is null
     */
    public Bomb placeBomb(String sessionId, String playerId, Point position) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);
        validatePosition(position);

        if (!collisionService.isValidPosition(sessionId, position)) {
            return null;
        }

        Bomb bomb = Bomb.builder()
                .posX(position.x)
                .posY(position.y)
                .range(2)
                .state(BombState.PLACED)
                .placedTime(System.currentTimeMillis())
                .explosionDelay(DEFAULT_EXPLOSION_DELAY)
                .build();
        // bomb.initDefaults(); // TODO: Initialize default properties if needed

        String bombId = bomb.getId().toString();
        bombs.put(bombId, bomb);

        scheduleBombExplosion(bombId, DEFAULT_EXPLOSION_DELAY);

        return bomb;
    }

    /**
     * Triggers bomb explosion and calculates affected tiles.
     * Changes bomb state to EXPLODING/EXPLODED and removes from active bombs.
     *
     * @param bombId unique identifier of the bomb
     * @return list of Point instances representing affected tiles
     * @throws IllegalArgumentException if bombId is null or blank
     * @throws IllegalStateException    if bomb not found or already exploded
     */
    public List<Point> explodeBomb(String bombId) {
        validateBombId(bombId);

        Bomb bomb = bombs.get(bombId);

        if (bomb == null) {
            throw new IllegalStateException("Bomb not found: " + bombId);
        }

        if (bomb.getState() != BombState.PLACED) {
            throw new IllegalStateException("Bomb must be in PLACED state to explode");
        }

        bomb.setState(BombState.EXPLODING);

        List<Point> affectedTiles = calculateExplosionTiles(bomb);

        bomb.setState(BombState.EXPLODED);

        return affectedTiles;
    }

    /**
     * Gets all active bombs for a specific session.
     *
     * @param sessionId unique identifier of the session
     * @return list of Bomb instances in the session
     * @throws ValidationException if sessionId is null or blank
     */
    public List<Bomb> getActiveBombs(String sessionId) {
        validateSessionId(sessionId);

        List<Bomb> sessionBombs = bombs.values().stream()
                .filter(bomb -> bomb.getState() == BombState.PLACED)
                .collect(Collectors.toList());

        return sessionBombs;
    }

    /**
     * Schedules a bomb explosion after specified delay.
     * Uses ScheduledExecutorService for non-blocking timer.
     *
     * @param bombId         unique identifier of the bomb
     * @param explosionDelay delay in milliseconds until explosion
     * @throws IllegalArgumentException if bombId is null or delay invalid
     */
    public void scheduleBombExplosion(String bombId, long explosionDelay) {
        validateBombId(bombId);

        if (explosionDelay < 1000) {
            throw new IllegalArgumentException("Explosion delay must be at least 1000ms");
        }

        explosionScheduler.schedule(() -> {
            try {
                if (isReadyToExplode(bombId)) {
                    explodeBomb(bombId);
                    removeBomb(bombId);
                }
            } catch (Exception e) {
            }
        }, explosionDelay, TimeUnit.MILLISECONDS);

    }

    /**
     * Checks if a bomb is ready to explode.
     *
     * @param bombId unique identifier of the bomb
     * @return true if bomb countdown has finished
     * @throws IllegalArgumentException if bombId is null or blank
     */
    public boolean isReadyToExplode(String bombId) {
        validateBombId(bombId);

        Bomb bomb = bombs.get(bombId);

        if (bomb == null) {
            return false;
        }

        return bomb.isReadyToExplode();
    }

    /**
     * Removes a bomb from active bombs storage.
     * Called after explosion completes.
     *
     * @param bombId unique identifier of the bomb
     * @throws IllegalArgumentException if bombId is null or blank
     */
    public void removeBomb(String bombId) {
        validateBombId(bombId);

        Bomb removed = bombs.remove(bombId);

        if (removed != null) {
        } else {
        }
    }

    /**
     * Calculates affected tiles in cross pattern for bomb explosion.
     * Propagates in 4 cardinal directions until hitting solid walls.
     *
     * @param bomb Bomb instance to calculate explosion for
     * @return list of affected Point instances
     */
    private List<Point> calculateExplosionTiles(Bomb bomb) {
        List<Point> affectedTiles = new ArrayList<>();

        Point center = new Point(bomb.getPosX(), bomb.getPosY());
        affectedTiles.add(center);

        int range = bomb.getRange();

        affectedTiles.addAll(calculateDirectionalExplosion(center, 0, -1, range)); // Up
        affectedTiles.addAll(calculateDirectionalExplosion(center, 0, 1, range)); // Down
        affectedTiles.addAll(calculateDirectionalExplosion(center, -1, 0, range)); // Left
        affectedTiles.addAll(calculateDirectionalExplosion(center, 1, 0, range)); // Right

        return affectedTiles;
    }

    /**
     * Calculates explosion propagation in a specific direction.
     *
     * @param origin starting point
     * @param dx     X direction delta (-1, 0, 1)
     * @param dy     Y direction delta (-1, 0, 1)
     * @param range  maximum tiles to propagate
     * @return list of affected Point instances in this direction
     */
    private List<Point> calculateDirectionalExplosion(Point origin, int dx, int dy, int range) {
        List<Point> tiles = new ArrayList<>();

        for (int i = 1; i <= range; i++) {
            int newX = origin.x + (dx * i);
            int newY = origin.y + (dy * i);

            Point tile = new Point(newX, newY);
            tiles.add(tile);

            // Note: Full implementation needs TileService to check if tile blocks explosion
            // For now, assume explosion continues until range limit
        }

        return tiles;
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
     * Validates player ID parameter.
     *
     * @param playerId player identifier to validate
     * @throws ValidationException if playerId is null or blank
     */
    private void validatePlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new ValidationException("Player ID cannot be null or blank", "playerId");
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

    /**
     * Cleanup method to shutdown executor service gracefully.
     * Should be called during application shutdown.
     */
    public void shutdown() {
        explosionScheduler.shutdown();
        try {
            if (!explosionScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                explosionScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            explosionScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
