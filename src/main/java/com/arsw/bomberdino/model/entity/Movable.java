package com.arsw.bomberdino.model.entity;

import com.arsw.bomberdino.model.enums.Direction;

/**
 * Interface for entities that can move on the game grid. Defines movement capabilities and
 * collision validation.
 *
 * @author Mapunix, Rivaceraptos, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public interface Movable {

    /**
     * Moves the entity in the specified direction. Updates entity position if movement is valid.
     *
     * @param direction the direction to move (UP, DOWN, LEFT, RIGHT)
     * @throws IllegalArgumentException if direction is null
     * @throws IllegalStateException if movement is blocked
     */
    void move(Direction direction);

    /**
     * Checks if the entity can move to the specified position. Validates tile availability and
     * collision rules.
     *
     * @param posX target X coordinate
     * @param posY target Y coordinate
     * @return true if position is accessible, false otherwise
     */
    boolean canMoveTo(int posX, int posY);

    /**
     * Gets the current movement speed of the entity. Speed determines tiles moved per action.
     *
     * @return speed value (tiles per move)
     */
    int getSpeed();
}
