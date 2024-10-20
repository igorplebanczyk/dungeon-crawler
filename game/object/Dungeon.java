package game.object;

import game.GameState;
import game.object.entity.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Dungeon {
    // Dungeon objects
    private final Entity[][] map; // 2D array to represent the dungeon map
    private final Random random = new Random();

    // Dungeon properties
    private final int width;
    private final int height;
    private final int gridX;
    private final int gridY;
    private int exitX;
    private int exitY;
    private final List<Point> doorPositions = new ArrayList<>();
    private boolean doesHaveExit = false;


    public Dungeon(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.gridX = x;
        this.gridY = y;
        this.map = new Entity[height][width];

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
                    map[y][x] = new Wall(); // Wall
                } else {
                    map[y][x] = new Floor(); // Floor
                }
            }
        }
    }

    // Ensure each wall has at least one adjacent wall
    private void ensureAdjacentWalls() {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (map[y][x].getType() == EntityType.WALL) { // If current tile is wall
                    // Check surrounding tiles
                    if (map[y - 1][x].getType() == EntityType.FLOOR && map[y][x - 1].getType() == EntityType.FLOOR && map[y + 1][x].getType() == EntityType.FLOOR && map[y][x + 1].getType() == EntityType.FLOOR) {
                        // If no adjacent walls, make one adjacent wall
                        int direction = random.nextInt(4); // 0: Up, 1: Left, 2: Down, 3: Right
                        switch (direction) {
                            case 0:
                                setTile(x, y - 1, new Wall());
                                break;
                            case 1:
                                setTile(x - 1, y, new Wall());
                                break;
                            case 2:
                                setTile(x, y + 1, new Wall());
                                break;
                            case 3:
                                setTile(x + 1, y, new Wall());
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
                    setTile(x, y, new Wall()); // Fill with a wall
                }
            }
        }
    }

    // Mark all accessible tiles from a given position using DFS
    private void floodFill(boolean[][] accessible, int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return; // Out of bounds
        }
        if (map[y][x].getType() == EntityType.WALL || accessible[y][x]) {
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

    // Check if a tile is reachable from the start position using
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
        return x >= 0 && x < width && y >= 0 && y < height && map[y][x].getType() != EntityType.WALL;
    }

    // Node class to hold information for A* algorithm
    private static class Node implements Comparable<Node> {
        public Point point;
        public Node parent;
        public double g; // Cost from start to current node
        public double h; // Heuristic cost to goal
        public double f; // Total cost (g + h)

        public Node(Point point, Node parent, double g, double h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    private boolean isValidTileForPathfinder(int x, int y) {
        if (GameState.isBulldozerMode()) {
            return x >= 0 && x < width && y >= 0 && y < height && map[y][x].getType() != EntityType.EXIT && map[y][x].getType() != EntityType.DOOR;
        } else {
            return x >= 0 && x < width && y >= 0 && y < height && map[y][x].getType() != EntityType.WALL && map[y][x].getType() != EntityType.EXIT && map[y][x].getType() != EntityType.DOOR;
        }
    }

    private List<Point> reconstructPath(Node currentNode) {
        List<Point> path = new LinkedList<>();
        while (currentNode != null) {
            path.add(currentNode.point);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path); // Reverse the path to get from start to goal
        return path;
    }

    private double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private List<Point> getNeighbors(Point point) {
        List<Point> neighbors = new ArrayList<>();
        int x = point.x;
        int y = point.y;
        if (isValidTileForPathfinder(x - 1, y)) neighbors.add(new Point(x - 1, y)); // Left
        if (isValidTileForPathfinder(x + 1, y)) neighbors.add(new Point(x + 1, y)); // Right
        if (isValidTileForPathfinder(x, y - 1)) neighbors.add(new Point(x, y - 1)); // Up
        if (isValidTileForPathfinder(x, y + 1)) neighbors.add(new Point(x, y + 1)); // Down
        return neighbors;
    }

    // A* algorithm
    public List<Point> findPath(int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();

        Node startNode = new Node(new Point(startX, startY), null, 0, heuristic(startX, startY, goalX, goalY));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            // Check if we reached the goal
            if (currentNode.point.x == goalX && currentNode.point.y == goalY) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.point);

            // Explore neighbors
            for (Point neighbor : getNeighbors(currentNode.point)) {
                if (closedSet.contains(neighbor) || !isValidTileForPathfinder(neighbor.x, neighbor.y)) {
                    continue; // Skip if already visited or not valid
                }

                double tentativeG = currentNode.g + 1; // Assuming cost of 1 for each step
                Node neighborNode = new Node(neighbor, currentNode, tentativeG, heuristic(neighbor.x, neighbor.y, goalX, goalY));

                // Check if this path to the neighbor is better
                if (!openSet.contains(neighborNode) || tentativeG < neighborNode.g) {
                    openSet.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }

    public void addDoor(int x, int y) {
        setTile(x, y, new Door()); // 'D' represents a door
        doorPositions.add(new Point(x, y)); // Add the door position to the list
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

    public boolean doesHaveExit() {
        return doesHaveExit;
    }

    public void setDoesHaveExit(boolean doesHaveExit) {
        this.doesHaveExit = doesHaveExit;
    }
}
