package com.arsw.bomberdino.controller.rest.v1;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.bomberdino.controller.websocket.WebSocketController;
import com.arsw.bomberdino.model.dto.request.CreateRoomRequestDTO;
import com.arsw.bomberdino.model.dto.request.JoinRoomRequestDTO;
import com.arsw.bomberdino.model.dto.response.GameRoomDTO;
import com.arsw.bomberdino.model.dto.response.GameStateDTO;
import com.arsw.bomberdino.model.dto.response.PlayerDTO;
import com.arsw.bomberdino.model.entity.GameRoom;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.model.entity.Player;
import com.arsw.bomberdino.model.enums.GameStatus;
import com.arsw.bomberdino.service.impl.GameSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for game room management operations. Handles room creation, joining, listing, and
 * player disconnection.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@RestController
@RequestMapping("/api/v1/game")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GameController {

    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final GameSessionService gameSessionService;
    private final WebSocketController webSocketController;

    /**
     * Creates a new game room. Initializes room with host player and configuration.
     *
     * @param request CreateRoomRequestDTO with room configuration
     * @return ResponseEntity with GameRoomDTO containing room details
     */
    @PostMapping("/rooms")
    public ResponseEntity<GameRoomDTO> createRoom(
            @Valid @RequestBody CreateRoomRequestDTO request) {
        try {
            UUID roomId = UUID.randomUUID();
            String roomIdStr = roomId.toString();
            String roomCode = roomId.toString().substring(0, 6).toUpperCase(); // 👈 Código corto

            GameRoom room = GameRoom.builder().roomId(roomId).name(request.getRoomName())
                    .roomCode(roomCode)
                    .playerIds(new ArrayList<>()).maxPlayers(request.getMaxPlayers())
                    .isPrivate(request.isPrivate()).status(GameStatus.WAITING)
                    .createdAt(LocalDateTime.now()).password(request.getPassword()).build();

            GameSession session =
                    gameSessionService.createSession(roomCode, request.getMaxPlayers());

            GameRoomDTO roomDTO = GameRoomDTO.builder().roomId(roomIdStr).roomName(room.getName())
                    .roomCode(room.getRoomCode()).status(room.getStatus())
                    .currentPlayers(getCurrentPlayersDTO(session.getPlayers())).maxPlayers(room.getMaxPlayers())
                    .isPrivate(room.isPrivate()).build();



            logger.info("Room created successfully: {} (session: {})", roomIdStr,
                    session.getSessionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(roomDTO);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid room creation request: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating room: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create room", e);
        }
    }

    /**
     * Allows a player to join an existing room. Validates room availability and password if
     * private.
     *
     * @param request JoinRoomRequestDTO with room and player details
     * @return ResponseEntity with GameRoomDTO containing updated room state
     */
    @PostMapping("/rooms/join")
    public ResponseEntity<GameRoomDTO> joinRoom(@Valid @RequestBody JoinRoomRequestDTO request) {
        logger.info("Player {} attempting to join room {}", request.getPlayerId(),
                request.getRoomId());

        try {
            String sessionId = request.getRoomId();
            GameSession session = gameSessionService.getSession(sessionId);

            if (session.getStatus() != GameStatus.WAITING) {
                logger.warn("Cannot join room {} - game already started (status: {})", sessionId,
                        session.getStatus());
                throw new IllegalStateException("Cannot join room after game started");
            }

            List<Point> availableSpawnPoints = session.getMap().getAvailableSpawnPoints();
            if (availableSpawnPoints.isEmpty()) {
                logger.warn("No spawn points available in room {}", sessionId);
                throw new IllegalStateException("No available spawn points");
            }

            Point spawnPoint = availableSpawnPoints.get(0);
            gameSessionService.addPlayer(sessionId, request.getPlayerId(), spawnPoint);

        //     GameStateDTO currentState = session.getCurrentState();
        // webSocketController.broadcastGameState(sessionId, currentState);

            GameRoomDTO roomDTO = GameRoomDTO.builder().roomId(sessionId)
                    .roomName("Room_" + sessionId).roomCode(request.getRoomId())
                    .status(session.getStatus()).currentPlayers(getCurrentPlayersDTO(session.getPlayers()))
                    .maxPlayers(4).isPrivate(false).build();

            logger.info("Player {} joined room {} successfully", request.getPlayerId(), sessionId);

            return ResponseEntity.ok(roomDTO);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid join room request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.error("Cannot join room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            logger.error("Error joining room: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all rooms matching a specific status. Used for lobby listing and room browser.
     *
     * @param status GameStatus to filter by
     * @return ResponseEntity with list of GameRoomDTO
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<GameRoomDTO>> getRoomsByStatus(@RequestParam GameStatus status) {
        logger.debug("Fetching rooms with status: {}", status);

        try {
            List<GameSession> sessions = gameSessionService.getSessionsByStatus(status);

            List<GameRoomDTO> roomDTOs = sessions.stream().map(session -> GameRoomDTO.builder()
                    .roomId(session.getSessionId().toString())
                    .roomName("Room_" + session.getSessionId().toString().substring(0, 8))
                    .roomCode(session.getSessionId().toString().substring(0, 6).toUpperCase())
                    .status(session.getStatus()).currentPlayers(getCurrentPlayersDTO(session.getPlayers()))
                    .maxPlayers(4).isPrivate(false).build()).toList();

            logger.info("Found {} rooms with status {}", roomDTOs.size(), status);

            return ResponseEntity.ok(roomDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid status parameter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error fetching rooms by status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Removes a player from a room. Handles graceful disconnection and lobby cleanup.
     *
     * @param sessionId session identifier
     * @param playerId player identifier to remove
     * @return ResponseEntity with no content on success
     */
    @DeleteMapping("/rooms/{sessionId}/players/{playerId}")
    public ResponseEntity<Void> leaveRoom(@PathVariable String sessionId,
            @PathVariable String playerId) {

        logger.info("Player {} leaving room {}", playerId, sessionId);

        try {
            gameSessionService.removePlayer(sessionId, playerId);

            logger.info("Player {} left room {} successfully", playerId, sessionId);

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid leave room request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            logger.error("Cannot leave room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error leaving room: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<PlayerDTO> getCurrentPlayersDTO(List<Player> currentPlayers){
        return currentPlayers.stream()
        .map(p -> PlayerDTO.builder()
                .id(p.getId().toString())
                .username(p.getUsername())
                .posX(p.getPosX())
                .posY(p.getPosY())
                .lifeCount(p.getLifeCount())
                .status(p.getStatus())
                .kills(p.getKills())
                .deaths(p.getDeaths())
                .hasShield(p.hasActiveShield())
                .build())
        .toList();
    }


}
