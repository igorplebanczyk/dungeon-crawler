package game.objects;

public class Player extends GameObject {
    private int x;
    private int y;

    public Player(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.type = Tile.PLAYER;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public int[] getPosition() {
        return new int[]{x, y};
    }
}
