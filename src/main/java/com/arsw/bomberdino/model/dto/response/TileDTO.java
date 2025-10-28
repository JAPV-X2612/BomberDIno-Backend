package com.arsw.bomberdino.model.dto.response;

import com.arsw.bomberdino.model.enums.TileType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable tile snapshot for client rendering.
 * Only contains data required by the frontend to repaint the map.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
@Builder
@AllArgsConstructor
public final class TileDTO {

  @Min(value = 0, message = "X coordinate must be non-negative")
  private final int x;

  @Min(value = 0, message = "Y coordinate must be non-negative")
  private final int y;

  @NotNull(message = "Tile type cannot be null")
  private final TileType type;
}
