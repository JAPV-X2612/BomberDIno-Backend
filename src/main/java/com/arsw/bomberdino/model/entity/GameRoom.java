package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Game room representing lobby before session starts.
 * Manages player joining and session creation.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoom {

    @NotNull(message = "Room ID cannot be null")
    private UUID roomId;

    @NotBlank(message = "Room name cannot be blank")
    @Size(min = 3, max = 50, message = "Room name must be between 3 and 50 characters")
    private String name;

    @NotNull(message = "Host user ID cannot be null")
    private UUID hostUserId;

    @NotNull(message = "Player IDs list cannot be null")
    private List<UUID> playerIds;

    @Min(value = 2, message = "Maximum players must be at least 2")
    @Max(value = 8, message = "Maximum players cannot exceed 8")
    private int maxPlayers;

    private boolean isPrivate;

    @NotNull(message = "Room status cannot be null")
    private GameStatus status;

    @NotNull(message = "Creation timestamp cannot be null")
    private LocalDateTime createdAt;

    private String password;

    private final ReentrantLock lock = new ReentrantLock();

    private static final int DEFAULT_MAX_PLAYERS = 4;

    /**
     * Checks if room has reached maximum player capacity.
     * 
     * @return true if player count equals maxPlayers
     */
    public boolean isFull() {
        lock.lock();
        try {
            return playerIds != null && playerIds.size() >= maxPlayers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds player to the room.
     *
     * @param userId UUID of player to add
     * @throws IllegalArgumentException if userId is null or blank
     * @throws IllegalStateException    if room is full or not in WAITING status
     */
    public void addPlayer(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }

        lock.lock();
        try {
            if (isFull()) {
                throw new IllegalStateException("Room is full");
            }

            if (status != GameStatus.WAITING) {
                throw new IllegalStateException("Cannot join room after game started");
            }

            if (playerIds == null) {
                playerIds = new ArrayList<>();
            }

            UUID playerId = UUID.fromString(userId);

            if (playerIds.contains(playerId)) {
                throw new IllegalStateException("Player already in room");
            }

            playerIds.add(playerId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes player from the room.
     *
     * @param userId UUID of player to remove
     * @throws IllegalArgumentException if userId is null or blank
     */
    public void removePlayer(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }

        lock.lock();
        try {
            UUID playerId = UUID.fromString(userId);
            playerIds.remove(playerId);

            if (playerId.equals(hostUserId) && !playerIds.isEmpty()) {
                hostUserId = playerIds.get(0);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a new game session from this room.
     * Transitions room status to STARTING.
     *
     * @return new GameSession instance
     * @throws IllegalStateException if room is not ready to start
     */
    public GameSession createSession() {
        lock.lock();
        try {
            if (status != GameStatus.WAITING) {
                throw new IllegalStateException("Room is not in WAITING status");
            }

            if (playerIds.size() < 2) {
                throw new IllegalStateException("Need at least 2 players to start");
            }

            this.status = GameStatus.STARTING;

            return GameSession.builder()
                    .sessionId(UUID.randomUUID())
                    .roomCode(generateRoomCode())
                    .status(GameStatus.STARTING)
                    .players(new ArrayList<>())
                    .activeBombs(new ArrayList<>())
                    .activeExplosions(new ArrayList<>())
                    .availablePowerUps(new ArrayList<>())
                    .roundDuration(180)
                    .build();
        } finally {
            lock.unlock();
        }
    }

    private String generateRoomCode() {
        return roomId.toString().substring(0, 8).toUpperCase();
    }
}
