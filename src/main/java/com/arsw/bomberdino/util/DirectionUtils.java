package com.arsw.bomberdino.util;

import com.arsw.bomberdino.model.enums.Direction;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for direction-based operations. Provides helper methods for movement and adjacency
 * calculations.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class DirectionUtils {

    private DirectionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets all four cardinal directions.
     *
     * @return array of Direction enum values [UP, DOWN, LEFT, RIGHT]
     */
    public static Direction[] getCardinalDirections() {
        return new Direction[] {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
    }

    /**
     * Gets all adjacent points in cardinal directions.
     *
     * @param center center point
     * @return list of adjacent points (up to 4)
     */
    public static List<Point> getAdjacentPoints(Point center) {
        if (center == null) {
            throw new IllegalArgumentException("Center point cannot be null");
        }

        List<Point> adjacent = new ArrayList<>();

        for (Direction direction : getCardinalDirections()) {
            Point neighbor = direction.applyTo(center.x, center.y);
            if (neighbor != null) {
                adjacent.add(neighbor);
            }
        }

        return adjacent;
    }

    /**
     * Gets adjacent points within map bounds.
     *
     * @param center center point
     * @param width map width
     * @param height map height
     * @return list of valid adjacent points
     */
    public static List<Point> getAdjacentPointsInBounds(Point center, int width, int height) {
        List<Point> adjacent = getAdjacentPoints(center);

        return adjacent.stream().filter(point -> PointMapper.isInBounds(point, width, height))
                .toList();
    }

    /**
     * Gets direction from one point to another. Only works for points that differ by exactly 1 in
     * one axis.
     *
     * @param from starting point
     * @param to target point
     * @return Direction if valid, null if points are not adjacent
     */
    public static Direction getDirectionBetween(Point from, Point to) {
        if (from == null || to == null) {
            return null;
        }

        int dx = to.x - from.x;
        int dy = to.y - from.y;

        if (dx == 0 && dy == -1)
            return Direction.UP;
        if (dx == 0 && dy == 1)
            return Direction.DOWN;
        if (dx == -1 && dy == 0)
            return Direction.LEFT;
        if (dx == 1 && dy == 0)
            return Direction.RIGHT;

        return null;
    }

    /**
     * Gets opposite direction.
     *
     * @param direction original direction
     * @return opposite direction
     */
    public static Direction getOpposite(Direction direction) {
        if (direction == null) {
            return null;
        }

        return switch (direction) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }
}
