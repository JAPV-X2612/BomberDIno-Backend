package com.arsw.bomberdino.model.enums;

import lombok.Getter;

/**
 * Types of tiles that compose the game map.
 * Defines traversability, destructibility, and spawning rules.
 * 
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum TileType {
    
    EMPTY("Empty", true, false),
    SOLID_WALL("Solid Wall", false, false),
    DESTRUCTIBLE_WALL("Destructible Wall", false, true),
    SPAWN_POINT("Spawn Point", true, false);
    
    private final String displayName;
    private final boolean walkable;
    private final boolean destructible;
    
    TileType(String displayName, boolean walkable, boolean destructible) {
        this.displayName = displayName;
        this.walkable = walkable;
        this.destructible = destructible;
    }
    
    /**
     * Checks if players can walk on this tile type.
     * @return true if tile is walkable
     */
    public boolean isWalkable() {
        return walkable;
    }
    
    /**
     * Checks if this tile can be destroyed by explosions.
     * @return true if tile is destructible
     */
    public boolean isDestructible() {
        return destructible;
    }
    
    /**
     * Checks if power-ups can spawn on this tile type. 
     * @return true if tile type is EMPTY
     */
    public boolean canSpawnPowerUp() {
        return this == EMPTY;
    }
    
    /**
     * Checks if explosions can propagate through this tile.
     * @return true if tile is not a solid wall
     */
    public boolean allowsExplosion() {
        return this != SOLID_WALL;
    }
}
