public class Player {
    // Player's coordinates
    private int x;
    private int y;

    // Constructor to initialize player's position
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Method to get the player's x-coordinate
    public int getX() {
        return x;
    }

    // Method to get the player's y-coordinate
    public int getY() {
        return y;
    }

    // Method to move the player by specified amounts in the x and y directions
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    // Method to get the player's position as an array of integers [x, y]
    public int[] getPosition() {
        return new int[]{x, y};
    }
}
