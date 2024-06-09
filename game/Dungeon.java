package game;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.*;
import java.util.concurrent.*;

public class Dungeon {
    // Dungeon properties
    private final int width;
    private final int height;
    private final int gridX;
    private final int gridY;
    private int exitX;
    private int exitY;
    public boolean doesHaveExit = false;
    private final List<Point> doorPositions = new ArrayList<>();

    // Dungeon objects
    public final char[][] map; // 2D array to represent the dungeon map
    private final Random random = new Random();

    public Dungeon(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.gridX = x;
        this.gridY = y;
        this.map = new char[height][width];

        // Select a tile where the exit might be placed
        int[] exit = getPossibleExitTile();
        this.setExitX(exit[0]);
        this.setExitY(exit[1]);

        generateDungeon();// Generate the dungeon layout
    }

    // Select a random tile in the bottom right quadrant
    private int[] getPossibleExitTile() {
        int x = random.nextInt(width / 2) + width / 2;
        int y = random.nextInt(height / 2) + height / 2;
        return new int[]{x, y};
    }

    // Generate the dungeon layout
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
                    map[y][x] = '#'; // Wall
                } else {
                    map[y][x] = '.'; // Floor
                }
            }
        }
    }
    
    // Ensure each wall has at least one adjacent wall
    private void ensureAdjacentWalls() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (map[y][x] == '#') { // If current tile is wall
                    // Check surrounding tiles
                    if (map[y - 1][x] == '.' && map[y][x - 1] == '.' && map[y + 1][x] == '.' && map[y][x + 1] == '.') {
                        // If no adjacent walls, make one adjacent wall
                        int direction = random.nextInt(4); // 0: Up, 1: Left, 2: Down, 3: Right
                        switch (direction) {
                            case 0:
                                map[y - 1][x] = '#';
                                break;
                            case 1:
                                map[y][x - 1] = '#';
                                break;
                            case 2:
                                map[y + 1][x] = '#';
                                break;
                            case 3:
                                map[y][x + 1] = '#';
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
                if (!accessible[y][x] && map[y][x] != 'E') { // Don't fill the exit with a wall
                    map[y][x] = '#';
                }
            }
        }
    }
    
    // Mark all accessible tiles from a given position using DFS
    private void floodFill(boolean[][] accessible, int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return; // Out of bounds
        }
        if (map[y][x] == '#' || accessible[y][x]) {
            return; // Wall or already visited
        }

        accessible[y][x] = true; // Mark as accessible

        // Recursively flood fill the neighboring tiles
        floodFill(accessible, x - 1, y); // Left
        floodFill(accessible, x + 1, y); // Right
        floodFill(accessible, x, y - 1); // Up
        floodFill(accessible, x, y + 1); // Down
    }
    
    // Method to check if the exit and all doors are reachable
    private boolean areExitsAndDoorsReachable() {
        boolean allEdgeMiddlesReachable;
        // Check if there exists a path from player's starting position to the exit
        boolean exitReachable = isTileReachable(getExitX(), getExitY());
        allEdgeMiddlesReachable = exitReachable; // Initially set allEdgeMiddlesReachable to the value of exitReachable

        if (exitReachable) { // If the exit is reachable, check the reachability of all edge middles
            int[] edgeMiddles = {width / 2, 0, width - 1, height / 2, width / 2, height - 1, 0, height / 2}; // Middle points of each edge
            for (int i = 0; i < edgeMiddles.length; i += 2) {
                if (!isTileReachable(edgeMiddles[i], edgeMiddles[i + 1])) {
                    allEdgeMiddlesReachable = false; // If any edge middle is not reachable, set allEdgeMiddlesReachable to false and break the loop
                    break;
                }
            }
        }
        return allEdgeMiddlesReachable;
    }
    
    // Check if a tile is reachable from the start position using DFS
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
            // Check adjacent tiles
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

    // Check if a tile is valid (within bounds and not a wall)
    private boolean isValidTile(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && map[y][x] != '#';
    }

    // Check if a tile is valid for the pathfinder
    private boolean isValidTileForPathfinder(int x, int y) {
        if (Game.isBulldozerMode()) {
            return x >= 0 && x < width && y >= 0 && y < height && map[y][x] != 'E' && map[y][x] != 'D';
        } else {
            return x >= 0 && x < width && y >= 0 && y < height && map[y][x] != '#' && map[y][x] != 'E' && map[y][x] != 'D';
        }
    }
    
    // Breadth-first search algorithm to find the shortest path between two points
    public List<Point> findPath(int startX, int startY, int goalX, int goalY) throws InterruptedException, ExecutionException, TimeoutException {
        // Run BFS in a separate thread with a timeout
        CompletableFuture<List<Point>> future = CompletableFuture.supplyAsync(() -> {
            // Use a Queue for BFS
            Queue<Point> frontier = new LinkedList<>();
            frontier.add(new Point(startX, startY));
            Map<Point, Point> cameFrom = new HashMap<>();
            cameFrom.put(new Point(startX, startY), null);

            // Use a boolean[][] to keep track of visited nodes
            boolean[][] visited = new boolean[height][width];
            visited[startY][startX] = true;

            while (!frontier.isEmpty()) {
                Point current = frontier.poll();
                int x = current.x;
                int y = current.y;
                if (x == goalX && y == goalY) {
                    List<Point> path = new ArrayList<>();
                    while (current != null) {
                        path.addFirst(current);
                        current = cameFrom.get(current);
                    }
                    return path; // Early exit if goal is found
                }

                // Check adjacent tiles
                if (isValidTileForPathfinder(x - 1, y) && !visited[y][x - 1]) { // Left
                    frontier.add(new Point(x - 1, y));
                    cameFrom.put(new Point(x - 1, y), current);
                    visited[y][x - 1] = true; // Mark node as visited
                }
                if (isValidTileForPathfinder(x + 1, y) && !visited[y][x + 1]) { // Right
                    frontier.add(new Point(x + 1, y));
                    cameFrom.put(new Point(x + 1, y), current);
                    visited[y][x + 1] = true;
                }
                if (isValidTileForPathfinder(x, y - 1) && !visited[y - 1][x]) { // Up
                    frontier.add(new Point(x, y - 1));
                    cameFrom.put(new Point(x, y - 1), current);
                    visited[y - 1][x] = true;
                }
                if (isValidTileForPathfinder(x, y + 1) && !visited[y + 1][x]) { // Down
                    frontier.add(new Point(x, y + 1));
                    cameFrom.put(new Point(x, y + 1), current);
                    visited[y + 1][x] = true;
                }
            }

            return null; // No path found
        });

        // Return the result of the BFS method, or throw a TimeoutException if it does not complete within 5 seconds
        return future.orTimeout(5, TimeUnit.SECONDS).get();
    }

    // Add a door at a given position
    public void addDoor(int x, int y) {
        map[y][x] = 'D'; // 'D' represents a door
        doorPositions.add(new Point(x, y)); // Add the door position to the list
    }

    // Check if a tile is a door
    public boolean isDoor(int x, int y) {
        return doorPositions.contains(new Point(x, y));
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    // Get the tile type at a given position
    public char getTile(int x, int y) {
        return map[y][x];
    }

    // set the tile type at a given position
    public void setTile(int x, int y, char tile) {
        map[y][x] = tile;
    }

    public int getExitY() {
        return exitY;
    }

    public void setExitY(int exitY) {
        this.exitY = exitY;
    }

    public int getExitX() {
        return exitX;
    }

    public void setExitX(int exitX) {
        this.exitX = exitX;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
