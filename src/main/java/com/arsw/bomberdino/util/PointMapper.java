package com.arsw.bomberdino.util;

import com.arsw.bomberdino.model.dto.response.PointDTO;

import java.awt.Point;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for converting between java.awt.Point and PointDTO. Centralizes point conversion
 * logic across the application.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class PointMapper {

    private PointMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts java.awt.Point to PointDTO.
     *
     * @param point java.awt.Point to convert
     * @return PointDTO instance, or null if input is null
     */
    public static PointDTO toDTO(Point point) {
        if (point == null) {
            return null;
        }

        return PointDTO.builder().x(point.x).y(point.y).build();
    }

    /**
     * Converts PointDTO to java.awt.Point.
     *
     * @param dto PointDTO to convert
     * @return java.awt.Point instance, or null if input is null
     */
    public static Point toPoint(PointDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Point(dto.getX(), dto.getY());
    }

    /**
     * Converts list of Points to list of PointDTOs.
     *
     * @param points list of java.awt.Point instances
     * @return list of PointDTO instances
     */
    public static List<PointDTO> toDTOList(List<Point> points) {
        if (points == null) {
            return null;
        }

        return points.stream().map(PointMapper::toDTO).toList();
    }

    /**
     * Converts list of PointDTOs to list of Points.
     *
     * @param dtos list of PointDTO instances
     * @return list of java.awt.Point instances
     */
    public static List<Point> toPointList(List<PointDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream().map(PointMapper::toPoint).toList();
    }

    /**
     * Checks if two points are equal.
     *
     * @param p1 first point
     * @param p2 second point
     * @return true if both points have same coordinates
     */
    public static boolean areEqual(Point p1, Point p2) {
        if (p1 == null || p2 == null) {
            return p1 == p2;
        }

        return p1.x == p2.x && p1.y == p2.y;
    }

    /**
     * Calculates Manhattan distance between two points.
     *
     * @param p1 first point
     * @param p2 second point
     * @return Manhattan distance (|x1-x2| + |y1-y2|)
     */
    public static int manhattanDistance(Point p1, Point p2) {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Points cannot be null");
        }

        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    /**
     * Checks if a point is within rectangular bounds.
     *
     * @param point point to check
     * @param width maximum X coordinate (exclusive)
     * @param height maximum Y coordinate (exclusive)
     * @return true if point is within bounds
     */
    public static boolean isInBounds(Point point, int width, int height) {
        if (point == null) {
            return false;
        }

        return point.x >= 0 && point.x < width && point.y >= 0 && point.y < height;
    }
}
