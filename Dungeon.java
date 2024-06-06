import java.util.Random;
import java.util.Stack;
import java.util.*;

public class Dungeon {
    // Dungeon dimensions
    private final int width;
    private final int height;

    // 2D array to represent the dungeon map
    private final char[][] map;

    // Random number generator
    private final Random random = new Random();

    // Coordinates of the exit
    public int exitX;
    public int exitY;

    // Constructor to initialize the dungeon with given dimensions
    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new char[height][width];
        generateDungeon(); // Generate the dungeon layout
    }

    // Method to generate the dungeon layout
    private void generateDungeon() {
        boolean exitReachable = false;
        while (!exitReachable) {
            // Generate initial dungeon with walls and floors
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (random.nextDouble() < 0.4) { // Increased chance for walls
                        map[y][x] = '#'; // Wall
                    } else {
                        map[y][x] = '.'; // Floor
                    }
                }
            }

            // Ensure each wall has at least one adjacent wall
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

            // Place exit in the bottom right quadrant
            int[] exitPosition = getRandomTileInBottomRightQuadrant();
            exitX = exitPosition[0];
            exitY = exitPosition[1];
            map[exitY][exitX] = 'E';

            // Set player starting position
            map[0][0] = 'P';
            map[0][1] = '.'; // Ensure path is possible
            map[1][0] = '.';

            // Check if there exists a path from player's starting position to the exit
            exitReachable = isExitReachable(0, 0);
        }
    }

    // Method to check if the exit is reachable from a given position using DFS
    private boolean isExitReachable(int startX, int startY) {
        boolean[][] visited = new boolean[height][width];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int x = current[0];
            int y = current[1];
            visited[y][x] = true;
            if (x == exitX && y == exitY) {
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

    // Method to check if a tile is valid (within bounds and not a wall)
    private boolean isValidTile(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && map[y][x] != '#';
    }

    // Method to get the tile type at a given position
    public char getTile(int x, int y) {
        return map[y][x];
    }

    // Method to set the tile type at a given position
    public void setTile(int x, int y, char tile) {
        map[y][x] = tile;
    }

    // Method to generate a random tile in the bottom right quadrant
    public int[] getRandomTileInBottomRightQuadrant() {
        int x = random.nextInt(width / 2) + width / 2;
        int y = random.nextInt(height / 2) + height / 2;
        return new int[]{x, y};
    }

    public List<int[]> aStar(int[] start, int[] goal) {
        PriorityQueue<int[]> frontier = new PriorityQueue<>(Comparator.comparingInt(pos -> Math.abs(pos[0] - goal[0]) + Math.abs(pos[1] - goal[1])));
        frontier.add(start);
        Map<int[], int[]> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        while (!frontier.isEmpty()) {
            int[] current = frontier.poll();

            if (Arrays.equals(current, goal)) {
                List<int[]> path = new ArrayList<>();
                while (current != null) {
                    path.add(0, current);
                    current = cameFrom.get(current);
                }
                return path;
            }

            for (int[] next : getNeighbors(current)) {
                if (!cameFrom.containsKey(next)) {
                    frontier.add(next);
                    cameFrom.put(next, current);
                }
            }
        }

        return null; // No path found
    }

    // Get valid neighbors for A*
    public List<int[]> getNeighbors(int[] pos) {
        List<int[]> neighbors = new ArrayList<>();
        if (isValidTile(pos[0] - 1, pos[1])) { // Left
            neighbors.add(new int[]{pos[0] - 1, pos[1]});
        }
        if (isValidTile(pos[0] + 1, pos[1])) { // Right
            neighbors.add(new int[]{pos[0] + 1, pos[1]});
        }
        if (isValidTile(pos[0], pos[1] - 1)) { // Up
            neighbors.add(new int[]{pos[0], pos[1] - 1});
        }
        if (isValidTile(pos[0], pos[1] + 1)) { // Down
            neighbors.add(new int[]{pos[0], pos[1] + 1});
        }
        return neighbors;
    }
}
