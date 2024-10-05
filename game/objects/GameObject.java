package game.objects;

public abstract class GameObject {
    protected int x;
    protected int y;
    protected String imagePath;

    protected int[] getPosition() {
        return new int[]{x, y};
    }

    protected void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected String getImagePath() {
        return imagePath;
    }
}
