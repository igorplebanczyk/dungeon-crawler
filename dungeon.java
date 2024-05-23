import java.util.Random;
import java.util.Stack;

public class Dungeon {
    private final int width;
    private final int height;
    private final char[][] map;
    private final Random random = new Random();
    public int exitX;
    public int exitY;

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new char[height][width];
        generateDungeon();
    }

    private void generateDungeon() {
        // ... (existing code for generating initial dungeon with walls and floors)

        // Guaranteed path creation
        int startX = 0;
        int startY = 0;
        carvePath(startX, startY, exitX, exitY);

        // ... (remaining dungeon generation steps)
    }

    private void carvePath(int x, int y, int targetX, int targetY) {
        if (x == targetX && y == targetY) {
            return; // Reached target exit
        }

        // Mark current tile as visited
        map[y][x] = '.';

        // Check adjacent tiles ( prioritizing closer to target )
        int[] directions = {RIGHT, DOWN, LEFT, UP}; // prioritize right and down for a more direct path
        for (int direction : directions) {
            int newX = x + getDeltaX(direction);
            int newY = y + getDeltaY(direction);
            if (isValidTile(newX, newY) && map[newY][newX] != '.') {
                carvePath(newX, newY, targetX, targetY);
            }
        }
    }

    // Helper methods for getting X and Y deltas based on direction
    private static int getDeltaX(int direction) {
        return (direction == RIGHT) ? 1 : ((direction == LEFT) ? -1 : 0);
    }

    private static int getDeltaY(int direction) {
        return (direction == DOWN) ? 1 : ((direction == UP) ? -1 : 0);
    }


    private boolean isValidTile(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height && map[y][x] != '#';
    }

    public char getTile(int x, int y) {
        return map[y][x];
    }

    public void setTile(int x, int y, char tile) {
        map[y][x] = tile;
    }

    public int[] getRandomTileInBottomRightQuadrant() {
        int x = random.nextInt(width / 2) + width / 2;
        int y = random.nextInt(height / 2) + height / 2;
        return new int[]{x, y};
    }
}
 
