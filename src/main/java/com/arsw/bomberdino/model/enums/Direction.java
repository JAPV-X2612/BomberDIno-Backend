package com.arsw.bomberdino.model.enums;

import java.awt.Point;

import lombok.Getter;

/**
 * Cardinal directions for entity movement on the game grid.
 * Uses computer graphics coordinate system where origin (0,0) is top-left.
 * Y-axis increases downward to match Phaser 3 rendering.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
@Getter
public enum Direction {

    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int deltaX;
    private final int deltaY;

    Direction(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    /**
     * Calculates new position after moving in this direction.
     *
     * @param currentX current X coordinate
     * @param currentY current Y coordinate
     * @return array with [newX, newY]
     */
    public Point applyTo(int currentX, int currentY) {
        return new Point(currentX + deltaX, currentY + deltaY);
    }

    /**
     * Gets the opposite direction.
     * 
     * @return opposite Direction
     */
    public Direction getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
