package com.arsw.bomberdino.controller.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.arsw.bomberdino.model.dto.request.PlaceBombRequestDTO;
import com.arsw.bomberdino.model.dto.request.PlayerMoveRequestDTO;
import com.arsw.bomberdino.model.dto.request.PowerUpCollectRequestDTO;
import com.arsw.bomberdino.model.dto.response.BombExplodedDTO;
import com.arsw.bomberdino.model.dto.response.GameStateDTO;
import com.arsw.bomberdino.model.dto.response.PlayerKilledDTO;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.service.impl.GameFacadeService;
import com.arsw.bomberdino.service.impl.GameSessionService;

import jakarta.validation.Valid;

/**
 * WebSocket controller for real-time game interactions. Handles player actions
 * via STOMP protocol and delegates to GameFacadeService. Does NOT broadcast
 * directly - events are published and handled by WebSocketEventListener.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final GameFacadeService gameFacadeService;
    private final GameSessionService gameSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(GameFacadeService gameFacadeService,
            GameSessionService gameSessionService,
            SimpMessagingTemplate messagingTemplate) {
        this.gameFacadeService = gameFacadeService;
        this.gameSessionService = gameSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles player movement requests via WebSocket. Validates movement and
     * updates player position through GameFacadeService.
     *
     * Endpoint: /app/game/move
     *
     * @param request PlayerMoveRequestDTO with session, player, and direction
     */
    @MessageMapping("/game/move")
    public void handlePlayerMove(@Valid @Payload PlayerMoveRequestDTO request) {
        try {
            logger.debug("Received move request from player {} in session {} (direction: {})",
                    request.getPlayerId(), request.getSessionId(), request.getDirection());

            gameFacadeService.handlePlayerMove(
                    request.getSessionId(),
                    request.getPlayerId(),
                    request.getDirection()
            );

            logger.info("Player {} moved {} in session {}",
                    request.getPlayerId(), request.getDirection(), request.getSessionId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid move request from player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "INVALID_MOVE", e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Move failed for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "MOVE_FAILED", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing move for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage(), e);
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "SERVER_ERROR", "Internal server error");
        }
    }

    /**
     * Handles bomb placement requests via WebSocket. Validates placement and
     * creates bomb through GameFacadeService.
     *
     * Endpoint: /app/game/bomb
     *
     * @param request PlaceBombRequestDTO with session, player, and position
     */
    @MessageMapping("/game/bomb")
    public void handlePlaceBomb(@Valid @Payload PlaceBombRequestDTO request) {
        try {
            logger.debug("Received bomb placement request from player {} in session {} at ({}, {})",
                    request.getPlayerId(), request.getSessionId(),
                    request.getPosition().x, request.getPosition().y);

            gameFacadeService.handlePlaceBomb(
                    request.getSessionId(),
                    request.getPlayerId(),
                    request.getPosition()
            );

            logger.info("Player {} placed bomb at ({}, {}) in session {}",
                    request.getPlayerId(), request.getPosition().x,
                    request.getPosition().y, request.getSessionId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid bomb placement request from player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "INVALID_BOMB_PLACEMENT", e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Bomb placement failed for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "BOMB_PLACEMENT_FAILED", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing bomb placement for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage(), e);
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "SERVER_ERROR", "Internal server error");
        }
    }

    /**
     * Handles power-up collection requests via WebSocket. Validates collection
     * and applies effect through GameFacadeService.
     *
     * Endpoint: /app/game/powerup
     *
     * @param request PowerUpCollectRequestDTO with session, player, and
     * power-up ID
     */
    @MessageMapping("/game/powerup")
    public void handlePowerUpCollect(@Valid @Payload PowerUpCollectRequestDTO request) {
        try {
            logger.debug("Received power-up collection request from player {} in session {} (powerUp: {})",
                    request.getPlayerId(), request.getSessionId(), request.getPowerUpId());

            gameFacadeService.handlePowerUpCollection(
                    request.getSessionId(),
                    request.getPlayerId(),
                    request.getPowerUpId()
            );

            logger.info("Player {} collected power-up {} in session {}",
                    request.getPlayerId(), request.getPowerUpId(), request.getSessionId());

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid power-up collection request from player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "INVALID_POWERUP_COLLECTION", e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Power-up collection failed for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage());
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "POWERUP_COLLECTION_FAILED", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing power-up collection for player {} in session {}: {}",
                    request.getPlayerId(), request.getSessionId(), e.getMessage(), e);
            sendErrorToPlayer(request.getSessionId(), request.getPlayerId(),
                    "SERVER_ERROR", "Internal server error");
        }
    }

    /**
     * Handles player connection to a game session. Called when player
     * subscribes to session topic. Sends initial game state to newly connected
     * player.
     *
     * Endpoint: /topic/game/{sessionId}/state (subscription)
     *
     * @param sessionId session identifier
     * @param playerId player identifier
     */
    public void onPlayerConnect(String sessionId, String playerId) {
        try {
            logger.info("Player {} connected to session {}", playerId, sessionId);

            GameStateDTO currentState = gameFacadeService.getGameState(sessionId);

            String destination = "/topic/game/" + sessionId + "/state";
            messagingTemplate.convertAndSendToUser(
                    playerId,
                    destination,
                    currentState
            );

            logger.debug("Sent initial game state to player {} in session {}", playerId, sessionId);

        } catch (Exception e) {
            logger.error("Error handling player connection for {} in session {}: {}",
                    playerId, sessionId, e.getMessage(), e);
        }
    }

    /**
     * Handles player disconnection from a game session. Called when player
     * unsubscribes or connection is lost. Removes player from session and
     * notifies other players.
     *
     * @param sessionId session identifier
     * @param playerId player identifier
     */
    public void onPlayerDisconnect(String sessionId, String playerId) {
        try {
            logger.info("Player {} disconnected from session {}", playerId, sessionId);

            gameSessionService.removePlayer(sessionId, playerId);

            broadcastPlayerDisconnected(sessionId, playerId);

            logger.debug("Processed disconnection for player {} in session {}", playerId, sessionId);

        } catch (IllegalStateException e) {
            logger.warn("Player {} not found in session {} during disconnect: {}",
                    playerId, sessionId, e.getMessage());
        } catch (Exception e) {
            logger.error("Error handling player disconnection for {} in session {}: {}",
                    playerId, sessionId, e.getMessage(), e);
        }
    }

    /**
     * Broadcasts game start event to all clients in a session. Signals all
     * players to transition from lobby to game.
     *
     * @param sessionId session identifier
     * @param state initial game state
     */
    public void broadcastGameStart(String sessionId, GameStateDTO state) {
        try {
            String destination = "/topic/game/" + sessionId + "/start";

            GameStartNotification notification = GameStartNotification.builder()
                    .sessionId(sessionId)
                    .initialState(state)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend(destination, notification);

            logger.info("Broadcasted game start to session {}", sessionId);

        } catch (Exception e) {
            logger.error("Error broadcasting game start to session {}: {}",
                    sessionId, e.getMessage(), e);
        }
    }

    /**
     * Broadcasts game state to all clients in a session. Used by event
     * listeners for state synchronization.
     *
     * @param sessionId session identifier
     * @param state GameStateUpdateDTO to broadcast
     */
    public void broadcastGameState(String sessionId, GameStateDTO state) {
        try {
            logger.info("🔥 Broadcasting state - Players: {}", state.getPlayers().size());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId + "/state", state);
            logger.info("✅ Broadcast successful");
        } catch (Exception e) {
            logger.error("❌ Broadcast failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcasts player killed event to all clients in a session. Used for kill
     * feed and scoreboard updates.
     *
     * @param sessionId session identifier
     * @param event PlayerKilledDTO with kill details
     */
    public void broadcastPlayerKilled(String sessionId, PlayerKilledDTO event) {
        try {
            String destination = "/topic/game/" + sessionId + "/kill";
            messagingTemplate.convertAndSend(destination, event);

            logger.debug("Broadcasted player killed to session {} (killer: {}, victim: {})",
                    sessionId, event.getKillerId(), event.getVictimId());

        } catch (Exception e) {
            logger.error("Error broadcasting player killed to session {}: {}",
                    sessionId, e.getMessage(), e);
        }
    }

    /**
     * Broadcasts bomb exploded event to all clients in a session. Used for
     * explosion animations and sound effects.
     *
     * @param sessionId session identifier
     * @param event BombExplodedDTO with explosion details
     */
    public void broadcastBombExploded(String sessionId, BombExplodedDTO event) {
        try {
            String destination = "/topic/game/" + sessionId + "/explosion";
            messagingTemplate.convertAndSend(destination, event);

            logger.debug("Broadcasted bomb explosion to session {} (bomb: {}, tiles: {}, players: {})",
                    sessionId, event.getBombId(),
                    event.getAffectedTiles().size(),
                    event.getAffectedPlayers().size());

        } catch (Exception e) {
            logger.error("Error broadcasting bomb explosion to session {}: {}",
                    sessionId, e.getMessage(), e);
        }
    }

    /**
     * Broadcasts player disconnection notification. Notifies remaining players
     * that someone left the session.
     *
     * @param sessionId session identifier
     * @param playerId disconnected player identifier
     */
    private void broadcastPlayerDisconnected(String sessionId, String playerId) {
        try {
            String destination = "/topic/game/" + sessionId + "/disconnect";

            DisconnectNotification notification = DisconnectNotification.builder()
                    .playerId(playerId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend(destination, notification);

            logger.debug("Broadcasted player disconnect notification for {} to session {}",
                    playerId, sessionId);

        } catch (Exception e) {
            logger.error("Error broadcasting player disconnect for {} in session {}: {}",
                    playerId, sessionId, e.getMessage(), e);
        }
    }

    /**
     * Sends error message to a specific player. Used for validation errors and
     * failed actions.
     *
     * @param sessionId session identifier
     * @param playerId player identifier
     * @param errorCode error code identifier
     * @param message error message description
     */
    private void sendErrorToPlayer(String sessionId, String playerId,
            String errorCode, String message) {
        try {
            String destination = "/queue/errors";

            ErrorNotification error = ErrorNotification.builder()
                    .errorCode(errorCode)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSendToUser(playerId, destination, error);

            logger.debug("Sent error {} to player {} in session {}: {}",
                    errorCode, playerId, sessionId, message);

        } catch (Exception e) {
            logger.error("Error sending error notification to player {} in session {}: {}",
                    playerId, sessionId, e.getMessage(), e);
        }
    }

    /**
     * Internal DTO for disconnect notifications.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DisconnectNotification {

        private String playerId;
        private Long timestamp;
    }

    /**
     * Internal DTO for error notifications.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ErrorNotification {

        private String errorCode;
        private String message;
        private Long timestamp;
    }

    /**
     * Internal DTO for game start notifications.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class GameStartNotification {

        private String sessionId;
        private GameStateDTO initialState;
        private Long timestamp;
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headers.getDestination();

        if (destination != null && destination.contains("/topic/game/") && destination.endsWith("/state")) {
            String sessionId = destination.split("/topic/game/")[1].split("/state")[0];

            try {
                GameSession session = gameSessionService.getSession(sessionId);
                GameStateDTO state = session.getCurrentState();
                broadcastGameState(sessionId, state);
            } catch (Exception e) {
                logger.error("Error sending initial state", e);
            }
        }
    }
}
