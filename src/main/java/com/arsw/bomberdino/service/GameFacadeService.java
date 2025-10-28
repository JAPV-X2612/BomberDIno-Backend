package com.arsw.bomberdino.service;

import com.arsw.bomberdino.model.dto.response.GameStateUpdateDTO;
import com.arsw.bomberdino.model.dto.response.PowerUpEffect;
import com.arsw.bomberdino.model.entity.Bomb;
import com.arsw.bomberdino.model.entity.GameSession;
import com.arsw.bomberdino.model.entity.Player;
import com.arsw.bomberdino.model.entity.PowerUp;
import com.arsw.bomberdino.model.enums.Direction;
import com.arsw.bomberdino.model.enums.PlayerStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.arsw.bomberdino.exception.*;

import java.awt.Point;
import java.util.List;

/**
 * Facade service orchestrating all game operations.
 * Single entry point for WebSocket controllers handling player actions.
 * Coordinates between multiple services and publishes domain events.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Service
public class GameFacadeService {

    private final GameSessionService gameSessionService;
    private final PlayerService playerService;
    private final BombService bombService;
    private final PowerUpService powerUpService;
    private final CollisionService collisionService;
    private final TileService tileService;
    private final ApplicationEventPublisher eventPublisher;

    public GameFacadeService(
            GameSessionService gameSessionService,
            PlayerService playerService,
            BombService bombService,
            PowerUpService powerUpService,
            CollisionService collisionService,
            TileService tileService,
            ApplicationEventPublisher eventPublisher) {
        this.gameSessionService = gameSessionService;
        this.playerService = playerService;
        this.bombService = bombService;
        this.powerUpService = powerUpService;
        this.collisionService = collisionService;
        this.tileService = tileService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles player movement request.
     * Validates movement, updates tile occupation, and publishes state change
     * event.
     *
     * @param sessionId unique identifier of the game session
     * @param playerId  unique identifier of the player
     * @param direction direction to move (UP, DOWN, LEFT, RIGHT)
     * @return GameStateUpdateDTO with updated game state
     * @throws ValidationException      if parameters are null or blank
     * @throws PlayerNotFoundException  if player is not found in session
     * @throws SessionNotFoundException if session is not found
     * @throws InvalidMoveException     if movement is invalid
     */
    public GameStateUpdateDTO handlePlayerMove(String sessionId, String playerId, Direction direction) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);
        validateDirection(direction);

        GameSession session = gameSessionService.getSession(sessionId);
        Player player = findPlayerInSession(session, playerId);

        if (player.getStatus() != PlayerStatus.ALIVE) {
            throw new InvalidMoveException(playerId,
                    "Player cannot move in current status: " + player.getStatus());
        }

        Point currentPosition = new Point(player.getPosX(), player.getPosY());
        Point newPosition = direction.applyTo(player.getPosX(), player.getPosY());

        if (!collisionService.canMoveTo(sessionId, newPosition)) {
            throw new InvalidMoveException(playerId, newPosition, "Destination tile is not walkable or is occupied");
        }

        tileService.releaseOccupation(sessionId, currentPosition);

        boolean occupationSuccess = tileService.tryOccupy(sessionId, newPosition, false);
        if (!occupationSuccess) {
            tileService.tryOccupy(sessionId, currentPosition, false);
            throw new InvalidMoveException(playerId, newPosition, "Failed to occupy destination tile");
        }

        player.setPosX(newPosition.x);
        player.setPosY(newPosition.y);

        PowerUp collectedPowerUp = detectAndCollectPowerUp(session, player, newPosition);
        if (collectedPowerUp != null) {
            handlePowerUpCollection(sessionId, playerId, collectedPowerUp.getId().toString());
        }

        publishGameStateChangedEvent(sessionId);

        return getGameState(sessionId);
    }

    /**
     * Handles bomb placement request.
     * Validates placement, marks tile, schedules explosion, and publishes state
     * change.
     *
     * @param sessionId unique identifier of the game session
     * @param playerId  unique identifier of the player
     * @param position  coordinates where bomb should be placed
     * @return GameStateUpdateDTO with updated game state
     * @throws ValidationException      if parameters are null or blank
     * @throws PlayerNotFoundException  if the player does not exist
     * @throws SessionNotFoundException if the session does not exist
     * @throws BombPlacementException   if bomb placement fails
     */
    public GameStateUpdateDTO handlePlaceBomb(String sessionId, String playerId, Point position) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);
        validatePosition(position);

        GameSession session = gameSessionService.getSession(sessionId);
        Player player = findPlayerInSession(session, playerId);

        if (player.getStatus() != PlayerStatus.ALIVE) {
            throw new BombPlacementException(playerId, position, "Dead players cannot place bombs");
        }

        Point playerPosition = new Point(player.getPosX(), player.getPosY());
        if (!playerPosition.equals(position)) {
            throw new BombPlacementException(playerId, position, "Bomb must be placed at player's current position");
        }

        if (tileService.getTile(sessionId, position).hasBomb()) {
            throw new BombPlacementException(playerId, position, "Tile already has a bomb");
        }

        Bomb bomb = bombService.placeBomb(sessionId, playerId, position);
        if (bomb == null) {
            throw new BombPlacementException(playerId, position, "Failed to place bomb");
        }

        tileService.markBomb(sessionId, position, true);
        session.getActiveBombs().add(bomb);

        scheduleBombExplosion(sessionId, bomb);

        publishGameStateChangedEvent(sessionId);

        return getGameState(sessionId);
    }

    /**
     * Handles power-up collection request.
     * Validates collection, applies effect to player, removes power-up, and
     * publishes events.
     *
     * @param sessionId unique identifier of the game session
     * @param playerId  unique identifier of the player
     * @param powerUpId unique identifier of the power-up to collect
     * @return GameStateUpdateDTO with updated game state
     * @throws ValidationException      if parameters are null or blank
     * @throws PlayerNotFoundException  if the player does not exist
     * @throws SessionNotFoundException if the session does not exist
     * @throws PowerUpNotFoundException if the power-up does not exist or has
     *                                  expired
     */
    public GameStateUpdateDTO handlePowerUpCollection(String sessionId, String playerId, String powerUpId) {
        validateSessionId(sessionId);
        validatePlayerId(playerId);
        validatePowerUpId(powerUpId);

        GameSession session = gameSessionService.getSession(sessionId);
        Player player = findPlayerInSession(session, playerId);

        PowerUp powerUp = session.getAvailablePowerUps().stream()
                .filter(pu -> pu.getId().toString().equals(powerUpId))
                .findFirst()
                .orElseThrow(() -> new PowerUpNotFoundException(powerUpId, sessionId));

        if (powerUp.isExpired()) {
            throw new PowerUpNotFoundException(powerUpId, sessionId, "Power-up has expired");
        }

        Point powerUpPosition = new Point(powerUp.getPosX(), powerUp.getPosY());
        Point playerPosition = new Point(player.getPosX(), player.getPosY());

        // TODO: Player doesn't need to be exactly on power-up position to collect
        if (!powerUpPosition.equals(playerPosition)) {
            throw new InvalidMoveException(playerId, powerUpPosition, "Player must be at power-up position to collect");
        }

        PowerUpEffect effect = powerUpService.applyPowerUpEffect(playerId, powerUpId);
        playerService.applyPowerUpEffect(playerId, effect);

        session.getAvailablePowerUps().remove(powerUp);
        tileService.releaseOccupation(sessionId, powerUpPosition);

        publishPowerUpCollectedEvent(sessionId, playerId, powerUpId, effect);
        publishGameStateChangedEvent(sessionId);

        return getGameState(sessionId);
    }

    /**
     * Retrieves current game state for a session.
     * Creates GameStateUpdateDTO with all active entities.
     *
     * @param sessionId unique identifier of the game session
     * @return GameStateUpdateDTO containing complete game state
     * @throws ValidationException      if sessionId is null or blank
     * @throws SessionNotFoundException if the session does not exist
     */
    public GameStateUpdateDTO getGameState(String sessionId) {
        validateSessionId(sessionId);

        GameSession session = gameSessionService.getSession(sessionId);

        GameStateUpdateDTO state = GameStateUpdateDTO.builder()
                .sessionId(sessionId)
                .players(session.getPlayers().stream()
                        .map(this::mapPlayerToDTO)
                        .toList())
                .bombs(session.getActiveBombs().stream()
                        .map(this::mapBombToDTO)
                        .toList())
                .powerUps(session.getAvailablePowerUps().stream()
                        .map(this::mapPowerUpToDTO)
                        .toList())
                .timestamp(System.currentTimeMillis())
                .build();

        return state;
    }

    /**
     * Schedules bomb explosion and handles explosion logic.
     *
     * @param sessionId session identifier
     * @param bomb      bomb to schedule
     */
    private void scheduleBombExplosion(String sessionId, Bomb bomb) {
        String bombId = bomb.getId().toString();
        long delay = bomb.getExplosionDelay();

        bombService.scheduleBombExplosion(bombId, delay);

    }

    /**
     * Processes bomb explosion and handles damage to players.
     *
     * @param sessionId session identifier
     * @param bomb      exploding bomb
     */
    private void processBombExplosion(String sessionId, Bomb bomb) {
        String bombId = bomb.getId().toString();

        List<Point> affectedTiles = bombService.explodeBomb(bombId);

        Point bombPosition = new Point(bomb.getPosX(), bomb.getPosY());
        tileService.markBomb(sessionId, bombPosition, false);

        GameSession session = gameSessionService.getSession(sessionId);

        List<Player> affectedPlayers = session.getPlayers().stream()
                .filter(player -> {
                    Point playerPos = new Point(player.getPosX(), player.getPosY());
                    return affectedTiles.contains(playerPos);
                })
                .toList();

        for (Player player : affectedPlayers) {
            if (!player.hasActiveShield()) {
                player.takeDamage(1);

                if (!player.isAlive()) {
                    handlePlayerDeath(sessionId, null, player.getId().toString());
                }
            }
        }

        session.getActiveBombs().remove(bomb);

        List<String> affectedPlayerIds = affectedPlayers.stream()
                .map(p -> p.getId().toString())
                .toList();

        publishBombExplodedEvent(sessionId, bombId, affectedTiles, affectedPlayerIds);
        publishGameStateChangedEvent(sessionId);
    }

    /**
     * Handles player death and updates kill/death counters.
     *
     * @param sessionId session identifier
     * @param killerId  killer player ID (nullable)
     * @param victimId  victim player ID
     */
    private void handlePlayerDeath(String sessionId, String killerId, String victimId) {
        playerService.incrementDeaths(victimId);

        if (killerId != null && !killerId.equals(victimId)) {
            playerService.incrementKills(killerId);
        }

        publishPlayerKilledEvent(sessionId, killerId, victimId);
    }

    /**
     * Detects power-up at player's position.
     *
     * @param session  game session
     * @param player   player to check
     * @param position position to check
     * @return PowerUp if found, null otherwise
     */
    private PowerUp detectAndCollectPowerUp(GameSession session, Player player, Point position) {
        return session.getAvailablePowerUps().stream()
                .filter(pu -> pu.getPosX() == position.x && pu.getPosY() == position.y)
                .filter(pu -> !pu.isExpired())
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds player in session by ID.
     *
     * @param session  game session
     * @param playerId player identifier
     * @return Player instance
     * @throws PlayerNotFoundException if player not found in session
     */
    private Player findPlayerInSession(GameSession session, String playerId) {
        return session.getPlayers().stream()
                .filter(p -> p.getId().toString().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(playerId, session.getSessionId().toString()));
    }

    /**
     * Maps Player entity to PlayerDTO.
     */
    private com.arsw.bomberdino.model.dto.response.PlayerDTO mapPlayerToDTO(Player player) {
        return com.arsw.bomberdino.model.dto.response.PlayerDTO.builder()
                .id(player.getId().toString())
                .username(player.getUsername())
                .posX(player.getPosX())
                .posY(player.getPosY())
                .lifeCount(player.getLifeCount())
                .status(player.getStatus())
                .kills(player.getKills())
                .deaths(player.getDeaths())
                .hasShield(player.hasActiveShield())
                .build();
    }

    /**
     * Maps Bomb entity to BombDTO.
     */
    private com.arsw.bomberdino.model.dto.response.BombDTO mapBombToDTO(Bomb bomb) {
        return com.arsw.bomberdino.model.dto.response.BombDTO.builder()
                .id(bomb.getId().toString())
                .ownerId(bomb.getId().toString())
                .posX(bomb.getPosX())
                .posY(bomb.getPosY())
                .range(bomb.getRange())
                .timeToExplode(bomb.getTimeUntilExplosion())
                .build();
    }

    /**
     * Maps PowerUp entity to PowerUpDTO.
     */
    private com.arsw.bomberdino.model.dto.response.PowerUpDTO mapPowerUpToDTO(PowerUp powerUp) {
        return com.arsw.bomberdino.model.dto.response.PowerUpDTO.builder()
                .id(powerUp.getId().toString())
                .type(powerUp.getType())
                .posX(powerUp.getPosX())
                .posY(powerUp.getPosY())
                .build();
    }

    /**
     * Publishes GameStateChangedEvent.
     */
    private void publishGameStateChangedEvent(String sessionId) {
        // Event implementation will be added later
    }

    /**
     * Publishes PlayerKilledEvent.
     */
    private void publishPlayerKilledEvent(String sessionId, String killerId, String victimId) {
        // Event implementation will be added later
    }

    /**
     * Publishes BombExplodedEvent.
     */
    private void publishBombExplodedEvent(String sessionId, String bombId,
            List<Point> affectedTiles, List<String> affectedPlayers) {
        // Event implementation will be added later
    }

    /**
     * Publishes PowerUpCollectedEvent.
     */
    private void publishPowerUpCollectedEvent(String sessionId, String playerId,
            String powerUpId, PowerUpEffect effect) {
        // Event implementation will be added later
    }

    /**
     * Validates the session ID parameter.
     *
     * @param sessionId the session ID to validate
     * @throws ValidationException if sessionId is null or blank
     */
    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ValidationException("Session ID cannot be null or blank", "sessionId");
        }
    }

    /**
     * Validates the player ID parameter.
     *
     * @param playerId the player ID to validate
     * @throws ValidationException if playerId is null or blank
     */
    private void validatePlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new ValidationException("Player ID cannot be null or blank", "playerId");
        }
    }

    /**
     * Validates the power-up ID parameter.
     *
     * @param powerUpId the power-up ID to validate
     * @throws ValidationException if powerUpId is null or blank
     */
    private void validatePowerUpId(String powerUpId) {
        if (powerUpId == null || powerUpId.isBlank()) {
            throw new ValidationException("Power-up ID cannot be null or blank", "powerUpId");
        }
    }

    /**
     * Validates the direction parameter.
     *
     * @param direction the direction to validate
     * @throws ValidationException if direction is null
     */
    private void validateDirection(Direction direction) {
        if (direction == null) {
            throw new ValidationException("Direction cannot be null", "direction");
        }
    }

    /**
     * Validates the position parameter.
     *
     * @param position the position to validate
     * @throws ValidationException if position is null
     */
    private void validatePosition(Point position) {
        if (position == null) {
            throw new ValidationException("Position cannot be null", "position");
        }
    }
}
