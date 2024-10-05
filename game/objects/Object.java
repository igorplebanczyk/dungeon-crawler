package game.objects;

public interface Object {
    String imagePath = null;
    int x = -1;
    int y = -1;

    public int[] getPosition();
    public void setPosition(int x, int y);
    public String getImagePath();
}
