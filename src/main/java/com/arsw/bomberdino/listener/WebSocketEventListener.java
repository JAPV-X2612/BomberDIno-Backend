package com.arsw.bomberdino.listener;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.arsw.bomberdino.model.dto.response.BombExplodedDTO;
import com.arsw.bomberdino.model.dto.response.GameStateDTO;
import com.arsw.bomberdino.model.dto.response.PlayerKilledDTO;
import com.arsw.bomberdino.model.dto.response.PointDTO;
import com.arsw.bomberdino.model.event.BombExplodedEvent;
import com.arsw.bomberdino.model.event.GameStateChangedEvent;
import com.arsw.bomberdino.model.event.PlayerKilledEvent;
import com.arsw.bomberdino.model.event.PowerUpCollectedEvent;
import com.arsw.bomberdino.service.impl.GameFacadeService;

/**
 * Event listener for domain events that require WebSocket broadcasting. Listens
 * to game events and broadcasts updates to connected clients. All methods are
 * async to prevent blocking the event publisher.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final GameFacadeService gameFacadeService;

    private static final String GAME_TOPIC_PREFIX = "/topic/game/";
    private static final String STATE_SUFFIX = "/state";
    private static final String KILL_SUFFIX = "/kill";
    private static final String EXPLOSION_SUFFIX = "/explosion";
    private static final String POWERUP_SUFFIX = "/powerup";

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate,
            GameFacadeService gameFacadeService) {
        this.messagingTemplate = messagingTemplate;
        this.gameFacadeService = gameFacadeService;
    }

    /**
     * Handles GameStateChangedEvent by broadcasting full game state. Sends
     * GameStateDTO to all clients subscribed to the session topic.
     *
     * @param event GameStateChangedEvent containing session ID
     */
    @EventListener
    @Async
    public void onGameStateChanged(GameStateChangedEvent event) {
        try {
            String sessionId = event.getSessionId();
            logger.debug("Processing GameStateChangedEvent for session: {}", sessionId);

            GameStateDTO gameState = gameFacadeService.getGameState(sessionId);

            String destination = GAME_TOPIC_PREFIX + sessionId + STATE_SUFFIX;
            messagingTemplate.convertAndSend(destination, gameState);

            logger.info("Broadcasted game state to {} with {} players, {} bombs, {} power-ups",
                    destination,
                    gameState.getPlayers().size(),
                    gameState.getBombs().size(),
                    gameState.getPowerUps().size());

        } catch (Exception e) {
            logger.error("Error broadcasting game state for session {}: {}",
                    event.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Handles PlayerKilledEvent by broadcasting kill notification. Sends
     * PlayerKilledDTO to all clients for UI kill feed updates.
     *
     * @param event PlayerKilledEvent containing killer and victim IDs
     */
    @EventListener
    @Async
    public void onPlayerKilled(PlayerKilledEvent event) {
        try {
            String sessionId = event.getSessionId();
            logger.debug("Processing PlayerKilledEvent for session: {} (killer: {}, victim: {})",
                    sessionId, event.getKillerId(), event.getVictimId());

            PlayerKilledDTO dto = PlayerKilledDTO.builder()
                    .sessionId(sessionId)
                    .killerId(event.getKillerId())
                    .victimId(event.getVictimId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            String destination = GAME_TOPIC_PREFIX + sessionId + KILL_SUFFIX;
            messagingTemplate.convertAndSend(destination, dto);

            logger.info("Broadcasted player kill to {} (killer: {}, victim: {})",
                    destination, event.getKillerId(), event.getVictimId());

            onGameStateChanged(GameStateChangedEvent.of(sessionId));

        } catch (Exception e) {
            logger.error("Error broadcasting player killed event for session {}: {}",
                    event.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Handles BombExplodedEvent by broadcasting explosion details. Sends
     * BombExplodedDTO with affected tiles for client-side animation.
     *
     * @param event BombExplodedEvent containing explosion data
     */
    @EventListener
    @Async
    public void onBombExploded(BombExplodedEvent event) {
        try {
            String sessionId = event.getSessionId();
            logger.debug("Processing BombExplodedEvent for session: {} (bomb: {}, tiles: {}, players: {})",
                    sessionId, event.getBombId(),
                    event.getAffectedTilesCount(),
                    event.getAffectedPlayers().size());

            BombExplodedDTO dto = BombExplodedDTO.builder()
                    .sessionId(sessionId)
                    .bombId(event.getBombId())
                    .affectedTiles(event.getAffectedTiles().stream()
                            .map(point -> PointDTO.builder()
                            .x(point.x)
                            .y(point.y)
                            .build())
                            .collect(Collectors.toList()))
                    .affectedPlayers(event.getAffectedPlayers())
                    .timestamp(System.currentTimeMillis())
                    .build();

            String destination = GAME_TOPIC_PREFIX + sessionId + EXPLOSION_SUFFIX;
            messagingTemplate.convertAndSend(destination, dto);

            logger.info("Broadcasted bomb explosion to {} (bomb: {}, affected tiles: {}, affected players: {})",
                    destination, event.getBombId(),
                    event.getAffectedTilesCount(),
                    event.getAffectedPlayers().size());

            onGameStateChanged(GameStateChangedEvent.of(sessionId));

        } catch (Exception e) {
            logger.error("Error broadcasting bomb exploded event for session {}: {}",
                    event.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Handles PowerUpCollectedEvent by broadcasting collection notification.
     * Sends power-up effect details to clients for UI feedback.
     *
     * @param event PowerUpCollectedEvent containing player and power-up data
     */
    @EventListener
    @Async
    public void onPowerUpCollected(PowerUpCollectedEvent event) {
        try {
            String sessionId = event.getSessionId();
            logger.debug("Processing PowerUpCollectedEvent for session: {} (player: {}, powerUp: {}, type: {})",
                    sessionId, event.getPlayerId(), event.getPowerUpId(), event.getPowerUpType());

            String destination = GAME_TOPIC_PREFIX + sessionId + POWERUP_SUFFIX;
            messagingTemplate.convertAndSend(destination, event);

            logger.info("Broadcasted power-up collection to {} (player: {}, type: {})",
                    destination, event.getPlayerId(), event.getPowerUpType());

            onGameStateChanged(GameStateChangedEvent.of(sessionId));

        } catch (Exception e) {
            logger.error("Error broadcasting power-up collected event for session {}: {}",
                    event.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Broadcasts a custom message to a session topic. Utility method for ad-hoc
     * notifications.
     *
     * @param sessionId session identifier
     * @param suffix topic suffix (e.g., "/notification")
     * @param payload message payload
     */
    public void broadcastToSession(String sessionId, String suffix, Object payload) {
        try {
            String destination = GAME_TOPIC_PREFIX + sessionId + suffix;
            messagingTemplate.convertAndSend(destination, payload);

            logger.debug("Broadcasted custom message to {}", destination);

        } catch (Exception e) {
            logger.error("Error broadcasting custom message to session {}: {}",
                    sessionId, e.getMessage(), e);
        }
    }
}
