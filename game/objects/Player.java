package game.objects;

public class Player implements Object {
    private int x;
    private int y;
    private final String imagePath;

    public Player(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
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
