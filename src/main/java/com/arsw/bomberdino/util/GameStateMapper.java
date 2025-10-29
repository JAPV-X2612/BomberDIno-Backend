package com.arsw.bomberdino.util;

import com.arsw.bomberdino.model.dto.response.BombDTO;
import com.arsw.bomberdino.model.dto.response.PlayerDTO;
import com.arsw.bomberdino.model.dto.response.PowerUpDTO;
import com.arsw.bomberdino.model.entity.Bomb;
import com.arsw.bomberdino.model.entity.Player;
import com.arsw.bomberdino.model.entity.PowerUp;

import java.util.List;

/**
 * Mapper utility for converting game entities to DTOs. Centralizes entity-to-DTO conversion logic.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class GameStateMapper {

    private GameStateMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts Player entity to PlayerDTO.
     *
     * @param player Player entity
     * @return PlayerDTO instance
     */
    public static PlayerDTO toPlayerDTO(Player player) {
        if (player == null) {
            return null;
        }

        return PlayerDTO.builder().id(player.getId().toString()).username(player.getUsername())
                .posX(player.getPosX()).posY(player.getPosY()).lifeCount(player.getLifeCount())
                .status(player.getStatus()).kills(player.getKills()).deaths(player.getDeaths())
                .hasShield(player.hasActiveShield()).build();
    }

    /**
     * Converts Bomb entity to BombDTO.
     *
     * @param bomb Bomb entity
     * @return BombDTO instance
     */
    public static BombDTO toBombDTO(Bomb bomb) {
        if (bomb == null) {
            return null;
        }

        return BombDTO.builder().id(bomb.getId().toString()).ownerId(bomb.getId().toString())
                .posX(bomb.getPosX()).posY(bomb.getPosY()).range(bomb.getRange())
                .timeToExplode(bomb.getTimeUntilExplosion()).build();
    }

    /**
     * Converts PowerUp entity to PowerUpDTO.
     *
     * @param powerUp PowerUp entity
     * @return PowerUpDTO instance
     */
    public static PowerUpDTO toPowerUpDTO(PowerUp powerUp) {
        if (powerUp == null) {
            return null;
        }

        return PowerUpDTO.builder().id(powerUp.getId().toString()).type(powerUp.getType())
                .posX(powerUp.getPosX()).posY(powerUp.getPosY()).build();
    }

    /**
     * Converts list of Players to list of PlayerDTOs.
     *
     * @param players list of Player entities
     * @return list of PlayerDTO instances
     */
    public static List<PlayerDTO> toPlayerDTOList(List<Player> players) {
        if (players == null) {
            return null;
        }

        return players.stream().map(GameStateMapper::toPlayerDTO).toList();
    }

    /**
     * Converts list of Bombs to list of BombDTOs.
     *
     * @param bombs list of Bomb entities
     * @return list of BombDTO instances
     */
    public static List<BombDTO> toBombDTOList(List<Bomb> bombs) {
        if (bombs == null) {
            return null;
        }

        return bombs.stream().map(GameStateMapper::toBombDTO).toList();
    }

    /**
     * Converts list of PowerUps to list of PowerUpDTOs.
     *
     * @param powerUps list of PowerUp entities
     * @return list of PowerUpDTO instances
     */
    public static List<PowerUpDTO> toPowerUpDTOList(List<PowerUp> powerUps) {
        if (powerUps == null) {
            return null;
        }

        return powerUps.stream().map(GameStateMapper::toPowerUpDTO).toList();
    }
}
