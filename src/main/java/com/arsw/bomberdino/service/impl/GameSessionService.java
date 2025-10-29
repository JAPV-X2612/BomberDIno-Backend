package com.arsw.bomberdino.service.impl;

import com.arsw.bomberdino.model.entity.GameMap;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.model.entity.Player;
import com.arsw.bomberdino.model.enums.GameStatus;
import com.arsw.bomberdino.model.enums.PlayerStatus;

import lombok.RequiredArgsConstructor;

import com.arsw.bomberdino.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing game sessions lifecycle and state. Handles session creation, player
 * management, and state transitions. Thread-safe for concurrent access from multiple players.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final TileService tileService;
    private final GameMapService gameMapService;

    /**
     * In-memory storage for active game sessions. Key: sessionId, Value: GameSession
     */
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    private static final int DEFAULT_MAX_PLAYERS = 4;
    private static final int DEFAULT_ROUND_DURATION = 180;

    /**
     * Creates a new game session from a room. Initializes game map and tile service for the
     * session.
     *
     * @param roomId unique identifier of the room creating this session
     * @param maxPlayers maximum number of players allowed
     * @return newly created GameSession instance
     * @throws ValidationException if roomId is null/blank or maxPlayers invalid
     * @throws IllegalStateException if session already exists for room
     */
    public GameSession createSession(String roomId, int maxPlayers) {
        validateRoomId(roomId);
        validateMaxPlayers(maxPlayers);

        if (sessions.containsKey(roomId)) {
            throw new IllegalStateException("Session already exists for room: " + roomId);
        }

        GameMap map = gameMapService.createMap(roomId, 13, 13);
        tileService.initializeTiles(roomId, map);

        GameSession session = GameSession.builder().sessionId(UUID.randomUUID())
                .status(GameStatus.WAITING).map(map).players(new ArrayList<>())
                .activeBombs(new ArrayList<>()).activeExplosions(new ArrayList<>())
                .availablePowerUps(new ArrayList<>()).roundDuration(DEFAULT_ROUND_DURATION).build();

        sessions.put(roomId, session);

        return session;
    }

    /**
     * Starts a game session. Transitions status to IN_PROGRESS and initializes start time.
     *
     * @param sessionId unique identifier of the session
     * @throws ValidationException if sessionId is null or blank
     * @throws IllegalStateException if session not found or not in valid state
     */
    public void startSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.WAITING
                && session.getStatus() != GameStatus.STARTING) {
            throw new IllegalStateException(
                    "Session must be in WAITING or STARTING status to start");
        }

        if (session.getPlayers().size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start session");
        }

        session.start();
    }

    /**
     * Ends a game session. Transitions status to FINISHED and cleans up resources.
     *
     * @param sessionId unique identifier of the session
     * @throws ValidationException if sessionId is null or blank
     * @throws IllegalStateException if session not found
     */
    public void endSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = getSession(sessionId);

        session.setStatus(GameStatus.FINISHED);
        session.setEndTime(LocalDateTime.now());

    }

    /**
     * Pauses a game session. Transitions status to PAUSED. Reserved for future implementation.
     *
     * @param sessionId unique identifier of the session
     * @throws ValidationException if sessionId is null or blank
     * @throws IllegalStateException if session not found or not in progress
     */
    public void pauseSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only pause sessions in IN_PROGRESS status");
        }

        session.setStatus(GameStatus.PAUSED);
    }

    /**
     * Resumes a paused game session. Transitions status back to IN_PROGRESS. Reserved for future
     * implementation.
     *
     * @param sessionId unique identifier of the session
     * @throws IllegalArgumentException if sessionId is null or blank
     * @throws IllegalStateException if session not found or not paused
     */
    public void resumeSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.PAUSED) {
            throw new IllegalStateException("Can only resume sessions in PAUSED status");
        }

        session.setStatus(GameStatus.IN_PROGRESS);
    }

    /**
     * Retrieves a game session by ID.
     *
     * @param sessionId unique identifier of the session
     * @return GameSession instance
     * @throws IllegalArgumentException if sessionId is null or blank
     * @throws IllegalStateException if session not found
     */
    public GameSession getSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = sessions.get(sessionId);

        if (session == null) {
            throw new IllegalStateException("Session not found: " + sessionId);
        }

        return session;
    }

    /**
     * Adds a player to a game session. Assigns spawn point and initializes player state.
     *
     * @param sessionId unique identifier of the session
     * @param playerId unique identifier of the player
     * @param spawnPoint initial spawn position for the player
     * @return newly created Player instance
     * @throws ValidationException if parameters are null or blank
     * @throws IllegalStateException if session not found or not in waiting status
     */
    public Player addPlayer(String sessionId, String playerId, Point spawnPoint) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);

        if (spawnPoint == null) {
            throw new ValidationException("Spawn point cannot be null", "spawnPoint");
        }

        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Can only add players to sessions in WAITING status");
        }

        Player player = Player.builder().id(UUID.fromString(playerId))
                .username("Player_" + playerId.substring(0, 8)).posX(spawnPoint.x)
                .posY(spawnPoint.y).lifeCount(3).bombCount(1).bombRange(2).speed(1)
                .status(PlayerStatus.ALIVE).activePowerUps(new ArrayList<>()).kills(0).deaths(0)
                .spawnPoint(spawnPoint).build();
        player.initDefaults();

        session.addPlayer(player);
        tileService.tryOccupy(sessionId, spawnPoint, false);

        return player;
    }

    /**
     * Removes a player from a game session. Frees occupied tile and removes player from session.
     *
     * @param sessionId unique identifier of the session
     * @param playerId unique identifier of the player to remove
     * @throws ValidationException if sessionId or playerId is null or blank
     * @throws IllegalStateException if session or player not found
     */
    public void removePlayer(String sessionId, String playerId) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);

        GameSession session = getSession(sessionId);

        Player playerToRemove =
                session.getPlayers().stream().filter(p -> p.getId().toString().equals(playerId))
                        .findFirst().orElseThrow(() -> new IllegalStateException(
                                "Player not found in session: " + playerId));

        Point playerPosition = new Point(playerToRemove.getPosX(), playerToRemove.getPosY());
        tileService.releaseOccupation(sessionId, playerPosition);

        session.removePlayer(playerToRemove);

    }

    /**
     * Updates the game state for a session. Processes bombs, explosions, power-ups, and win
     * conditions. Called by game loop at fixed rate (e.g., 60 FPS).
     *
     * @param sessionId unique identifier of the session
     * @throws ValidationException if sessionId is null or blank
     * @throws IllegalStateException if session not found
     */
    public void updateGameState(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = getSession(sessionId);

        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            return;
        }

        session.update(0.016f); // 60 FPS delta time

    }

    /**
     * Gets all sessions matching a specific status. Used for lobby listing and admin dashboards.
     *
     * @param status GameStatus to filter by
     * @return list of GameSession instances with matching status
     * @throws ValidationException if status is null
     */
    public List<GameSession> getSessionsByStatus(GameStatus status) {
        if (status == null) {
            throw new ValidationException("Status cannot be null", "status");
        }

        return sessions.values().stream().filter(session -> session.getStatus() == status).toList();
    }

    /**
     * Removes a session and cleans up associated resources. Called when session ends or expires.
     *
     * @param sessionId unique identifier of the session
     * @throws ValidationException if sessionId is null or blank
     */
    public void clearSession(String sessionId) {
        validateSessionId(sessionId);

        GameSession removed = sessions.remove(sessionId);

        if (removed != null) {
            tileService.clearSession(sessionId);
            gameMapService.clearSession(sessionId);
        }
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
     * Validates room ID parameter.
     *
     * @param roomId room identifier to validate
     * @throws ValidationException if roomId is null or blank
     */
    private void validateRoomId(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new ValidationException("Room ID cannot be null or blank", "roomId");
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
     * Validates max players parameter.
     *
     * @param maxPlayers maximum number of players
     * @throws ValidationException if maxPlayers is out of valid range
     */
    private void validateMaxPlayers(int maxPlayers) {
        if (maxPlayers < 2 || maxPlayers > 8) {
            throw new ValidationException("Max players must be between 2 and 8", "maxPlayers");
        }
    }

    /**
     * Gets list of player IDs affected by an explosion. Checks if any players occupy tiles in the
     * explosion range.
     *
     * @param sessionId unique identifier of the session
     * @param explosionTiles list of tiles affected by explosion
     * @return list of player IDs as Strings
     * @throws IllegalArgumentException if sessionId or explosionTiles is null
     * @throws IllegalStateException if session not found
     */
    public List<String> getAffectedPlayers(String sessionId, List<Point> explosionTiles) {
        validateSessionId(sessionId);

        if (explosionTiles == null) {
            throw new IllegalArgumentException("Explosion tiles list cannot be null");
        }

        if (explosionTiles.isEmpty()) {
            return new ArrayList<>();
        }

        GameSession session = getSession(sessionId);
        List<String> affectedPlayerIds = new ArrayList<>();

        for (Player player : session.getPlayers()) {
            if (player.getStatus() != PlayerStatus.ALIVE) {
                continue;
            }

            Point playerPosition = new Point(player.getPosX(), player.getPosY());

            boolean isInExplosion = explosionTiles.stream()
                    .anyMatch(tile -> tile.x == playerPosition.x && tile.y == playerPosition.y);

            if (isInExplosion) {
                affectedPlayerIds.add(player.getId().toString());
            }
        }

        return affectedPlayerIds;
    }
}
