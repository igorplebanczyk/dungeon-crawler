package game.objects;

public class Wall implements Object {
    private int x;
    private int y;
    private static final String imagePath = "/images/wall.png";

    public Wall(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int[] getPosition() {
        return new int[]{x, y};
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String getImagePath() {
        return imagePath;
    }
}
