package game.object;

import game.object.entity.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Dungeon {
    private final Entity[][] map; // 2D array to represent the dungeon map
    private final Random random = new Random();

    private final int width;
    private final int height;
    private final int gridX;
    private final int gridY;
    private int exitX;
    private int exitY;
    private final List<Point> doorPositions = new ArrayList<>();

    public Dungeon(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.gridX = x;
        this.gridY = y;
        this.map = new Entity[height][width];

        int[] exit = getPossibleExitTile();
        this.exitX = exit[0];
        this.exitY = exit[1];

        generateDungeon();
    }

    // Select a random tile in the bottom right quadrant
    private int[] getPossibleExitTile() {
        int x = random.nextInt(width / 2) + width / 2;
        int y = random.nextInt(height / 2) + height / 2;
        return new int[]{x, y};
    }

    private void generateDungeon() {
        boolean allEdgeMiddlesReachable = false;
        while (!allEdgeMiddlesReachable) { // Keep generating until the exit and all doors are reachable
            generateDungeonStructure();
            ensureAdjacentWalls();
            fillInaccessibleAreasWithWalls();
            allEdgeMiddlesReachable = areExitsAndDoorsReachable();
        }
    }

    // Generate the dungeon walls and floors
    private void generateDungeonStructure() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (random.nextDouble() < 0.4) { // Increased chance for walls
                    map[y][x] = new StaticEntity(EntityType.WALL); // Wall
                } else {
                    map[y][x] = new StaticEntity(EntityType.FLOOR); // Floor
                }
            }
        }
    }

    private void ensureAdjacentWalls() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (map[y][x].getType() == EntityType.WALL) { // If current tile is wall
                    // Check surrounding tiles
                    if (map[y - 1][x].getType() == EntityType.FLOOR && map[y][x - 1].getType() == EntityType.FLOOR &&
                            map[y + 1][x].getType() == EntityType.FLOOR && map[y][x + 1].getType() == EntityType.FLOOR) {
                        // If no adjacent walls, make one adjacent wall
                        int direction = random.nextInt(4); // 0: Up, 1: Left, 2: Down, 3: Right
                        switch (direction) {
                            case 0:
                                setTile(x, y - 1, new StaticEntity(EntityType.WALL));
                                break;
                            case 1:
                                setTile(x - 1, y, new StaticEntity(EntityType.WALL));
                                break;
                            case 2:
                                setTile(x, y + 1, new StaticEntity(EntityType.WALL));
                                break;
                            case 3:
                                setTile(x + 1, y, new StaticEntity(EntityType.WALL));
                                break;
                        }
                    }
                }
            }
        }
    }

    private void fillInaccessibleAreasWithWalls() {
        // Use the flood fill algorithm starting from the player's starting position
        boolean[][] accessible = new boolean[height][width];
        floodFill(accessible, 0, 0); // Assuming player starts at (0, 0)

        // Fill any area that is not accessible with walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!accessible[y][x] && map[y][x].getType() != EntityType.EXIT) { // Don't fill the exit with a wall
                    setTile(x, y, new StaticEntity(EntityType.WALL)); // Fill with a wall
                }
            }
        }
    }

    private void floodFill(boolean[][] accessible, int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }

        if (map[y][x].getType() == EntityType.WALL || accessible[y][x]) {
            return;
        }

        accessible[y][x] = true;

        floodFill(accessible, x - 1, y); // Left
        floodFill(accessible, x + 1, y); // Right
        floodFill(accessible, x, y - 1); // Up
        floodFill(accessible, x, y + 1); // Down
    }

    private boolean areExitsAndDoorsReachable() {
        boolean allEdgeMiddlesReachable;
        boolean exitReachable = isTileReachable(getExitX(), getExitY());
        allEdgeMiddlesReachable = exitReachable;

        if (exitReachable) {
            int[] edgeMiddles = {width / 2, 0, width - 1, height / 2, width / 2, height - 1, 0, height / 2};
            for (int i = 0; i < edgeMiddles.length; i += 2) {
                if (!isTileReachable(edgeMiddles[i], edgeMiddles[i + 1])) {
                    allEdgeMiddlesReachable = false;
                    break;
                }
            }
        }
        return allEdgeMiddlesReachable;
    }

    private boolean isTileReachable(int tileX, int tileY) {
        boolean[][] visited = new boolean[height][width];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, 0});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];
            visited[y][x] = true;
            if (x == tileX && y == tileY) {
                return true;
            }
            if (isValidTile(x - 1, y) && !visited[y][x - 1]) { // Left
                stack.push(new int[]{x - 1, y});
            }
            if (isValidTile(x + 1, y) && !visited[y][x + 1]) { // Right
                stack.push(new int[]{x + 1, y});
            }
            if (isValidTile(x, y - 1) && !visited[y - 1][x]) { // Up
                stack.push(new int[]{x, y - 1});
            }
            if (isValidTile(x, y + 1) && !visited[y + 1][x]) { // Down
                stack.push(new int[]{x, y + 1});
            }
        }
        return false;
    }

    private boolean isValidTile(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && map[y][x].getType() != EntityType.WALL;
    }

    public void addDoor(int x, int y) {
        setTile(x, y, new StaticEntity(EntityType.DOOR));
        doorPositions.add(new Point(x, y));
    }

    public boolean isDoor(int x, int y) {
        return doorPositions.contains(new Point(x, y));
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public Entity getTile(int x, int y) {
        return map[y][x];
    }

    public void setTile(int x, int y, Entity object) {
        map[y][x] = object;
    }

    public int getExitX() {
        return exitX;
    }

    public int getExitY() {
        return exitY;
    }

    public void setExitX(int x) {
        this.exitX = x;
    }

    public void setExitY(int y) {
        this.exitY = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Entity[][] getMap() {
        return map;
    }
}
