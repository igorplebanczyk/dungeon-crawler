package game;

import game.object.entity.EntityType;
import game.object.entity.PlayerCharacter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Constants {
    public static final int Y_OFFSET = 8;

    public static final int GAME_TILE_SIZE = 30; // Safe to modify; must always be a multiple of 15
    public static final int GAME_TILE_NUM = 29; // Amount of tiles in the x and y directions
    public static final int GAME_AUTO_MOVEMENT_DELAY = 150; // in milliseconds

    public static final int MAP_GRID_SIZE = 3; // Size of the grid which stores individual dungeon rooms
    public static final double DUNGEON_TARGET_COUNT_LOW = 0.4;
    public static final double DUNGEON_TARGET_COUNT_HIGH = 0.8;

    public static final int PAUSE_TILE_SIZE = 150;
    public static final int PAUSE_WINDOW_WIDTH = 3;
    public static final int PAUSE_WINDOW_HEIGHT = 3;

    public static final int START_TILE_SIZE = 200;
    public static final int START_WINDOW_WIDTH = 4;
    public static final int START_WINDOW_HEIGHT = 4;

    public static final int IMAGE_CACHE_THREAD_NUM = 5;

    public static final Font MESSAGE_FONT = new Font("Times", Font.BOLD, 36);
    public static final Color MESSAGE_COLOR = Color.WHITE;

    public static final Map<EntityType, String> OBJECT_IMAGE_MAP;
    public static final Map<PlayerCharacter, String> PLAYER_IMAGE_MAP;

    static {
        OBJECT_IMAGE_MAP = new HashMap<>();
        OBJECT_IMAGE_MAP.put(EntityType.DOOR, "/images/door.png");
        OBJECT_IMAGE_MAP.put(EntityType.EXIT, "/images/ciri.png");
        OBJECT_IMAGE_MAP.put(EntityType.FLOOR, "/images/floor.png");
        OBJECT_IMAGE_MAP.put(EntityType.WALL, "/images/wall.png");
    }

    static {
        PLAYER_IMAGE_MAP = new HashMap<>();
        PLAYER_IMAGE_MAP.put(PlayerCharacter.GERALT, "/images/geralt.png");
        PLAYER_IMAGE_MAP.put(PlayerCharacter.YENNEFER, "/images/yen.png");
    }
}
