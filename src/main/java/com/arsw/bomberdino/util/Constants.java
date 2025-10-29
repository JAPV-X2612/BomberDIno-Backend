package com.arsw.bomberdino.util;

/**
 * Application-wide constants.
 *
 * @author Mapunix, Rivaceratops, Yisus-Rex
 * @version 1.0
 * @since 2025-10-26
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final class Game {
        private Game() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final int DEFAULT_MAP_WIDTH = 15;
        public static final int DEFAULT_MAP_HEIGHT = 13;
        public static final int DEFAULT_MAX_PLAYERS = 4;
        public static final int DEFAULT_ROUND_DURATION = 180;
        public static final int DEFAULT_PLAYER_LIVES = 3;
        public static final int DEFAULT_BOMB_COUNT = 1;
        public static final int DEFAULT_BOMB_RANGE = 2;
        public static final int DEFAULT_PLAYER_SPEED = 1;
    }

    public static final class Bomb {
        private Bomb() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final long DEFAULT_EXPLOSION_DELAY_MS = 3000L;
        public static final int DEFAULT_DAMAGE = 1;
    }

    public static final class PowerUp {
        private PowerUp() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final long DEFAULT_DURATION_MS = 30000L;
        public static final double DROP_RATE = 0.3;
    }

    public static final class WebSocket {
        private WebSocket() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String GAME_TOPIC_PREFIX = "/topic/game/";
        public static final String STATE_SUFFIX = "/state";
        public static final String KILL_SUFFIX = "/kill";
        public static final String EXPLOSION_SUFFIX = "/explosion";
        public static final String POWERUP_SUFFIX = "/powerup";
        public static final String DISCONNECT_SUFFIX = "/disconnect";
        public static final String ERROR_QUEUE = "/queue/errors";
    }

    public static final class API {
        private API() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static final String V1_PREFIX = "/api/v1";
        public static final String GAME_PATH = V1_PREFIX + "/game";
        public static final String ROOMS_PATH = GAME_PATH + "/rooms";
    }
}
