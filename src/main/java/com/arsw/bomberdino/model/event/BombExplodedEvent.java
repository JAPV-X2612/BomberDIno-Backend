package com.arsw.bomberdino.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.Point;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain event published when a bomb explodes.
 * Contains explosion range and affected entities for client-side effects.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BombExplodedEvent {

    private String sessionId;
    private String bombId;

    @Builder.Default
    private List<Point> affectedTiles = new ArrayList<>();

    @Builder.Default
    private List<String> affectedPlayers = new ArrayList<>();

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Checks if the explosion affected any players.
     * @return true if at least one player was affected
     */
    public boolean hasAffectedPlayers() {
        return affectedPlayers != null && !affectedPlayers.isEmpty();
    }

    /**
     * Gets the number of tiles affected by the explosion.
     * @return count of affected tiles
     */
    public int getAffectedTilesCount() {
        return affectedTiles != null ? affectedTiles.size() : 0;
    }

    /**
     * Creates a new BombExplodedEvent with current timestamp.
     *
     * @param sessionId       session identifier
     * @param bombId          bomb identifier
     * @param affectedTiles   list of affected tile positions
     * @param affectedPlayers list of affected player identifiers
     * @return BombExplodedEvent instance
     */
    public static BombExplodedEvent of(String sessionId, String bombId,
            List<Point> affectedTiles, List<String> affectedPlayers) {
        return BombExplodedEvent.builder()
                .sessionId(sessionId)
                .bombId(bombId)
                .affectedTiles(affectedTiles != null ? affectedTiles : new ArrayList<>())
                .affectedPlayers(affectedPlayers != null ? affectedPlayers : new ArrayList<>())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
