package game.objects;

public class Floor implements Object {
    private int x;
    private int y;
    private static final String imagePath = "/images/floor.png";

    public Floor(int x, int y) {
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
