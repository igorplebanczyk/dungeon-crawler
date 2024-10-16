package game;

import game.objects.GameObjectType;

import java.util.HashMap;
import java.util.Map;

public abstract class Constants {
    public static final int Y_OFFSET = 70;

    public static final int GAME_TILE_SIZE = 45; // Safe to modify; must always be a multiple of 15
    public static final int GAME_TILE_NUM = 15; // Amount of tiles in the x and y directions

    public static final int MAP_GRID_SIZE = 2; // Size of the grid which stores individual dungeon rooms

    public static final int PAUSE_TILE_SIZE = 150;
    public static final int PAUSE_WINDOW_WIDTH = 3;
    public static final int PAUSE_WINDOW_HEIGHT = 3;

    public static final int START_TILE_SIZE = 200;
    public static final int START_WINDOW_WIDTH = 4;
    public static final int START_WINDOW_HEIGHT = 4;

    public static final Map<GameObjectType, String> OBJECT_IMAGE_MAP;
    static {
        OBJECT_IMAGE_MAP = new HashMap<>();
        OBJECT_IMAGE_MAP.put(GameObjectType.DOOR, "/images/door.png");
        OBJECT_IMAGE_MAP.put(GameObjectType.EXIT, "/images/ciri.png");
        OBJECT_IMAGE_MAP.put(GameObjectType.FLOOR, "/images/floor.png");
        OBJECT_IMAGE_MAP.put(GameObjectType.WALL, "/images/wall.png");
    }

    public static final int IMAGE_CACHE_THREAD_NUM = 5;
}
